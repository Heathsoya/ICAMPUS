from bs4 import BeautifulSoup

from config import settings


def extract_clean_content(html):
    """从详情页 HTML 中提取标题和正文内容。"""
    soup = BeautifulSoup(html, "lxml")

    for tag in soup(["script", "style", "nav", "header", "footer", "iframe"]):
        tag.decompose()

    text = soup.get_text(separator="\n", strip=True)
    lines = [line.strip() for line in text.split("\n") if line.strip()]

    noise_keywords = settings.FILTER_CONFIG.get("noise_keywords", [])
    clean_lines = [
        line for line in lines
        if len(line) >= 3 and not any(keyword in line for keyword in noise_keywords)
    ]

    title, title_index = _extract_title(clean_lines)
    if title_index >= 0:
        content_lines = clean_lines[:title_index] + clean_lines[title_index + 1:]
    else:
        content_lines = clean_lines

    final_lines = []
    for line in content_lines:
        if any(line.startswith(prefix) for prefix in settings.FILTER_CONFIG.get("meta_prefixes", [])):
            continue
        final_lines.append(line)

    content = "\n".join(final_lines)
    if len(content) < settings.FILTER_CONFIG.get("min_content_length", 50):
        return title, ""

    return title, content


def extract_source_department(text: str) -> str:
    if not text:
        return ""
    lowered = text
    for keyword in settings.DEPARTMENT_KEYWORDS:
        if keyword in lowered:
            return keyword
    return ""


def _extract_title(lines):
    title = "无标题"
    title_index = -1
    title_rules = settings.FILTER_CONFIG.get("title_rules", {})

    for index, line in enumerate(lines):
        if title_rules.get("must_contain") and title_rules["must_contain"] not in line:
            continue
        if title_rules.get("should_contain") and not any(word in line for word in title_rules["should_contain"]):
            continue
        if len(line) < title_rules.get("min_length", 5):
            continue

        if title == "无标题" or len(line) > len(title):
            title = line
            title_index = index

    return title, title_index
