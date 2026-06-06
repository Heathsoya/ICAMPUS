import json
import logging
import re
from datetime import datetime
from typing import Optional

from config import settings
from .client import call_llm
from .prompt import build_prompt

logger = logging.getLogger(__name__)


def _safe_parse_json(text: str):
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        # 尝试从文本中提取第一个 JSON 对象
        match = re.search(r"\{(?:[^{}]|\{[^{}]*\})*\}", text, re.S)
        if match:
            try:
                return json.loads(match.group(0))
            except json.JSONDecodeError:
                pass
        # 尝试提取第一个 JSON 数组
        match = re.search(r"\[(?:[^\[\]]|\[[^\[\]]*\])*\]", text, re.S)
        if match:
            try:
                return json.loads(match.group(0))
            except json.JSONDecodeError:
                return None
        return None


def _has_llm_credentials() -> bool:
    provider = settings.LLM_PROVIDER.lower()
    if provider == "openai":
        return bool(settings.LLM_API_KEY)
    if provider == "deepseekapi":
        return bool(settings.DEEPSEEKAPI_KEY)
    return False


def _normalize_qa_pairs(parsed):
    if isinstance(parsed, list):
        return parsed
    if isinstance(parsed, dict):
        if "qa_pairs" in parsed:
            return parsed.get("qa_pairs") or []
        if "question" in parsed and "answer" in parsed:
            return [{"question": parsed.get("question", ""), "answer": parsed.get("answer", "")}]
    return []


def _normalize_category(category: str, raw_category_tag: Optional[str] = None) -> str:
    if not category:
        category = ""
    category = str(category).strip()
    if category in settings.CATEGORIES:
        return category

    mapped = settings.CATEGORY_MAP.get(category)
    if mapped:
        return mapped

    normalized = category.lower()
    if any(token in normalized for token in ["考试", "教务", "课程", "选课", "学期", "重考", "重修"]):
        return "教务教学"
    if any(token in normalized for token in ["活动", "讲座", "比赛", "交流", "公示"]):
        return "校园活动"
    if any(token in normalized for token in ["住宿", "宿舍"]):
        return "住宿生活"
    if any(token in normalized for token in ["餐饮", "食堂", "饭", "饮食"]):
        return "餐饮服务"
    if any(token in normalized for token in ["图书馆", "借阅", "馆藏"]):
        return "图书馆"
    if any(token in normalized for token in ["财务", "缴费", "学费", "报销", "费用"]):
        return "财务缴费"
    if any(token in normalized for token in ["就业", "毕业", "实习", "留学", "招生", "录取"]):
        return "就业毕业"
    if raw_category_tag:
        fallback = settings.CATEGORY_MAP.get(raw_category_tag.strip())
        if fallback:
            return fallback
    return "综合咨询"


def _normalize_source(raw_item: dict) -> str:
    raw_source = (raw_item.get("category_tag") or "").strip()
    if re.search(r"教务处|学生处|学院|系|办公室|中心|部", raw_source):
        return raw_source
    return settings.SOURCE_DEPARTMENT_DEFAULT


def process_raw_item(raw_item: dict) -> list[dict]:
    prompt = build_prompt(raw_item)
    llm_text = None
    if _has_llm_credentials():
        llm_text = call_llm(prompt)
    else:
        logger.info("未检测到可用的 LLM 凭证，使用本地占位结果生成。")
        title = raw_item.get("list_title", "")
        keywords = [title[i:i+2] for i in range(0, min(len(title), 6), 2) if title[i:i+2].strip()]
        if len(keywords) < 3:
            keywords = (keywords + ["公告", "通知", "安排"])[:3]
        llm_text = json.dumps({
            "qa_pairs": [{
                "question": "这则公告的主要内容是什么？",
                "answer": raw_item["content"][:180],
            }],
            "category": settings.CATEGORIES[0] if settings.CATEGORIES else "",
            "keywords": keywords,
            "skip_flag": False,
            "skip_reason": "",
        }, ensure_ascii=False)

    parsed = _safe_parse_json(llm_text)
    if not parsed:
        raise ValueError("LLM 返回无法解析为 JSON")

    qa_pairs = _normalize_qa_pairs(parsed)
    if not qa_pairs:
        if isinstance(parsed, dict) and parsed.get("skip_flag"):
            return []
        raise ValueError("LLM 返回的 QA 对列表为空")

    category = _normalize_category(parsed.get("category", ""), raw_item.get("category_tag"))
    keywords = parsed.get("keywords", [])
    skip_flag = parsed.get("skip_flag", False)
    skip_reason = parsed.get("skip_reason", "")
    source = _normalize_source(raw_item)

    records = []
    for pair in qa_pairs:
        answer = pair.get("answer", "") or ""
        max_chars = getattr(settings, 'ANSWER_MAX_CHARS', 200)
        if len(answer) > max_chars:
            logger.warning("答案超过 %s 字，已自动截断。链接: %s", max_chars, raw_item.get("url"))
            answer = answer[:max_chars]

        post_id = raw_item.get("post_id") or raw_item.get("id")
        records.append({
            "question": pair.get("question", ""),
            "answer": answer,
            "category": category,
            "keywords": keywords,
            "skip_flag": skip_flag,
            "skip_reason": skip_reason,
            "original_title": raw_item.get("list_title", ""),
            "original_publish_date": raw_item.get("publish_date", ""),
            "source_department": raw_item.get("category_tag", ""),
            "source": source,
            "original_url": raw_item.get("url", ""),
            "post_id": post_id,
            "confidence_score": 0.8 if _has_llm_credentials() else 1.0,
            "created_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        })

    # 尝试保存处理后的记录到 data/processed，方便人工审核
    try:
        from core.storage import save_processed_item
        save_processed_item(raw_item, records)
    except Exception:
        logger.exception('保存 processed JSON 失败')

    return records
