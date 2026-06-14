import logging
import os
import smtplib
import traceback
from email.message import EmailMessage

import requests

from config import settings

logger = logging.getLogger(__name__)


def _safe_get(key: str, default=None):
    return getattr(settings, "ALERTING", {}).get(key, default)


def is_alert_enabled() -> bool:
    return _safe_get("enabled", False)


def _format_alert_text(subject: str, message: str) -> str:
    return f"[{subject}]\n{message}" if subject else message


def send_alert(subject: str, message: str, level: str = "ERROR") -> None:
    if not is_alert_enabled():
        return

    body = _format_alert_text(subject, message)
    provider = _safe_get("provider", "webhook").lower()

    try:
        if provider == "webhook":
            _send_webhook_alert(body)
        elif provider == "smtp":
            _send_email_alert(subject, body)
        else:
            logger.warning("未知报警提供商: %s", provider)
    except Exception as exc:
        logger.exception("发送报警失败: %s", exc)


def send_exception_alert(subject: str, exc: Exception, context: str = "") -> None:
    if not is_alert_enabled():
        return

    trace = traceback.format_exc()
    message = f"{context}\n异常类型: {type(exc).__name__}\n异常信息: {exc}\n\n堆栈:\n{trace}"
    send_alert(subject, message)


def _send_webhook_alert(body: str) -> None:
    url = _safe_get("webhook_url", "")
    if not url:
        raise RuntimeError("未配置 ALERTING.webhook_url")

    payload = {"text": body}
    timeout = _safe_get("webhook_timeout_seconds", 10)
    response = requests.post(url, json=payload, timeout=timeout)
    response.raise_for_status()
    logger.info("报警 webhook 已发送: %s", url)


def _send_email_alert(subject: str, body: str) -> None:
    host = _safe_get("smtp_host", "")
    port = int(_safe_get("smtp_port", 587))
    username = _safe_get("smtp_username", "")
    password = _safe_get("smtp_password", "")
    sender = _safe_get("sender", "")
    receivers = _safe_get("receivers", "")

    if not host or not sender or not receivers:
        raise RuntimeError("SMTP 报警缺少必要配置: smtp_host/sender/receivers")

    recipients = [address.strip() for address in str(receivers).split(",") if address.strip()]
    if not recipients:
        raise RuntimeError("SMTP 报警未配置接收者邮箱")

    message = EmailMessage()
    message["Subject"] = subject
    message["From"] = sender
    message["To"] = ", ".join(recipients)
    message.set_content(body)

    with smtplib.SMTP(host, port, timeout=10) as smtp:
        smtp.starttls()
        if username and password:
            smtp.login(username, password)
        smtp.send_message(message)
    logger.info("SMTP 报警已发送给: %s", recipients)
