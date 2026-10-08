import re


EMAIL_PATTERN = re.compile(r"\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b", re.IGNORECASE)
PHONE_PATTERN = re.compile(r"(?<!\w)\+?[\d().\s-]{8,}\d(?!\w)")


def sanitize_public_title(title: str | None, max_length: int = 120) -> str:
    sanitized = EMAIL_PATTERN.sub("[contact removed]", title or "")
    sanitized = PHONE_PATTERN.sub("[number removed]", sanitized)
    sanitized = re.sub(r"\b\d{15}\b", "[identifier removed]", sanitized)
    sanitized = re.sub(r"[\x00-\x1f<>]", " ", sanitized)
    return " ".join(sanitized.split())[:max_length].strip()
