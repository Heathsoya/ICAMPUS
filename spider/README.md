项目部署与定时爬取说明
=====================

快速说明
---------
这是一个用于爬取高校公告并生成知识问答对的爬虫项目。仓库提供三个入口脚本：

- `run.py`：完整流程（爬取 + 处理 + 导出）
- `run_crawl_only.py`：仅爬取并保存原始数据
- `run_process_only.py`：处理已存在的原始数据并导出 CSV
- `run_scheduler.py`：以定时任务方式周期执行爬取（基于 `apscheduler`）

环境与依赖
-----------
1. 创建 Python 虚拟环境并激活：

```bash
python -m venv venv
source venv/bin/activate   # Linux / macOS
venv\Scripts\Activate.ps1 # Windows PowerShell
```

2. 安装依赖：

```bash
pip install -r requirements.txt
```

配置
----
复制 `.env.example` 为 `.env` 并填写必需的环境变量（例如 `LLM_API_KEY`、报警配置等）：

```bash
cp .env.example .env
# 然后编辑 .env
```

定时爬取（服务器部署）
------------------
项目内已提供 `run_scheduler.py`，默认每 7 天运行一次以检查网站更新（只抓取新增公告）。您可以通过环境变量调整：

- `SCHEDULE_INTERVAL_DAYS`：间隔天数，默认 `7`（优先使用）
- `SCHEDULE_INTERVAL_MINUTES`：兼容旧配置，按分钟间隔（最低精度）
- `SCHEDULE_RUN_ON_START`：是否在启动时立即执行一次，默认 `true`

示例 systemd 单元（在 Linux 服务器上）：

```
[Unit]
Description=ICAMPUS Spider Scheduler
After=network.target

[Service]
User=spideruser
WorkingDirectory=/path/to/spider
EnvironmentFile=/path/to/spider/.env
ExecStart=/path/to/spider/venv/bin/python /path/to/spider/run_scheduler.py
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

将上面内容保存为 `/etc/systemd/system/icampus-spider.service`，然后运行：

```bash
sudo systemctl daemon-reload
sudo systemctl enable icampus-spider
sudo systemctl start icampus-spider
sudo journalctl -u icampus-spider -f
```

或使用 `cron`（每小时运行示例）:

```cron
0 * * * * /path/to/spider/venv/bin/python /path/to/spider/run_scheduler.py >> /path/to/spider/logs/cron_scheduler.log 2>&1
```

注意事项
--------
- 确保 `data/`、`logs/` 等目录对运行用户可写。
- `.env` 中不要提交敏感信息到版本控制。
- 如果使用 OpenAI，需要配置 `LLM_API_KEY`。
