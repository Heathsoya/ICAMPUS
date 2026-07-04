#!/usr/bin/env bash
set -euo pipefail

: "${QWEN_API_KEY:?QWEN_API_KEY is required}"

mkdir -p data logs
exec 9>data/crawler.lock
if ! flock -n 9; then
  echo "Crawler is already running."
  exit 75
fi

export LLM_PROVIDER=openai
export LLM_API_KEY="${QWEN_API_KEY}"
export LLM_API_URL="https://dashscope.aliyuncs.com/compatible-mode/v1"
export LLM_MODEL="qwen-plus"
export SPIDER_DB_IMPORT_ENABLED=true
export CRAWL_PAGES_PER_CATEGORY="${CRAWL_PAGES_PER_CATEGORY:-3}"
export CRAWL_MAX_NEW_PER_CATEGORY="${CRAWL_MAX_NEW_PER_CATEGORY:-5}"
export LLM_MAX_QA_PER_ANNOUNCEMENT="${LLM_MAX_QA_PER_ANNOUNCEMENT:-3}"
export CRAWL_MAX_QA_PER_RUN="${CRAWL_MAX_QA_PER_RUN:-30}"
export MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
export MYSQL_PORT="${MYSQL_PORT:-3306}"
export MYSQL_DATABASE="${MYSQL_DATABASE:-icampus}"

exec .venv/bin/python run.py
