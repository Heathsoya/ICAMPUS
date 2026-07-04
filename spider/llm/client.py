import json
import logging

import requests

from alert import send_exception_alert
from config import settings

logger = logging.getLogger(__name__)

def call_llm(prompt: str):
    provider = settings.LLM_PROVIDER.lower()
    if provider == "openai":
        return _call_openai(prompt)
    if provider == "deepseekapi":
        return _call_deepseekapi(prompt)
    raise RuntimeError(f"不支持的 LLM 提供商: {provider}")


def _call_openai(prompt: str):
    if not settings.LLM_API_KEY:
        raise RuntimeError("未配置 LLM_API_KEY，无法调用大模型。")

    url = settings.LLM_API_URL.rstrip("/")
    if not url.endswith("/chat/completions"):
        url += "/chat/completions"

    payload = {
        "model": settings.LLM_MODEL,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0.2,
    }
    try:
        response = requests.post(
            url,
            headers={
                "Authorization": f"Bearer {settings.LLM_API_KEY}",
                "Content-Type": "application/json",
            },
            json=payload,
            timeout=settings.LLM_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]["content"]
    except Exception as exc:
        logger.exception("OpenAI 调用失败：%s", exc)
        send_exception_alert("LLM 调用失败", exc, "OpenAI ChatCompletion 调用失败")
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
        settings.DEEPSEEKAPI_MODEL_FIELD: settings.LLM_MODEL,
        settings.DEEPSEEKAPI_PROMPT_FIELD: prompt,
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
            return data["choices"][0].get("text", "")
        return json.dumps(data, ensure_ascii=False)
    except Exception as exc:
        logger.exception("DeepseekAPI 调用失败：%s", exc)
        send_exception_alert("LLM 调用失败", exc, "DeepseekAPI 调用失败")
        raise
