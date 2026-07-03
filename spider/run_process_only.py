import glob
import json
import logging
from pathlib import Path

from config.logging_config import setup_logging
from config import settings
from llm.processor import process_raw_item
from output.exporter import export_to_csv, export_db_csv
from output.mysql_importer import import_records
from pipeline.cleaner import clean_text
from pipeline.filter import should_skip
from pipeline.validator import validate_processed_item
from core.storage import processed_item_exists


def load_raw_items():
    raw_dir = Path(settings.RAW_DATA_DIR)
    files = sorted(raw_dir.glob("*.json"))
    for filepath in files:
        with open(filepath, "r", encoding="utf-8") as handle:
            yield json.load(handle)


def main():
    setup_logging()
    records = []
    skipped = 0

    for raw_item in load_raw_items():
        if processed_item_exists(raw_item):
            logging.info("跳过已处理公告: %s", raw_item.get("url"))
            skipped += 1
            continue

        skip, reason = should_skip(raw_item)
        if skip:
            logging.info("跳过: %s => %s", raw_item.get("url"), reason)
            skipped += 1
            continue

        raw_item["content"] = clean_text(raw_item.get("content", ""))
        processed_items = process_raw_item(raw_item)
        if not processed_items:
            logging.info("未生成有效问答对: %s", raw_item.get("url"))
            continue

        for processed in processed_items:
            valid, message = validate_processed_item(processed)
            if not valid:
                logging.warning("校验失败: %s | %s", raw_item.get("url"), message)
                continue
            records.append(processed)

    if records:
        # 生成通用 CSV
        csv_path = export_to_csv(records)
        print(f"已生成 CSV：{csv_path}")
        # 生成数据库导入兼容的 CSV（question,answer,category,keywords,source）
        db_csv_path = export_db_csv(records, filename="db_" + Path(csv_path).name)
        print(f"已生成 DB 兼容 CSV：{db_csv_path}")
        if settings.MYSQL_IMPORT_ENABLED:
            imported = import_records(records)
            print(f"已写入 MySQL knowledge_base：{imported} 条")
    else:
        print("未生成任何有效问答数据。")

    print(f"处理完成：已处理 {len(records)} 条，跳过 {skipped} 条。")


if __name__ == "__main__":
    main()
