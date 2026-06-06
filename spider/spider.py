#!/usr/bin/env python3


import requests
from bs4 import BeautifulSoup
import time
import json
from datetime import datetime



# 爬取范围配置
# 格式：(tag编号, 分类名称, 爬取页数, 每页条数)
# tag编号：0=全部, 1=选课, 2=考试, 3=实践, 4=交流, 5=教师, 6=信息
CRAWL_TARGETS = [
    (1, "选课",   1, 10),   # 选课分类，爬1页，每页10条
    (2, "考试",   1, 10),  
    (3, "实践",   1, 10),  
    (4, "交流",   1, 10),  
]

# 请求配置
REQUEST_CONFIG = {
    "headers": {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
        "X-Requested-With": "XMLHttpRequest",
        "Referer": "https://jw.scut.edu.cn/zhinan/cms/toPosts.do",
        "Origin": "https://jw.scut.edu.cn"
    },
    "timeout": 15,           # 请求超时（秒）
    "delay": 0.8,            # 请求间隔（秒）
    "retry_times": 3,        # 失败重试次数
}

# 输出配置
OUTPUT_CONFIG = {
    "format": "txt",         # 输出格式：txt 或 json
    "filename": "raw_announcements_clean",  # 文件名前缀
    "add_timestamp": True,   # 文件名是否加时间戳
}

# 内容过滤配置
FILTER_CONFIG = {
    # 噪音关键词
    "noise_keywords": [
        "处长信箱", "华南理工大学主页", "管理登录", "学生服务", "教师服务",
        "网站快速导航", "首页", "一流专业", "一流课程", "深度学习课堂",
        "课程思政", "百步梯创新学院", "拔尖人才培养", "特色教改",
        "关于本科生院", "部门简介", "分管校领导", "职责分工", "招生办公室",
        "教育技术中心", "学院教学副院长", "学院教务员", "办事指南",
        "学生事务", "学籍类", "学业类", "毕结业类", "实践类", "交流交换类",
        "教师事务", "日常教学", "教学研究", "管理制度", "学生学习",
        "学生手册", "教师教学", "教师发展", "文档下载", "学生表格",
        "教师表格", "管理表格", "教务管理", "课室管理", "教学计划",
        "教材建设", "实践实习", "课程考试", "质量评价", "创新创业",
        "教学数据", "教学成果", "教学建设", "示范中心", "信息公开",
        "成绩更改公示", "学生信息公示", "教学信息公开", "本科教学质量报告",
        "教学奖励", "教学团队", "通讯地址", "邮政编码", "粤ICP备",
        "华工教务公众号", "详情页-本科生院", "学业指导", "常用表格"
    ],

    # 爬取条件：标题识别规则
    "title_rules": {
        "must_contain": "关于",           # 标题必须包含
        "should_contain": ["通知", "公示", "安排", "公布", "报名"],  # 标题至少包含一个
        "min_length": 5,                  # 标题最短长度
    },

    # 公告的数据过滤（正文开头要跳过的行）
    "meta_prefixes": ["发布时间：", "附件：", "附件", "【转载】"],

    # 正文最小长度
    "min_content_length": 50,
}

# API 配置
API_CONFIG = {
    "list_url": "https://jw.scut.edu.cn/zhinan/cms/article/v2/findInformNotice.do",
    "detail_url_template": "https://jw.scut.edu.cn/zhinan/cms/article/view.do?type=posts&id={id}",
    "category_default": "0",   # 默认分类参数
}




def fetch_posts_list(page_num=1, tag=6, page_size=15):
    """
    调用AJAX接口获取通知列表
    """
    data = {
        "category": API_CONFIG["category_default"],
        "tag": str(tag),
        "pageNum": str(page_num),
        "pageSize": str(page_size),
        "keyword": ""
    }

    for attempt in range(REQUEST_CONFIG["retry_times"]):
        try:
            response = requests.post(
                API_CONFIG["list_url"],
                data=data,
                headers=REQUEST_CONFIG["headers"],
                timeout=REQUEST_CONFIG["timeout"]
            )
            result = response.json()
            posts = result.get('list', [])
            total = result.get('total', 0)
            return posts, total
        except Exception as e:
            print(f" 列表接口请求失败 (尝试 {attempt + 1}/{REQUEST_CONFIG['retry_times']}): {e}")
            if attempt < REQUEST_CONFIG["retry_times"] - 1:
                time.sleep(1)
            else:
                return [], 0


def extract_clean_content(html):
    """
    进一步的智能提取：过滤导航噪音，识别标题和正文
    """
    soup = BeautifulSoup(html, 'lxml')

    # 去掉脚本、样式、导航等干扰元素
    for tag in soup(['script', 'style', 'nav', 'header', 'footer', 'iframe']):
        tag.decompose()

    text = soup.get_text(separator='\n', strip=True)
    lines = [line.strip() for line in text.split('\n') if line.strip()]

    # 过滤噪音行
    noise_keywords = FILTER_CONFIG["noise_keywords"]
    clean_lines = []
    for line in lines:
        if any(keyword in line for keyword in noise_keywords):
            continue
        if len(line) < 3:
            continue
        clean_lines.append(line)


    title_rules = FILTER_CONFIG["title_rules"]
    title = "无标题"
    title_idx = -1

    for i, line in enumerate(clean_lines):
        if (title_rules["must_contain"] in line and 
            any(word in line for word in title_rules["should_contain"]) and
            len(line) >= title_rules["min_length"]):
            if len(line) > len(title):
                title = line
                title_idx = i

    # 去掉标题行，剩余为正文
    if title_idx >= 0:
        content_lines = clean_lines[:title_idx] + clean_lines[title_idx+1:]
    else:
        content_lines = clean_lines

    # 过滤
    meta_prefixes = FILTER_CONFIG["meta_prefixes"]
    final_lines = []
    for line in content_lines:
        if any(line.startswith(prefix) for prefix in meta_prefixes):
            continue
        final_lines.append(line)

    content = '\n'.join(final_lines)

    # 检查内容长度
    if len(content) < FILTER_CONFIG["min_content_length"]:
        return title, ""  # 内容太短，认为提取失败

    return title, content


def fetch_detail(post_id):
    """
    获取通知详情页内容
    """
    url = API_CONFIG["detail_url_template"].format(id=post_id)

    for attempt in range(REQUEST_CONFIG["retry_times"]):
        try:
            response = requests.get(
                url,
                headers={"User-Agent": REQUEST_CONFIG["headers"]["User-Agent"]},
                timeout=REQUEST_CONFIG["timeout"]
            )
            response.encoding = response.apparent_encoding
            title, content = extract_clean_content(response.text)
            return {
                'title': title,
                'content': content,
                'url': url
            }
        except Exception as e:
            print(f"  详情页请求失败 (尝试 {attempt + 1}/{REQUEST_CONFIG['retry_times']}): {e}")
            if attempt < REQUEST_CONFIG["retry_times"] - 1:
                time.sleep(1)
            else:
                return None


def crawl_category(tag, tag_name, page_num=1, page_size=10):
    """
    爬取单个分类的公告
    """
    print(f"\n{'='*60}")
    print(f" 正在爬取分类: {tag_name} (tag={tag}, 第{page_num}页, 每页{page_size}条)")
    print(f"{'='*60}")

    posts, total = fetch_posts_list(page_num=page_num, tag=tag, page_size=page_size)

    if not posts:
        print(f"该分类没有数据")
        return []

    print(f"获取到 {len(posts)} 条通知（共 {total} 条）")

    results = []
    for i, post in enumerate(posts, 1):
        post_id = post.get('id')
        post_date = post.get('createTime', '未知')
        list_title = post.get('title', '无标题')

        print(f"  [{i}/{len(posts)}] {post_date} | {list_title[:40]}...", end=" ")

        detail = fetch_detail(post_id)
        if detail and detail['content']:
            results.append({
                'list_title': list_title,
                'extracted_title': detail['title'],
                'date': post_date,
                'content': detail['content'],
                'url': detail['url'],
                'category_tag': tag_name
            })
            print(f"({len(detail['content'])} 字符)")
        else:
            print(f"提取失败")

        time.sleep(REQUEST_CONFIG["delay"])

    return results


def save_to_txt(results, filepath):
    """
    保存为 TXT 格式
    """
    with open(filepath, 'w', encoding='utf-8') as f:
        for item in results:
            f.write(f"===== 分类: {item['category_tag']} =====\n")
            f.write(f"原标题: {item['list_title']}\n")
            f.write(f"提取标题: {item['extracted_title']}\n")
            f.write(f"发布时间: {item['date']}\n")
            f.write(f"链接: {item['url']}\n")
            f.write(f"正文:\n{item['content']}\n")
            f.write(f"\n{'='*60}\n\n")
    print(f" TXT 已保存: {filepath}")


def save_to_json(results, filepath):
    """
    保存为 JSON 格式（便于后续程序处理）
    """
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"JSON 已保存: {filepath}")


def generate_filename():
    """
    生成输出文件名
    """
    base = OUTPUT_CONFIG["filename"]
    if OUTPUT_CONFIG["add_timestamp"]:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        return f"{base}_{timestamp}"
    return base


def main():
    """
    主程序
    """
    print("=" * 60)
    print("iCampus 爬虫启动")
    print(f"开始时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # 统计信息
    total_crawled = 0
    total_success = 0

    all_results = []

    # 遍历所有爬取目标
    for tag, name, pages, page_size in CRAWL_TARGETS:
        for page in range(1, pages + 1):
            results = crawl_category(tag, name, page_num=page, page_size=page_size)
            total_crawled += page_size
            total_success += len(results)
            all_results.extend(results)

    # 保存结果
    if all_results:
        filename_base = generate_filename()

        if OUTPUT_CONFIG["format"] in ["txt", "both"]:
            save_to_txt(all_results, f"{filename_base}.txt")

        if OUTPUT_CONFIG["format"] in ["json", "both"]:
            save_to_json(all_results, f"{filename_base}.json")

        print(f"\n{'='*60}")
        print(f"爬取完成！")
        print(f"   目标条数: {total_crawled}")
        print(f"   成功提取: {total_success}")
        print(f"   成功率: {total_success/total_crawled*100:.1f}%")
        print(f"{'='*60}")
    else:
        print("\n未获取到任何内容")


if __name__ == '__main__':
    main()