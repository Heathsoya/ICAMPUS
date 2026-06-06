import logging
from datetime import datetime
from pathlib import Path

import pandas as pd

from config import settings

logger = logging.getLogger(__name__)


def _get_export_dir() -> Path:
    export_base = Path(settings.EXPORT["csv_dir"])
    archive_dir = export_base / datetime.now().strftime(settings.EXPORT["date_format"])
    archive_dir.mkdir(parents=True, exist_ok=True)
    return archive_dir


def export_to_csv(items: list[dict], filename: str = None) -> Path:
    if not items:
        raise ValueError("没有可导出的数据。")

    # 确保 keywords 字段为字符串（空格分隔），便于后续导入与检索
    normalized = []
    for it in items:
        ni = dict(it)
        kws = ni.get('keywords') or []
        if isinstance(kws, list):
            ni['keywords'] = ' '.join([str(x).strip() for x in kws if str(x).strip()])
        else:
            ni['keywords'] = str(kws)
        normalized.append(ni)

    filename = filename or f"knowledge_{datetime.now().strftime(settings.EXPORT['date_format'] + '_%H%M%S')}.csv"
    export_dir = _get_export_dir()
    filepath = export_dir / filename

    df = pd.DataFrame(normalized)
    columns = settings.OUTPUT_SCHEMA
    df = df.reindex(columns=columns)
    df.to_csv(filepath, index=False, encoding=settings.EXPORT["csv_encoding"])

    logger.info("CSV 导出成功：%s", filepath)
    return filepath



def export_db_csv(items: list[dict], filename: str = None) -> Path:
    """导出为直接可被 knowledge_base 表导入的 CSV。

    导出列顺：question,answer,category,keywords,source,post_id
    - question: 截断到 500 字符
    - answer: 不能为空，长度不限
    - category: 可空，已映射到数据库预设分类
    - keywords: 以空格分隔的字符串（3-5 个关键词优先），可空
    - source: 纯来源描述（例如“爬虫导入”），不要和 post_id 合并
    - post_id: 原始公告 ID，用于追溯来源

    会默认对相同 `question` 做去重（保留第一条）。
    """
    if not items:
        raise ValueError("没有可导出的数据。")

    rows = []
    for it in items:
        q = (it.get("question") or "").strip()
        a = (it.get("answer") or "").strip()
        if not q or not a:
            # 跳过不完整记录
            continue
        # 截断 question 到 500 字符
        q = q[:500]

        category = it.get("category") or ""

        kws = it.get("keywords") or []
        if isinstance(kws, list):
            kws_str = " ".join([str(x).strip() for x in kws if str(x).strip()])
        else:
            kws_str = str(kws)

        # source 字段只包含纯来源描述，不要和 post_id 合并
        source = it.get("source") or settings.SOURCE_DEPARTMENT_DEFAULT
        post_id = it.get("post_id") or ""

        rows.append({
            "question": q,
            "answer": a,
            "category": category,
            "keywords": kws_str,
            "source": source,
            "post_id": post_id,
        })

    # 使用 pandas 去重（基于 question）并保持第一次出现
    df = pd.DataFrame(rows)
    if df.empty:
        raise ValueError("没有有效的导出行。")

    df = df.drop_duplicates(subset=["question"], keep="first")

    filename = filename or f"knowledge_db_{datetime.now().strftime(settings.EXPORT['date_format'] + '_%H%M%S')}.csv"
    export_dir = _get_export_dir()
    filepath = export_dir / filename

    df.to_csv(filepath, index=False, encoding=settings.EXPORT["csv_encoding"])

    logger.info("DB 兼容 CSV 导出成功：%s", filepath)
    return filepath
