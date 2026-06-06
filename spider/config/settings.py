from pathlib import Path
import os

# 基础路径
BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"
RAW_DATA_DIR = DATA_DIR / "raw"
PROCESSED_DATA_DIR = DATA_DIR / "processed"
OUTPUT_DIR = BASE_DIR / "output"
DATA_OUTPUT_DIR = DATA_DIR / "output"
LOG_DIR = BASE_DIR / "logs"
STATE_DB_PATH = DATA_DIR / "state.db"
STATE_DB_TIMEOUT_SECONDS = int(os.getenv("STATE_DB_TIMEOUT_SECONDS", "30"))
ENABLE_STATE_DB = os.getenv("ENABLE_STATE_DB", "true").lower() == "true"

# 爬取站点配置
CRAWL_SITES = [
    {
        "site_id": "scut_jw",
        "name": "华工教务通知公告",
        "list_api_url": "https://jw.scut.edu.cn/zhinan/cms/article/v2/findInformNotice.do",
        "detail_url_template": "https://jw.scut.edu.cn/zhinan/cms/article/view.do?type=posts&id={id}",
        "category_default": "0",
        "tags": [
            {"id": 1, "name": "选课"},
            {"id": 2, "name": "考试"},
            {"id": 3, "name": "实践"},
            {"id": 4, "name": "交流"},
            {"id": 5, "name": "教师"},
            {"id": 6, "name": "信息"},
        ],
        "page_size": 15,
        "max_pages": 5,
        "fetch_interval_hours": 24,
    },
]

# 默认爬取目标：tag编号、分类名称、页数、每页条数
CRAWL_TARGETS = [
    (1, "选课", 1, 15),
    (2, "考试", 1, 15),
    (3, "实践", 1, 15),
    (4, "交流", 1, 15),
    (5, "教师", 1, 15),
    (6, "信息", 1, 15),
]

# HTTP 请求配置
HTTP_REQUEST = {
    "timeout": 15,
    "retries": 3,
    "delay_seconds": 1,
    # 指数退避基础因子（秒），最终等待 = min(max_backoff_seconds, backoff_factor * 2**attempt) + jitter
    "backoff_factor": 0.5,
    "max_backoff_seconds": 30,
    "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36",
}

# 过滤规则
SKIP_TITLE_KEYWORDS = ["招标", "中标", "竞价"]
MIN_CONTENT_LENGTH = 100

FILTER_CONFIG = {
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
    "title_rules": {
        "must_contain": "关于",
        "should_contain": ["通知", "公示", "安排", "公布", "报名"],
        "min_length": 5,
    },
    "meta_prefixes": ["发布时间：", "附件：", "附件", "【转载】"],
    "min_content_length": 50,
}

# 预设分类（与数据库预设分类一致）
CATEGORIES = [
    "住宿生活",
    "校园服务",
    "图书馆",
    "教务教学",
    "餐饮服务",
    "财务缴费",
    "就业毕业",
    "校园活动",
    "综合咨询",
]

CATEGORY_MAP = {
    "考试安排": "教务教学",
    "教务公告": "教务教学",
    "课程调整": "教务教学",
    "招生信息": "综合咨询",
    "政策解读": "综合咨询",
    "师生服务": "校园服务",
    "安全与后勤": "校园服务",
    "校园通知": "综合咨询",
    "学生活动": "校园活动",
}

SOURCE_DEPARTMENT_DEFAULT = "爬虫导入"
DEPARTMENT_KEYWORDS = [
    "教务处", "学生处", "学院", "系", "办公室", "中心", "院", "教研室", "处",
]

# LLM 配置
LLM_PROVIDER = os.getenv("LLM_PROVIDER", "openai")
LLM_API_KEY = os.getenv("LLM_API_KEY")
LLM_API_URL = os.getenv("LLM_API_URL", "https://api.openai.com/v1")
LLM_MODEL = os.getenv("LLM_MODEL", "gpt-4o-mini")
LLM_TIMEOUT_SECONDS = int(os.getenv("LLM_TIMEOUT_SECONDS", "30"))
LLM_BATCH_SIZE = int(os.getenv("LLM_BATCH_SIZE", "5"))
LLM_MAX_RETRIES = int(os.getenv("LLM_MAX_RETRIES", "2"))
# 可配置的答案最大字符数（用于 prompt 要求与本地截断），可在 .env 中设置
ANSWER_MAX_CHARS = int(os.getenv("ANSWER_MAX_CHARS", "200"))

# DeepseekAPI 配置
DEEPSEEKAPI_URL = os.getenv("DEEPSEEKAPI_URL", "")
DEEPSEEKAPI_KEY = os.getenv("DEEPSEEKAPI_KEY", "")
DEEPSEEKAPI_MODEL_FIELD = os.getenv("DEEPSEEKAPI_MODEL_FIELD", "model")
DEEPSEEKAPI_PROMPT_FIELD = os.getenv("DEEPSEEKAPI_PROMPT_FIELD", "prompt")
DEEPSEEKAPI_RESPONSE_FIELD = os.getenv("DEEPSEEKAPI_RESPONSE_FIELD", "text")

# 输出 CSV 字段顺序
OUTPUT_SCHEMA = [
    "question",
    "answer",
    "category",
    "keywords",
    "original_title",
    "original_publish_date",
    "source_department",
    "original_url",
    "post_id",
    "confidence_score",
    "created_at",
]

# 导出配置
EXPORT = {
    "date_format": "%Y%m%d",
    "csv_encoding": "utf-8-sig",
    "csv_dir": DATA_OUTPUT_DIR,
}

# 日志配置
LOGGING = {
    "level": "INFO",
    "file_name": LOG_DIR / "spider.log",
    "max_bytes": 5 * 1024 * 1024,
    "backup_count": 5,
}

# 调试模式
DEBUG = os.getenv("SPIDER_DEBUG", "false").lower() == "true"

# 只读配置：是否跳过重复 URL
ENABLE_DEDUPLICATION = True

# 原始数据文件名模板
RAW_ITEM_FILENAME_TEMPLATE = "{site_id}_{item_id}_{timestamp}.json"

# 模块默认运行参数
DEFAULT_RUN = {
    "crawl_only": False,
    "process_only": False,
    "limit": None,
}