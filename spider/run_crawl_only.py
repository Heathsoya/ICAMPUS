from config.logging_config import setup_logging

from core.crawler import crawl_all
from core.storage import ensure_data_dirs


def main():
    setup_logging()
    ensure_data_dirs()
    items = crawl_all()
    print(f"已爬取 {len(items)} 条原始公告，已保存到 data/raw/。")


if __name__ == "__main__":
    main()
