from config import settings


def _count_keywords(keywords) -> int:
    if isinstance(keywords, list):
        return len(keywords)
    if isinstance(keywords, str):
        # 允许空格或逗号分隔
        parts = [p for p in (keywords.replace(',', ' ').split()) if p.strip()]
        return len(parts)
    return 0


def validate_processed_item(item: dict) -> tuple[bool, str]:
    required_fields = ["question", "answer", "category", "keywords", "original_title", "original_publish_date", "original_url"]
    for field in required_fields:
        if not item.get(field):
            return False, f"缺少字段：{field}"

    if item.get("category") not in settings.CATEGORIES:
        return False, f"分类不在预设范围内：{item.get('category')}"

    keywords = item.get("keywords")
    kw_count = _count_keywords(keywords)
    if not (3 <= kw_count <= 5):
        return False, "关键词数量不符合要求（3到5个）"

    answer = item.get("answer", "")
    max_chars = getattr(settings, 'ANSWER_MAX_CHARS', 200)
    if len(answer) > max_chars:
        return False, f"答案长度超过 {max_chars} 字"

    # 禁止只引用附件或链接作为答案
    lower = answer.lower()
    if any(k in lower for k in ["附件", "见附件", "详见", "见链接", "链接"]):
        if len(answer) < 60:
            return False, "答案不能只引用附件或链接，请直接从正文提取可用摘要"

    return True, ""
