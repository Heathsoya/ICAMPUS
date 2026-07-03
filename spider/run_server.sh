#!/usr/bin/env bash
set -euo pipefail

: "${QWEN_API_KEY:?QWEN_API_KEY is required}"

export LLM_PROVIDER=openai
export LLM_API_KEY="${QWEN_API_KEY}"
export LLM_API_URL="https://dashscope.aliyuncs.com/compatible-mode/v1"
export LLM_MODEL="qwen-plus"
export SPIDER_DB_IMPORT_ENABLED=true
export MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
export MYSQL_PORT="${MYSQL_PORT:-3306}"
export MYSQL_DATABASE="${MYSQL_DATABASE:-icampus}"

exec .venv/bin/python run.py
