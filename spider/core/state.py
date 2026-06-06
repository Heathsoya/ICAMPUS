import sqlite3
from datetime import datetime
from pathlib import Path

from config import settings


def _get_connection():
    db_path = Path(settings.STATE_DB_PATH)
    db_path.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(str(db_path), timeout=settings.STATE_DB_TIMEOUT_SECONDS)
    return conn


def ensure_state_db():
    with _get_connection() as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS crawled_posts (
                post_id TEXT PRIMARY KEY,
                url TEXT,
                category_tag TEXT,
                source_department TEXT,
                title TEXT,
                fetched_at TEXT,
                raw_file TEXT
            )
            """
        )
        conn.commit()


def is_post_crawled(post_id: str) -> bool:
    if not post_id:
        return False
    with _get_connection() as conn:
        cursor = conn.execute(
            "SELECT 1 FROM crawled_posts WHERE post_id = ? LIMIT 1",
            (post_id,),
        )
        return cursor.fetchone() is not None


def mark_post_crawled(post_id: str, url: str, category_tag: str, source_department: str, title: str, raw_file: str):
    if not post_id:
        return
    fetched_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with _get_connection() as conn:
        conn.execute(
            "INSERT OR REPLACE INTO crawled_posts (post_id, url, category_tag, source_department, title, fetched_at, raw_file) VALUES (?, ?, ?, ?, ?, ?, ?)",
            (post_id, url, category_tag, source_department, title, fetched_at, raw_file),
        )
        conn.commit()
