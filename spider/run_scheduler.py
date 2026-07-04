import logging
import os
from datetime import datetime

from apscheduler.schedulers.blocking import BlockingScheduler

from config.logging_config import setup_logging
from core.storage import ensure_data_dirs
from core.crawler import crawl_all
from alert import send_exception_alert


def job_once():
    try:
        logging.info("定时任务开始执行: %s", datetime.now().isoformat())
        items = crawl_all()
        logging.info("定时爬取完成：共爬取 %s 条原始公告", len(items))
        return items
    except Exception as exc:
        logging.exception("定时爬取失败: %s", exc)
        try:
            send_exception_alert("定时爬取失败", exc, "scheduler")
        except Exception:
            logging.exception("发送报警失败")
    return None


def main():
    setup_logging()
    ensure_data_dirs()

    # 配置采集间隔：优先使用按天的配置（每隔 N 天），否则回退到分钟配置
    # 默认每 7 天检查一次
    interval_days = os.getenv("SCHEDULE_INTERVAL_DAYS")
    if interval_days:
        try:
            interval_days = int(interval_days)
        except Exception:
            interval_days = 7
    else:
        # 兼容旧配置（分钟）
        interval_min = int(os.getenv("SCHEDULE_INTERVAL_MINUTES", "60"))
        # 转换为天数（至少 1 天）
        interval_days = max(1, interval_min // (60 * 24))

    scheduler = BlockingScheduler()

    # 立即执行一次（可通过 env 关闭）
    if os.getenv("SCHEDULE_RUN_ON_START", "true").lower() == "true":
        items = job_once()
        if items is not None and len(items) == 0:
            logging.info("本次检查未发现新公告")

    scheduler.add_job(job_once, "interval", days=interval_days, next_run_time=None)

    logging.info("调度器已启动，间隔 %s 天。按 Ctrl+C 停止。", interval_days)
    try:
        scheduler.start()
    except (KeyboardInterrupt, SystemExit):
        logging.info("调度器已停止")


if __name__ == "__main__":
    main()
