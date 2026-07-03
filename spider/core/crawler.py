import logging
import time
import random
import json
from pathlib import Path

import requests

from config import settings
from .parser import extract_clean_content, extract_source_department
from .state import ensure_state_db, is_post_crawled, mark_post_crawled
from .storage import ensure_data_dirs, raw_item_exists, save_raw_item

logger = logging.getLogger(__name__)

HEADERS = {
    "User-Agent": settings.HTTP_REQUEST["user_agent"],
    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
    "X-Requested-With": "XMLHttpRequest",
    "Referer": "https://jw.scut.edu.cn/zhinan/cms/toPosts.do",
    "Origin": "https://jw.scut.edu.cn",
}


def sleep_between_requests():
    time.sleep(settings.HTTP_REQUEST.get("delay_seconds", 1))


def fetch_posts_list(page_num=1, tag=6, page_size=15):
    site = settings.CRAWL_SITES[0]
    data = {
        "category": site.get("category_default", "0"),
        "tag": str(tag),
        "pageNum": str(page_num),
        "pageSize": str(page_size),
        "keyword": "",
    }
    retries = settings.HTTP_REQUEST.get("retries", 3)
    backoff = settings.HTTP_REQUEST.get("backoff_factor", 0.5)
    max_backoff = settings.HTTP_REQUEST.get("max_backoff_seconds", 30)

    for attempt in range(retries):
        try:
            response = requests.post(
                site["list_api_url"],
                data=data,
                headers=HEADERS,
                timeout=settings.HTTP_REQUEST.get("timeout", 15),
            )
            try:
                response.raise_for_status()
            except Exception as http_exc:
                logger.warning("列表接口返回状态异常 (attempt %s/%s): %s %s", attempt + 1, retries, response.status_code, http_exc)
                # fallthrough to retry logic

            try:
                payload = response.json()
            except ValueError as json_exc:
                logger.warning("解析列表接口 JSON 失败 (attempt %s/%s): %s", attempt + 1, retries, json_exc)
                payload = {}

            posts = payload.get("list", [])
            total = payload.get("total", 0)
            return posts, total
        except requests.RequestException as exc:
            logger.warning(
                "列表接口请求异常 (attempt %s/%s): %s",
                attempt + 1,
                retries,
                exc,
            )
        except Exception as exc:
            logger.exception("未知错误于列表请求 (attempt %s/%s): %s", attempt + 1, retries, exc)

        # 重试前的指数退避 + 抖动
        if attempt < retries - 1:
            sleep_for = min(max_backoff, backoff * (2 ** attempt))
            jitter = random.uniform(0, sleep_for * 0.1)
            total_sleep = sleep_for + jitter
            logger.info("列表接口重试，等待 %.2f 秒后再试 (attempt %s/%s)", total_sleep, attempt + 1, retries)
            time.sleep(total_sleep)

    logger.error("列表接口最终失败: %s (tag=%s, page=%s)", site.get("list_api_url"), tag, page_num)
    return [], 0


def fetch_detail(post_id):
    site = settings.CRAWL_SITES[0]
    url = site["detail_url_template"].format(id=post_id)
    retries = settings.HTTP_REQUEST.get("retries", 3)
    backoff = settings.HTTP_REQUEST.get("backoff_factor", 0.5)
    max_backoff = settings.HTTP_REQUEST.get("max_backoff_seconds", 30)

    for attempt in range(retries):
        try:
            response = requests.get(
                url,
                headers={"User-Agent": settings.HTTP_REQUEST["user_agent"]},
                timeout=settings.HTTP_REQUEST.get("timeout", 15),
            )
            try:
                response.raise_for_status()
            except Exception as http_exc:
                logger.warning("详情页返回状态异常 (attempt %s/%s): %s %s", attempt + 1, retries, response.status_code, http_exc)
                # 仍尝试解析页面内容以便排查

            response.encoding = response.apparent_encoding
            title, content = extract_clean_content(response.text)
            return {
                "title": title,
                "content": content,
                "url": url,
            }
        except requests.RequestException as exc:
            logger.warning(
                "详情页请求异常 (attempt %s/%s): %s",
                attempt + 1,
                retries,
                exc,
            )
        except Exception as exc:
            logger.exception("详情页处理失败 (attempt %s/%s): %s", attempt + 1, retries, exc)

        if attempt < retries - 1:
            sleep_for = min(max_backoff, backoff * (2 ** attempt))
            jitter = random.uniform(0, sleep_for * 0.1)
            total_sleep = sleep_for + jitter
            logger.info("详情页重试，等待 %.2f 秒后再试 (attempt %s/%s)", total_sleep, attempt + 1, retries)
            time.sleep(total_sleep)

    logger.error("详情页最终失败: %s", url)
    return None


def crawl_category(tag, tag_name, page_num=1, page_size=15, limit=None):
    logger.info("爬取分类: %s (tag=%s, page=%s, size=%s)", tag_name, tag, page_num, page_size)

    posts, total = fetch_posts_list(page_num=page_num, tag=tag, page_size=page_size)
    if not posts:
        return []

    records = []
    for index, post in enumerate(posts, start=1):
        if limit is not None and len(records) >= limit:
            break

        post_id = post.get("id")
        if not post_id:
            continue

        if settings.ENABLE_STATE_DB and is_post_crawled(post_id):
            logger.info("已跳过已爬取公告: %s", post_id)
            continue

        post_date = post.get("createTime", "未知")
        list_title = post.get("title", "无标题")

        logger.info("[%s/%s] %s | %s", index, len(posts), post_date, list_title[:40])

        detail = fetch_detail(post_id)
        if not detail or not detail["content"]:
            logger.warning("详情提取失败: %s", post_id)
            continue

        source_department = extract_source_department(detail["content"]) or tag_name or settings.SOURCE_DEPARTMENT_DEFAULT

        raw_item = {
            "site_id": settings.CRAWL_SITES[0]["site_id"],
            "post_id": post_id,
            "list_title": list_title,
            "publish_date": post_date,
            "url": detail["url"],
            "extracted_title": detail["title"],
            "content": detail["content"],
            "category_tag": tag_name,
            "source_department": source_department,
            "fetched_at": time.strftime("%Y-%m-%d %H:%M:%S"),
        }

        if settings.ENABLE_STATE_DB and raw_item_exists(raw_item):
            logger.info("发现相同post_id raw文件已存在，跳过: %s", post_id)
            continue

        raw_filepath = save_raw_item(raw_item)
        if settings.ENABLE_STATE_DB:
            mark_post_crawled(
                post_id=post_id,
                url=detail["url"],
                category_tag=tag_name,
                source_department=source_department,
                title=list_title,
                raw_file=str(raw_filepath),
            )

        records.append(raw_item)
        sleep_between_requests()

    return records


def crawl_all(limit=None):
    ensure_data_dirs()
    if settings.ENABLE_STATE_DB:
        ensure_state_db()

    all_results = []
    for tag, name, pages, page_size in settings.CRAWL_TARGETS:
        category_count = 0
        for page in range(1, pages + 1):
            category_remaining = settings.CRAWL_MAX_NEW_PER_CATEGORY - category_count
            if category_remaining <= 0:
                break

            if limit is not None:
                global_remaining = limit - len(all_results)
                if global_remaining <= 0:
                    return all_results
                category_remaining = min(category_remaining, global_remaining)

            results = crawl_category(
                tag,
                name,
                page_num=page,
                page_size=page_size,
                limit=category_remaining,
            )
            all_results.extend(results)
            category_count += len(results)
    return all_results


if __name__ == "__main__":
    from config.logging_config import setup_logging

    setup_logging()
    items = crawl_all()
    print(f"总共爬取 {len(items)} 条原始公告")
