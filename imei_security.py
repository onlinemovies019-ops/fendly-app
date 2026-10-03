import hashlib
import hmac
import os
import re

from fastapi import HTTPException, status

IMEI_PATTERN = re.compile(r"[0-9]{15}\Z")


def validate_imei(imei_number: str) -> str:
    if not IMEI_PATTERN.fullmatch(imei_number):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="IMEI must contain exactly 15 digits",
        )
    return imei_number


def _secret_key() -> bytes:
    secret = os.getenv("APP_SECRET_KEY", "")
    if len(secret) < 32:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="IMEI verification is temporarily unavailable",
        )
    return secret.encode("utf-8")


def imei_digest(imei_number: str) -> str:
    key = _secret_key()
    return hmac.new(key, b"safetrade-imei-v1:" + imei_number.encode("ascii"), hashlib.sha256).hexdigest()


def client_quota_digest(client_ip: str) -> str:
    key = _secret_key()
    return hmac.new(key, b"safetrade-client-v1:" + client_ip.encode("utf-8"), hashlib.sha256).hexdigest()
