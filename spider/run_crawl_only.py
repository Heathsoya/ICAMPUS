import logging

from config.logging_config import setup_logging
from core.crawler import crawl_all
from core.storage import ensure_data_dirs
from alert import send_exception_alert


def main():
    setup_logging()
    ensure_data_dirs()
    try:
        items = crawl_all()
        logging.info("已爬取 %s 条原始公告，已保存到 data/raw/。", len(items))
    except Exception as exc:
        logging.exception("爬虫抓取失败: %s", exc)
        send_exception_alert("爬虫抓取失败", exc, "crawl_only 模式执行失败")
        raise


if __name__ == "__main__":
    main()
