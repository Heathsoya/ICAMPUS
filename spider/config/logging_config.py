import logging
from logging.handlers import RotatingFileHandler
from pathlib import Path

from config import settings


def setup_logging():
    """统一初始化根日志器：控制台 + 按文件滚动日志（基于 settings.LOGGING）。"""
    cfg = getattr(settings, "LOGGING", {})
    level = cfg.get("level", "INFO").upper()
    level_val = getattr(logging, level, logging.INFO)

    log_file = cfg.get("file_name")
    max_bytes = cfg.get("max_bytes", 5 * 1024 * 1024)
    backup_count = cfg.get("backup_count", 5)

    root = logging.getLogger()
    # 如果已经配置过 handler，则跳过重复配置
    if root.handlers:
        root.setLevel(level_val)
        return

    root.setLevel(level_val)

    fmt = logging.Formatter("%(asctime)s %(levelname)s [%(name)s] %(message)s")

    # 控制台
    ch = logging.StreamHandler()
    ch.setLevel(level_val)
    ch.setFormatter(fmt)
    root.addHandler(ch)

    # 文件（可选）
    if log_file:
        try:
            log_path = Path(log_file)
            log_path.parent.mkdir(parents=True, exist_ok=True)
            fh = RotatingFileHandler(str(log_path), maxBytes=max_bytes, backupCount=backup_count, encoding="utf-8")
            fh.setLevel(level_val)
            fh.setFormatter(fmt)
            root.addHandler(fh)
        except Exception:
            root.warning("无法创建日志文件 %s，继续使用控制台日志。", log_file)
