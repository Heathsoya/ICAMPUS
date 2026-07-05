import argparse
import logging

from config.logging_config import setup_logging
from core.crawler import crawl_all
from core.storage import ensure_data_dirs
from run_crawl_only import main as crawl_main
from run_process_only import main as process_main
from alert import send_exception_alert


def parse_args():
    parser = argparse.ArgumentParser(description="公告爬虫与问答转换运行入口")
    parser.add_argument("--crawl-only", action="store_true", help="仅执行爬取，不处理 LLM")
    parser.add_argument("--process-only", action="store_true", help="仅处理已有原始数据，不爬取")
    return parser.parse_args()


def main():
    setup_logging()
    args = parse_args()
    ensure_data_dirs()

    try:
        if args.crawl_only:
            crawl_main()
            return

        if args.process_only:
            process_main()
            return

        logging.info("开始完整流程：爬取 + 处理 + 导出")
        crawl_main()
        process_main()
    except Exception as exc:
        logging.exception("运行失败: %s", exc)
        send_exception_alert("爬虫运行失败", exc, "完整流程执行失败")
        raise


if __name__ == "__main__":
    main()
