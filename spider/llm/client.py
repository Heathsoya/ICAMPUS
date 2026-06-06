import json
import logging
import os

import requests

from config import settings

logger = logging.getLogger(__name__)

try:
    import openai
except ImportError:
    openai = None


def call_llm(prompt: str):
    provider = settings.LLM_PROVIDER.lower()
    if provider == "openai":
        return _call_openai(prompt)
    if provider == "deepseekapi":
        return _call_deepseekapi(prompt)
    raise RuntimeError(f"不支持的 LLM 提供商: {provider}")


def _call_openai(prompt: str):
    if not openai:
        raise RuntimeError("openai 库未安装，请安装 openai 后再运行。")
    if not settings.LLM_API_KEY:
        raise RuntimeError("未配置 LLM_API_KEY，无法调用 OpenAI。")
    openai.api_key = settings.LLM_API_KEY
    try:
        response = openai.ChatCompletion.create(
            model=settings.LLM_MODEL,
            messages=[{"role": "user", "content": prompt}],
            temperature=0.2,
            timeout=settings.LLM_TIMEOUT_SECONDS,
        )
        return response["choices"][0]["message"]["content"]
    except Exception as exc:
        logger.exception("OpenAI 调用失败：%s", exc)
        raise


def _call_deepseekapi(prompt: str):
    if not settings.DEEPSEEKAPI_URL:
        raise RuntimeError("未配置 DEEPSEEKAPI_URL，无法调用 DeepseekAPI。")
    headers = {
        "Content-Type": "application/json",
    }
    if settings.DEEPSEEKAPI_KEY:
        headers["Authorization"] = f"Bearer {settings.DEEPSEEKAPI_KEY}"

    payload = {
        "model": settings.LLM_MODEL,
        "messages": [
            {"role": "system", "content": "字段规则、数据库JSON格式要求"},
            {"role": "user", "content": prompt},
        ],
        "temperature": 0,
        "response_format": {"type": "json_object"},
    }

    try:
        response = requests.post(
            settings.DEEPSEEKAPI_URL,
            headers=headers,
            json=payload,
            timeout=settings.LLM_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        data = response.json()
        if settings.DEEPSEEKAPI_RESPONSE_FIELD in data:
            return data[settings.DEEPSEEKAPI_RESPONSE_FIELD]
        if isinstance(data, dict) and "choices" in data:
            choice = data["choices"][0]
            return choice.get("message", {}).get("content", "") or choice.get("text", "")
        return json.dumps(data, ensure_ascii=False)
    except Exception as exc:
        logger.exception("DeepseekAPI 调用失败：%s", exc)
        raise
