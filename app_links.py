import os
from urllib.parse import urlsplit


DEFAULT_FENDLY_APP_URL = "https://fendly.app"


def fendly_app_url() -> str:
    app_url = os.getenv("FENDLY_APP_URL", DEFAULT_FENDLY_APP_URL).strip().rstrip("/")
    parsed = urlsplit(app_url)
    if (
        parsed.scheme != "https"
        or not parsed.hostname
        or parsed.username
        or parsed.password
        or parsed.query
        or parsed.fragment
    ):
        raise RuntimeError("FENDLY_APP_URL must be a valid HTTPS app URL")
    return app_url


def fendly_report_url(report_id: str) -> str:
    if not report_id or "/" in report_id or "?" in report_id or "#" in report_id:
        raise ValueError("Report ID is invalid for a Fendly report link")
    return f"{fendly_app_url()}/item/{report_id}"
