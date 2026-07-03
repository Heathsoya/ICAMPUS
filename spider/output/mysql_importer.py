import logging

import pymysql

from config import settings

logger = logging.getLogger(__name__)


def import_records(items: list[dict]) -> int:
    rows = []
    for item in items:
        question = (item.get("question") or "").strip()[:500]
        answer = (item.get("answer") or "").strip()
        if not question or not answer:
            continue

        keywords = item.get("keywords") or []
        if isinstance(keywords, list):
            keywords = " ".join(str(value).strip() for value in keywords if str(value).strip())

        rows.append((
            question,
            answer,
            (item.get("category") or "").strip(),
            str(keywords),
            (item.get("source") or settings.SOURCE_DEPARTMENT_DEFAULT).strip(),
        ))

    if not rows:
        return 0

    sql = """
        INSERT INTO knowledge_base (question, answer, category, keywords, source)
        VALUES (%s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            answer = VALUES(answer),
            category = VALUES(category),
            keywords = VALUES(keywords),
            source = VALUES(source),
            updated_at = CURRENT_TIMESTAMP
    """

    connection = pymysql.connect(
        host=settings.MYSQL_HOST,
        port=settings.MYSQL_PORT,
        user=settings.MYSQL_USERNAME,
        password=settings.MYSQL_PASSWORD,
        database=settings.MYSQL_DATABASE,
        charset="utf8mb4",
        autocommit=False,
    )
    try:
        with connection.cursor() as cursor:
            cursor.executemany(sql, rows)
        connection.commit()
    except Exception:
        connection.rollback()
        logger.exception("写入 MySQL knowledge_base 失败")
        raise
    finally:
        connection.close()

    return len(rows)
