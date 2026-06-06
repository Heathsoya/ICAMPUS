from config import settings


def build_prompt(raw_item: dict) -> str:
    categories = settings.CATEGORIES
    categories_text = ", ".join([f'"{cat}"' for cat in categories])
    return f"""
你是一个公告内容转问答对的助手。
请根据下面原始公告内容，提取结构化结果。

原始标题：{raw_item['list_title']}
发布时间：{raw_item['publish_date']}
来源部门：{raw_item.get('category_tag', '')}
原文链接：{raw_item['url']}
正文内容：\n{raw_item['content']}\n
请严格按照 JSON 格式返回，字段如下：
- qa_pairs: 至少1条、最多3条对象数组，每个对象包含 question 和 answer
- category: 从以下九个分类中选择一个最匹配的类别：{categories_text}
- keywords: 3到5个关键词，数组形式
- skip_flag: 如果这条公告不适合转换为问答，请返回 true，否则返回 false
- skip_reason: 如果 skip_flag 为 true，请给出简短原因，否则返回空字符串

每个 question 应该是学生最可能提出的问题，answer 为该问题的核心摘要。
请用“简洁自然”的学生提问方式生成 question，避免逐字复述公告标题、避免学术化或冗长的表达。
一个 question 只能包含一个问题点，不要把多个问题揉在一起。
请不要引导用户去看附件或外部链接，必须从正文提取可直接使用的摘要。
每个 answer 应控制在 {settings.ANSWER_MAX_CHARS} 字符以内。

注意：
1. 只输出 JSON，不要增加任何解释或多余文字。
2. 如果无法生成问答对，请将 skip_flag 置为 true，并将 qa_pairs 置为空数组。
""".strip()
