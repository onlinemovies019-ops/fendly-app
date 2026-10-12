import base64
import hashlib
import logging
import os
import secrets
import time
from datetime import datetime, timezone
from urllib.parse import urlencode, urlsplit

import httpx
from cryptography.fernet import Fernet
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import HTMLResponse
from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from database import get_db
from models import SocialAccount, SocialOAuthState
from routers.admin import require_admin


router = APIRouter(prefix="/api/social", tags=["social accounts"])
logger = logging.getLogger(__name__)
META_SCOPES = (
    "public_profile",
    "pages_show_list",
    "pages_read_engagement",
    "pages_manage_posts",
    "instagram_basic",
    "instagram_content_publish",
)
REQUIRED_PUBLISHING_SCOPES = {
    "pages_manage_posts",
    "instagram_basic",
    "instagram_content_publish",
}
META_GRAPH_VERSION_DEFAULT = "v23.0"


def _secret_box() -> Fernet:
    secret = os.getenv("APP_SECRET_KEY", "")
    if len(secret) < 32:
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "APP_SECRET_KEY must contain at least 32 characters",
        )
    key = base64.urlsafe_b64encode(hashlib.sha256(secret.encode("utf-8")).digest())
    return Fernet(key)


def _graph_url(path: str) -> str:
    version = os.getenv("META_GRAPH_API_VERSION", META_GRAPH_VERSION_DEFAULT).strip()
    if not version.startswith("v") or not version[1:].replace(".", "").isdigit():
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "META_GRAPH_API_VERSION is invalid",
        )
    return f"https://graph.facebook.com/{version}/{path.lstrip('/')}"


def _redirect_uri() -> str:
    configured = os.getenv("META_REDIRECT_URI", "").strip()
    if not configured:
        base_url = os.getenv("PUBLIC_BASE_URL", "").rstrip("/")
        configured = f"{base_url}/api/social/callback/meta"
    parsed = urlsplit(configured)
    if parsed.scheme != "https" or not parsed.netloc:
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Meta callback URL must use HTTPS; configure META_REDIRECT_URI or PUBLIC_BASE_URL",
        )
    return configured


def _required_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            f"{name} is not configured on the backend",
        )
    return value


def _oauth_state(session: Session, uid: str) -> str:
    state = secrets.token_urlsafe(32)
    now = int(time.time())
    session.execute(delete(SocialOAuthState).where(SocialOAuthState.expires_at < now))
    session.add(
        SocialOAuthState(
            state_digest=hashlib.sha256(state.encode("utf-8")).hexdigest(),
            provider="meta",
            created_by=uid,
            expires_at=now + 600,
        )
    )
    session.commit()
    return state


def _exchange_response(response: httpx.Response, operation: str) -> dict[str, object]:
    try:
        payload = response.json()
    except ValueError as error:
        raise RuntimeError(f"Meta returned an invalid response during {operation}") from error
    if not response.is_success:
        message = ""
        if isinstance(payload, dict):
            error = payload.get("error")
            if isinstance(error, dict) and isinstance(error.get("message"), str):
                message = error["message"]
        raise RuntimeError(message or f"Meta rejected {operation} (HTTP {response.status_code})")
    if not isinstance(payload, dict):
        raise RuntimeError(f"Meta returned an invalid response during {operation}")
    return payload


def _consume_state(session: Session, state: str) -> None:
    digest = hashlib.sha256(state.encode("utf-8")).hexdigest()
    record = session.get(SocialOAuthState, digest)
    if (
        record is None
        or record.provider != "meta"
        or record.expires_at < int(time.time())
    ):
        if record is not None:
            session.delete(record)
            session.commit()
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            "Meta authorization state is invalid or expired. Start reconnect again.",
        )
    session.delete(record)
    session.commit()


def _upsert_account(
    session: Session,
    provider: str,
    account_id: str,
    account_name: str,
    page_token: str,
    user_token: str,
) -> None:
    account = session.get(SocialAccount, provider)
    encrypted_page_token = _secret_box().encrypt(page_token.encode("utf-8")).decode("ascii")
    encrypted_user_token = _secret_box().encrypt(user_token.encode("utf-8")).decode("ascii")
    values = {
        "account_id": account_id,
        "account_name": account_name[:160],
        "access_token_encrypted": encrypted_page_token,
        "deletion_access_token_encrypted": encrypted_user_token,
        "updated_at": datetime.now(timezone.utc),
    }
    if account is None:
        session.add(SocialAccount(provider=provider, **values))
    else:
        for key, value in values.items():
            setattr(account, key, value)


def _finish_meta_oauth(session: Session, code: str, state: str) -> set[str]:
    _consume_state(session, state)
    app_id = _required_env("META_APP_ID")
    app_secret = _required_env("META_APP_SECRET")
    redirect_uri = _redirect_uri()
    token_response = httpx.get(
        _graph_url("oauth/access_token"),
        params={
            "client_id": app_id,
            "client_secret": app_secret,
            "redirect_uri": redirect_uri,
            "code": code,
        },
        timeout=20,
    )
    user_token = _exchange_response(token_response, "OAuth token exchange").get("access_token")
    if not isinstance(user_token, str) or not user_token:
        raise RuntimeError("Meta did not return an access token")

    long_lived_response = httpx.get(
        _graph_url("oauth/access_token"),
        params={
            "grant_type": "fb_exchange_token",
            "client_id": app_id,
            "client_secret": app_secret,
            "fb_exchange_token": user_token,
        },
        timeout=20,
    )
    long_lived_token = _exchange_response(
        long_lived_response,
        "long-lived token exchange",
    ).get("access_token")
    if not isinstance(long_lived_token, str) or not long_lived_token:
        raise RuntimeError("Meta did not return a long-lived user access token")
    user_token = long_lived_token

    debug_response = httpx.get(
        _graph_url("debug_token"),
        params={
            "input_token": user_token,
            "access_token": f"{app_id}|{app_secret}",
        },
        timeout=20,
    )
    token_data = _exchange_response(debug_response, "token permission check").get("data")
    granted: set[str] = set()
    if isinstance(token_data, dict) and isinstance(token_data.get("scopes"), list):
        granted = {
            scope for scope in token_data["scopes"]
            if isinstance(scope, str)
        }

    pages_response = httpx.get(
        _graph_url("me/accounts"),
        params={
            "fields": "id,name,access_token,instagram_business_account{id,username}",
            "access_token": user_token,
        },
        timeout=20,
    )
    pages = _exchange_response(pages_response, "Facebook Page discovery").get("data")
    if not isinstance(pages, list):
        raise RuntimeError("Meta did not return a list of Facebook Pages")
    available_pages = [
        page for page in pages
        if isinstance(page, dict)
        and isinstance(page.get("id"), str)
        and isinstance(page.get("access_token"), str)
    ]
    desired_page_id = os.getenv("META_PAGE_ID", "").strip()
    candidates = [
        page for page in available_pages
        if not desired_page_id or page["id"] == desired_page_id
    ]
    if not desired_page_id and len(candidates) > 1:
        branded = [
            page for page in candidates
            if "fendly" in str(page.get("name") or "").casefold()
        ]
        if branded:
            candidates = branded
    if len(candidates) != 1:
        raise RuntimeError(
            "Could not identify one Fendly Facebook Page. Set META_PAGE_ID to its numeric ID "
            "and reconnect."
        )

    page = candidates[0]
    page_token = page["access_token"]
    page_id = page["id"]
    if not isinstance(page_token, str) or not isinstance(page_id, str):
        raise RuntimeError("Meta returned an invalid Facebook Page connection")
    instagram = page.get("instagram_business_account")
    instagram_id = instagram.get("id") if isinstance(instagram, dict) else None
    if "pages_manage_posts" not in granted:
        raise RuntimeError(
            "Meta did not grant pages_manage_posts. Check the Meta Login for Business "
            "configuration and reconnect."
        )
    if isinstance(instagram_id, str):
        _upsert_account(
            session,
            "instagram",
            instagram_id,
            str(instagram.get("username") or "Fendly Instagram"),
            page_token,
            user_token,
        )
    else:
        session.execute(delete(SocialAccount).where(SocialAccount.provider == "instagram"))
    _upsert_account(
        session,
        "facebook",
        page_id,
        str(page.get("name") or "Fendly Facebook Page"),
        page_token,
        user_token,
    )
    session.commit()
    return granted


@router.get("/status")
def get_social_status(
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> dict[str, dict[str, str | bool | None]]:
    accounts = {
        account.provider: account
        for account in session.scalars(
            select(SocialAccount).where(
                SocialAccount.provider.in_(("facebook", "instagram"))
            )
        ).all()
    }
    return {
        provider: {
            "connected": provider in accounts,
            "account_name": accounts[provider].account_name if provider in accounts else None,
        }
        for provider in ("facebook", "instagram")
    }


@router.post("/connect/meta")
def start_meta_connection(
    session: Session = Depends(get_db),
    uid: str = Depends(require_admin),
) -> dict[str, str]:
    _secret_box()
    app_id = _required_env("META_APP_ID")
    _required_env("META_APP_SECRET")
    config_id = _required_env("META_LOGIN_CONFIG_ID")
    redirect_uri = _redirect_uri()
    state = _oauth_state(session, uid)
    params = {
        "client_id": app_id,
        "redirect_uri": redirect_uri,
        "response_type": "code",
        "config_id": config_id,
        "state": state,
        "scope": ",".join(META_SCOPES),
    }
    return {"authorization_url": f"https://www.facebook.com/dialog/oauth?{urlencode(params)}"}


@router.get("/callback/meta", response_class=HTMLResponse)
def meta_oauth_callback(
    code: str | None = None,
    state: str | None = None,
    error: str | None = None,
    session: Session = Depends(get_db),
) -> HTMLResponse:
    if error:
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            "Meta authorization was cancelled or denied. Return to Fendly and reconnect.",
        )
    if not code or not state:
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            "Meta authorization response is missing required values.",
        )
    try:
        granted = _finish_meta_oauth(session, code, state)
    except HTTPException:
        raise
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as exception:
        logger.error(
            "Meta OAuth callback failed (%s)",
            type(exception).__name__,
        )
        raise HTTPException(
            status.HTTP_502_BAD_GATEWAY,
            "Meta could not complete the connection. Verify app configuration and reconnect.",
        ) from exception
    missing = REQUIRED_PUBLISHING_SCOPES - granted
    if missing:
        message = (
            "Meta connected the Facebook Page, but did not grant all adoption publishing "
            f"permissions: {', '.join(sorted(missing))}. Check the Meta Login for Business "
            "configuration and reconnect."
        )
    else:
        message = "Meta reconnected successfully. You may return to Fendly."
    escaped_message = (
        message.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    )
    return HTMLResponse(
        "<!doctype html><meta name='viewport' content='width=device-width'>"
        "<title>Fendly Meta connection</title>"
        "<body style='font:16px sans-serif;max-width:36rem;margin:3rem auto;padding:0 1rem'>"
        f"<h2>Fendly Meta connection</h2><p>{escaped_message}</p>"
        "<p>You can close this page and return to the Fendly admin workspace.</p></body>"
    )


@router.delete("/account/{provider}")
def disconnect_meta_account(
    provider: str,
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> dict[str, str]:
    if provider not in {"facebook", "instagram"}:
        raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, "Unsupported social account")
    accounts = session.scalars(
        select(SocialAccount).where(
            SocialAccount.provider.in_(("facebook", "instagram"))
        )
    ).all()
    for account in accounts:
        session.delete(account)
    session.commit()
    return {
        "message": "Fendly stopped using the saved Meta authorization. Revoke Fendly in Meta's app settings to revoke platform access too."
    }
