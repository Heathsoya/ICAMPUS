import json
from datetime import datetime
from pathlib import Path

from config import settings


def ensure_data_dirs():
    for path in [
        settings.DATA_DIR,
        settings.RAW_DATA_DIR,
        settings.PROCESSED_DATA_DIR,
        settings.OUTPUT_DIR,
        settings.LOG_DIR,
    ]:
        Path(path).mkdir(parents=True, exist_ok=True)


def save_raw_item(item):
    site_id = item.get("site_id", "unknown")
    item_id = item.get("post_id", item.get("id", "unknown"))
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = settings.RAW_ITEM_FILENAME_TEMPLATE.format(
        site_id=site_id,
        item_id=item_id,
        timestamp=timestamp,
    )
    filepath = Path(settings.RAW_DATA_DIR) / filename

    with open(filepath, "w", encoding="utf-8") as handle:
        json.dump(item, handle, ensure_ascii=False, indent=2)

    return filepath


def raw_item_exists(item):
    post_id = item.get("post_id") or item.get("id")
    if not post_id:
        return False

    raw_dir = Path(settings.RAW_DATA_DIR)
    if not raw_dir.exists():
        return False

    for filepath in raw_dir.glob("*.json"):
        if post_id in filepath.name:
            return True
    return False


def save_processed_item(raw_item, processed_records: list):
    """Save processed records for a raw item into `data/processed` for review.

    Filename: {site_id}_{post_id}_{timestamp}_processed.json
    """
    site_id = raw_item.get("site_id", "unknown")
    post_id = raw_item.get("post_id", raw_item.get("id", "unknown"))
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{site_id}_{post_id}_{timestamp}_processed.json"
    filepath = Path(settings.PROCESSED_DATA_DIR) / filename
    with open(filepath, "w", encoding="utf-8") as handle:
        json.dump({
            "raw": raw_item,
            "processed": processed_records,
        }, handle, ensure_ascii=False, indent=2)
    return filepath


def load_raw_item(filepath):
    with open(filepath, "r", encoding="utf-8") as handle:
        return json.load(handle)
