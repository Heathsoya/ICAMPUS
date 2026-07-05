from config import settings


def should_skip(raw_item: dict) -> tuple[bool, str]:
    title = raw_item.get("list_title", "")
    extract_title = raw_item.get("extracted_title", "")
    text = f"{title} {extract_title}"

    for keyword in settings.SKIP_TITLE_KEYWORDS:
        if keyword in text:
            return True, f"标题包含不适合关键词：{keyword}"

    content = raw_item.get("content", "")
    if len(content) < settings.MIN_CONTENT_LENGTH:
        return True, "正文长度低于最小阈值"

    return False, ""
