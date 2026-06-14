from config.logging_config import setup_logging

from core.crawler import crawl_all
from core.storage import ensure_data_dirs
from alert import send_exception_alert


def main():
    setup_logging()
    ensure_data_dirs()
    try:
        items = crawl_all()
        print(f"已爬取 {len(items)} 条原始公告，已保存到 data/raw/。")
    except Exception as exc:
        send_exception_alert("爬虫抓取失败", exc, "crawl_only 模式执行失败")
        raise


if __name__ == "__main__":
    main()
