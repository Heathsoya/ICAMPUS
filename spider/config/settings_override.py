import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"
RAW_DATA_DIR = DATA_DIR / "raw"
PROCESSED_DATA_DIR = DATA_DIR / "processed"
OUTPUT_DIR = BASE_DIR / "output"
DATA_OUTPUT_DIR = DATA_DIR / "output"
LOG_DIR = BASE_DIR / "logs"

# DeepseekAPI 配置
DEEPSEEKAPI_URL = os.getenv("DEEPSEEKAPI_URL", "")
DEEPSEEKAPI_KEY = os.getenv("DEEPSEEKAPI_KEY", "")
DEEPSEEKAPI_MODEL_FIELD = os.getenv("DEEPSEEKAPI_MODEL_FIELD", "model")
DEEPSEEKAPI_PROMPT_FIELD = os.getenv("DEEPSEEKAPI_PROMPT_FIELD", "prompt")
DEEPSEEKAPI_RESPONSE_FIELD = os.getenv("DEEPSEEKAPI_RESPONSE_FIELD", "text")

# 输出 CSV 字段顺序
OUTPUT_SCHEMA = [
    "question",
    "answer",
    "category",
    "keywords",
    "original_title",
    "original_publish_date",
    "source_department",
    "original_url",
    "post_id",
    "confidence_score",
    "created_at",
]

# 导出配置
EXPORT = {
    "date_format": "%Y%m%d",
    "csv_encoding": "utf-8-sig",
    "csv_dir": DATA_OUTPUT_DIR,
}

# 日志配置
LOGGING = {
    "level": "INFO",
    "file_name": LOG_DIR / "spider.log",
    "max_bytes": 5 * 1024 * 1024,
    "backup_count": 5,
}

# 调试模式
DEBUG = os.getenv("SPIDER_DEBUG", "false").lower() == "true"

# 只读配置：是否跳过重复 URL
ENABLE_DEDUPLICATION = True

# 原始数据文件名模板
RAW_ITEM_FILENAME_TEMPLATE = "{site_id}_{item_id}_{timestamp}.json"

# 告警配置
ALERTING = {
    "enabled": os.getenv("ALERT_ENABLED", "false").lower() == "true",
    "provider": os.getenv("ALERT_PROVIDER", "webhook"),
    "webhook_url": os.getenv("ALERT_WEBHOOK_URL", ""),
    "webhook_timeout_seconds": int(os.getenv("ALERT_WEBHOOK_TIMEOUT_SECONDS", "10")),
    "smtp_host": os.getenv("ALERT_SMTP_HOST", ""),
    "smtp_port": int(os.getenv("ALERT_SMTP_PORT", "587")),
    "smtp_username": os.getenv("ALERT_SMTP_USERNAME", ""),
    "smtp_password": os.getenv("ALERT_SMTP_PASSWORD", ""),
    "sender": os.getenv("ALERT_SENDER", ""),
    "receivers": os.getenv("ALERT_RECEIVERS", ""),
}

# 模块默认运行参数
DEFAULT_RUN = {
    "crawl_only": False,
    "process_only": False,
    "limit": None,
}
