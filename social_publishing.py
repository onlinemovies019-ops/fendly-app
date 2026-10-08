import base64
import hashlib
import logging
import os
import re
import secrets
import time
from datetime import datetime, timezone
from typing import Literal, cast
from urllib.parse import urlencode, urlsplit

import httpx
from cryptography.fernet import Fernet, InvalidToken
from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException, Query, status
from fastapi.responses import HTMLResponse
from sqlalchemy import delete, select, update
from sqlalchemy.orm import Session

from database import SessionLocal, get_db
from models import FoundItem, LostItem, SocialAccount, SocialOAuthState, SocialPublication
from routers.admin import require_admin

router = APIRouter(prefix="/api/social", tags=["social publishing"])
logger = logging.getLogger(__name__)
EMAIL_PATTERN = re.compile(r"\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b", re.IGNORECASE)
PHONE_PATTERN = re.compile(r"(?<!\w)\+?[\d().\s-]{8,}\d(?!\w)")
PROVIDER_NAMES = ("facebook", "instagram", "x")


def _secret_box() -> Fernet:
    secret = os.getenv("APP_SECRET_KEY", "")
    if len(secret) < 32:
        raise RuntimeError("APP_SECRET_KEY must contain at least 32 characters")
    key = base64.urlsafe_b64encode(hashlib.sha256(secret.encode("utf-8")).digest())
    return Fernet(key)


def _encrypt_token(token: str) -> str:
    return _secret_box().encrypt(token.encode("utf-8")).decode("ascii")


def _decrypt_token(token: str) -> str:
    try:
        return _secret_box().decrypt(token.encode("ascii")).decode("utf-8")
    except (InvalidToken, UnicodeError, ValueError) as error:
        raise RuntimeError("Stored social authorization is invalid; reconnect the account") from error


def _redirect_uri(provider: Literal["meta", "x"]) -> str:
    configured = os.getenv(f"{'META' if provider == 'meta' else 'X'}_REDIRECT_URI", "").strip()
    if configured:
        parsed = urlsplit(configured)
        if parsed.scheme != "https" or not parsed.netloc:
            raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Social callback URL must use HTTPS")
        return configured
    base_url = os.getenv("PUBLIC_BASE_URL", "").rstrip("/")
    if not base_url.startswith("https://"):
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Social callback URL must use HTTPS")
    callback_provider = "meta" if provider == "meta" else "x"
    return f"{base_url}/api/social/callback/{callback_provider}"


def _required_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, f"{name} is not configured")
    return value


def _response_object(value: object) -> dict[str, object]:
    if not isinstance(value, dict):
        raise RuntimeError("Social platform returned an invalid response")
    return cast(dict[str, object], value)


def _response_array(value: object) -> list[dict[str, object]]:
    if not isinstance(value, list):
        raise RuntimeError("Social platform returned an invalid response")
    values = cast(list[object], value)
    return [_response_object(entry) for entry in values]


def _response_integer(value: object, default: int) -> int:
    if isinstance(value, int) and not isinstance(value, bool):
        return value
    return default


def _graph_url(path: str) -> str:
    version = _required_env("META_GRAPH_API_VERSION")
    if not re.fullmatch(r"v\d+\.\d+", version):
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "META_GRAPH_API_VERSION is invalid")
    return f"https://graph.facebook.com/{version}/{path.lstrip('/')}"


def _oauth_state(session: Session, provider: str, uid: str, verifier: str | None = None) -> str:
    state = secrets.token_urlsafe(32)
    session.execute(delete(SocialOAuthState).where(SocialOAuthState.expires_at < int(time.time())))
    session.add(
        SocialOAuthState(
            state_digest=hashlib.sha256(state.encode("utf-8")).hexdigest(),
            provider=provider,
            created_by=uid,
            expires_at=int(time.time()) + 600,
            code_verifier=verifier,
        )
    )
    session.commit()
    return state


def schedule_report_publications(
    session: Session,
    report: LostItem | FoundItem,
    report_type: str,
    background_tasks: BackgroundTasks | None,
) -> None:
    if not report.social_share_consent:
        return

    connected = [str(provider) for provider in session.scalars(select(SocialAccount.provider)).all()]
    if not connected:
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Social publishing is not connected yet. Uncheck social sharing or try again later.",
        )

    now = int(time.time())
    queued_ids: list[SocialPublication] = []
    for provider in connected:
        job = SocialPublication(
            report_id=report.id,
            report_type=report_type.lower(),
            provider=provider,
            status="pending",
            next_attempt_at=now,
        )
        if provider == "instagram" and not report.image_url:
            job.status = "skipped"
            job.last_error = "Instagram requires a public report photo; no photo was attached."
        session.add(job)
        if job.status == "pending":
            queued_ids.append(job)
    session.flush()
    if background_tasks is not None:
        for job in queued_ids:
            background_tasks.add_task(publish_publication, job.id)


def _safe_caption(report: LostItem | FoundItem, report_type: str) -> str:
    title = EMAIL_PATTERN.sub("[contact removed]", report.title or "")
    title = PHONE_PATTERN.sub("[number removed]", title)
    title = re.sub(r"\b\d{15}\b", "[identifier removed]", title)
    title = re.sub(r"[\x00-\x1f<>]", " ", title)
    title = " ".join(title.split())[:120].strip()
    return (
        f"Fendly community alert: {report_type.upper()} — {title or 'reported item'}.\n"
        "Help reunite it with its owner through the Fendly app."
    )


def _provider_response(response: httpx.Response) -> dict[str, object]:
    if response.is_error:
        raise RuntimeError(f"Social platform returned HTTP {response.status_code}")
    try:
        result: object = response.json()
    except ValueError as error:
        raise RuntimeError("Social platform returned an invalid response") from error
    return _response_object(result)


def _refresh_x_token(session: Session, account: SocialAccount) -> str:
    now = int(time.time())
    access_token = _decrypt_token(account.access_token_encrypted)
    if account.expires_at is None or account.expires_at > now + 120:
        return access_token
    if not account.refresh_token_encrypted:
        raise RuntimeError("X authorization expired; reconnect the account")

    client_id = _required_env("X_CLIENT_ID")
    form = {
        "grant_type": "refresh_token",
        "refresh_token": _decrypt_token(account.refresh_token_encrypted),
    }
    client_secret = os.getenv("X_CLIENT_SECRET", "").strip()
    auth = (client_id, client_secret) if client_secret else None
    if auth is None:
        form["client_id"] = client_id
    response = httpx.post(
        "https://api.x.com/2/oauth2/token",
        data=form,
        auth=auth,
        timeout=20,
    )
    result = _provider_response(response)
    new_token = result.get("access_token")
    if not isinstance(new_token, str) or not new_token:
        raise RuntimeError("X did not return a refreshed authorization")
    account.access_token_encrypted = _encrypt_token(new_token)
    refreshed_token = result.get("refresh_token")
    if isinstance(refreshed_token, str) and refreshed_token:
        account.refresh_token_encrypted = _encrypt_token(refreshed_token)
    account.expires_at = now + _response_integer(result.get("expires_in"), 7200)
    session.commit()
    return new_token


def _publish_facebook(
    account: SocialAccount,
    report: LostItem | FoundItem,
    caption: str,
) -> str:
    token = _decrypt_token(account.access_token_encrypted)
    image_url = report.image_url
    if image_url:
        result = _provider_response(
            httpx.post(
                _graph_url(f"{account.account_id}/photos"),
                data={"url": image_url, "caption": caption, "access_token": token},
                timeout=30,
            )
        )
    else:
        result = _provider_response(
            httpx.post(
                _graph_url(f"{account.account_id}/feed"),
                data={"message": caption, "access_token": token},
                timeout=30,
            )
        )
    post_id = result.get("post_id") or result.get("id")
    if not isinstance(post_id, str) or not post_id:
        raise RuntimeError("Facebook did not return a post identifier")
    return post_id


def _publish_instagram(
    account: SocialAccount,
    report: LostItem | FoundItem,
    caption: str,
) -> str:
    if not report.image_url:
        raise RuntimeError("Instagram requires a report photo")
    token = _decrypt_token(account.access_token_encrypted)
    created = _provider_response(
        httpx.post(
            _graph_url(f"{account.account_id}/media"),
            data={"image_url": report.image_url, "caption": caption, "access_token": token},
            timeout=30,
        )
    )
    creation_id = created.get("id")
    if not isinstance(creation_id, str) or not creation_id:
        raise RuntimeError("Instagram did not return a media identifier")
    published = _provider_response(
        httpx.post(
            _graph_url(f"{account.account_id}/media_publish"),
            data={"creation_id": creation_id, "access_token": token},
            timeout=30,
        )
    )
    post_id = published.get("id")
    if not isinstance(post_id, str) or not post_id:
        raise RuntimeError("Instagram did not return a published media identifier")
    return post_id


def _publish_x(session: Session, account: SocialAccount, caption: str) -> str:
    token = _refresh_x_token(session, account)
    response = httpx.post(
        "https://api.x.com/2/tweets",
        json={"text": caption},
        headers={"Authorization": f"Bearer {token}"},
        timeout=20,
    )
    result = _provider_response(response)
    data_value = result.get("data")
    data = _response_object(data_value) if data_value is not None else {}
    post_id = data.get("id")
    if not isinstance(post_id, str) or not post_id:
        raise RuntimeError("X did not return a post identifier")
    return post_id


def publish_publication(publication_id: str) -> None:
    now = int(time.time())
    with SessionLocal() as session:
        claimed = session.execute(
            update(SocialPublication)
            .where(
                SocialPublication.id == publication_id,
                SocialPublication.status == "pending",
                SocialPublication.next_attempt_at <= now,
            )
            .values(
                status="processing",
                attempt_count=SocialPublication.attempt_count + 1,
                updated_at=datetime.now(timezone.utc),
            )
        )
        session.commit()
        if claimed.rowcount != 1:
            return

        publication = session.get(SocialPublication, publication_id)
        if publication is None:
            return
        account = session.get(SocialAccount, publication.provider)
        model = LostItem if publication.report_type == "lost" else FoundItem
        report = session.get(model, publication.report_id)
        try:
            if account is None:
                raise RuntimeError("Fendly's social account is disconnected")
            if report is None or not report.social_share_consent:
                publication.status = "skipped"
                publication.last_error = "The report no longer exists or social-sharing consent was withdrawn."
                session.commit()
                return
            caption = _safe_caption(report, publication.report_type)
            if publication.provider == "facebook":
                post_id = _publish_facebook(account, report, caption)
            elif publication.provider == "instagram":
                post_id = _publish_instagram(account, report, caption)
            elif publication.provider == "x":
                post_id = _publish_x(session, account, caption)
            else:
                raise RuntimeError("Unsupported social platform")
            publication.status = "published"
            publication.external_post_id = post_id
            publication.last_error = None
            publication.published_at = datetime.now(timezone.utc)
            session.commit()
        except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
            session.rollback()
            publication = session.get(SocialPublication, publication_id)
            if publication is None:
                logger.error("Social publication %s disappeared after provider failure", publication_id)
                return
            error_text = str(error)[:400] or type(error).__name__
            publication.status = "failed"
            publication.next_attempt_at = 0
            error_text = (
                f"{error_text}. Not retried automatically to avoid duplicate public posts."
            )[:500]
            publication.last_error = error_text
            session.commit()
            logger.error(
                "Social publication failed: provider=%s report_id=%s attempt=%s error=%s",
                publication.provider,
                publication.report_id,
                publication.attempt_count,
                error_text,
            )
        except Exception:
            session.rollback()
            logger.exception("Unexpected social publication failure for %s", publication_id)
            publication = session.get(SocialPublication, publication_id)
            if publication is not None:
                publication.status = "failed"
                publication.last_error = (
                    "Unexpected publishing error. Check the platform before retrying "
                    "to avoid a duplicate public post."
                )
                publication.next_attempt_at = 0
                session.commit()


def process_due_publications() -> None:
    now = int(time.time())
    stale_before = datetime.fromtimestamp(now - 900, timezone.utc)
    with SessionLocal() as session:
        stale_ids = session.scalars(
            select(SocialPublication.id).where(
                SocialPublication.status == "processing",
                SocialPublication.updated_at <= stale_before,
            ).limit(20)
        ).all()
        if stale_ids:
            session.execute(
                update(SocialPublication)
                .where(SocialPublication.id.in_(stale_ids))
                .values(
                    status="failed",
                    next_attempt_at=0,
                    last_error=(
                        "Worker stopped during publication. Check the platform before "
                        "retrying to avoid a duplicate public post."
                    ),
                )
            )
            session.commit()
        due_ids = session.scalars(
            select(SocialPublication.id)
            .where(
                SocialPublication.status == "pending",
                SocialPublication.next_attempt_at <= now,
            )
            .order_by(SocialPublication.created_at)
            .limit(20)
        ).all()
    for publication_id in due_ids:
        publish_publication(publication_id)


def _consume_state(session: Session, state: str, provider: str) -> str | None:
    digest = hashlib.sha256(state.encode("utf-8")).hexdigest()
    record = session.get(SocialOAuthState, digest)
    if record is None or record.provider != provider or record.expires_at < int(time.time()):
        if record is not None:
            session.delete(record)
            session.commit()
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "OAuth state is invalid or expired; restart connection.")
    code_verifier = record.code_verifier
    session.delete(record)
    session.commit()
    return code_verifier


def _upsert_social_account(
    session: Session,
    provider: str,
    account_id: str,
    account_name: str,
    access_token: str,
    *,
    refresh_token: str | None = None,
    expires_at: int | None = None,
) -> None:
    account = session.get(SocialAccount, provider)
    values: dict[str, object] = {
        "account_id": account_id,
        "account_name": account_name[:160],
        "access_token_encrypted": _encrypt_token(access_token),
        "refresh_token_encrypted": _encrypt_token(refresh_token) if refresh_token else None,
        "expires_at": expires_at,
        "updated_at": datetime.now(timezone.utc),
    }
    if account is None:
        account = SocialAccount(provider=provider, **values)
        session.add(account)
    else:
        for key, value in values.items():
            setattr(account, key, value)


def _finish_meta_oauth(session: Session, code: str, state: str) -> None:
    _consume_state(session, state, "meta")
    app_id = _required_env("META_APP_ID")
    app_secret = _required_env("META_APP_SECRET")
    redirect_uri = _redirect_uri("meta")
    response = httpx.get(
        _graph_url("oauth/access_token"),
        params={
            "client_id": app_id,
            "client_secret": app_secret,
            "redirect_uri": redirect_uri,
            "code": code,
        },
        timeout=20,
    )
    token_result = _provider_response(response)
    user_token = token_result.get("access_token")
    if not isinstance(user_token, str) or not user_token:
        raise RuntimeError("Meta did not return an access token")
    pages_result = _provider_response(
        httpx.get(
            _graph_url("me/accounts"),
            params={
                "fields": "id,name,access_token,instagram_business_account{id,username}",
                "access_token": user_token,
            },
            timeout=20,
        )
    )
    pages = _response_array(pages_result.get("data"))
    desired_page_id = os.getenv("META_PAGE_ID", "").strip()
    candidates: list[dict[str, object]] = []
    for page in pages:
        if isinstance(page.get("id"), str) and isinstance(page.get("access_token"), str):
            candidates.append(page)
    if desired_page_id:
        candidates = [page for page in candidates if page["id"] == desired_page_id]
    elif len(candidates) > 1:
        matching = [page for page in candidates if "fendly" in str(page.get("name", "")).casefold()]
        if matching:
            candidates = matching
    if len(candidates) != 1:
        raise RuntimeError("Select one Fendly Page by setting META_PAGE_ID, then reconnect Meta")
    page = candidates[0]
    page_id = page.get("id")
    page_token = page.get("access_token")
    if not isinstance(page_id, str) or not isinstance(page_token, str):
        raise RuntimeError("Meta returned an invalid Fendly Page")
    page_name = str(page.get("name") or "Fendly Facebook Page")
    _upsert_social_account(session, "facebook", page_id, page_name, page_token)
    instagram_value = page.get("instagram_business_account")
    instagram: dict[str, object] | None = (
        _response_object(instagram_value) if instagram_value is not None else None
    )
    instagram_id = instagram.get("id") if instagram is not None else None
    if isinstance(instagram_id, str) and instagram is not None:
        _upsert_social_account(
            session,
            "instagram",
            instagram_id,
            str(instagram.get("username") or "Fendly Instagram"),
            page_token,
        )
    else:
        session.execute(delete(SocialAccount).where(SocialAccount.provider == "instagram"))
    session.commit()


def _finish_x_oauth(session: Session, code: str, state: str) -> None:
    code_verifier = _consume_state(session, state, "x")
    client_id = _required_env("X_CLIENT_ID")
    form = {
        "grant_type": "authorization_code",
        "code": code,
        "redirect_uri": _redirect_uri("x"),
        "code_verifier": code_verifier or "",
    }
    client_secret = os.getenv("X_CLIENT_SECRET", "").strip()
    if not client_secret:
        form["client_id"] = client_id
    response = httpx.post(
        "https://api.x.com/2/oauth2/token",
        data=form,
        auth=(client_id, client_secret) if client_secret else None,
        timeout=20,
    )
    result = _provider_response(response)
    access_token = result.get("access_token")
    refresh_token = result.get("refresh_token")
    if not isinstance(access_token, str) or not access_token:
        raise RuntimeError("X did not return an access token")
    profile = _provider_response(
        httpx.get(
            "https://api.x.com/2/users/me",
            params={"user.fields": "name,username"},
            headers={"Authorization": f"Bearer {access_token}"},
            timeout=20,
        )
    )
    data_value = profile.get("data")
    data = _response_object(data_value) if data_value is not None else {}
    account_id = data.get("id")
    if not isinstance(account_id, str):
        raise RuntimeError("X did not return the authorized account")
    account_name = data.get("username") or data.get("name") or "Fendly X account"
    _upsert_social_account(
        session,
        "x",
        account_id,
        str(account_name),
        access_token,
        refresh_token=refresh_token if isinstance(refresh_token, str) else None,
        expires_at=int(time.time()) + _response_integer(result.get("expires_in"), 7200),
    )
    session.commit()


@router.get("/status")
def get_social_status(
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> dict[str, dict[str, str | bool | None]]:
    accounts = {
        account.provider: account
        for account in session.scalars(select(SocialAccount)).all()
    }
    return {
        provider: {
            "connected": provider in accounts,
            "account_name": accounts[provider].account_name if provider in accounts else None,
        }
        for provider in PROVIDER_NAMES
    }


@router.get("/publications")
def list_social_publications(
    limit: int = Query(default=50, ge=1, le=200),
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, str | int | None]]:
    publications = session.scalars(
        select(SocialPublication)
        .order_by(SocialPublication.created_at.desc())
        .limit(limit)
    ).all()
    return [
        {
            "report_id": publication.report_id,
            "report_type": publication.report_type,
            "provider": publication.provider,
            "status": publication.status,
            "attempt_count": publication.attempt_count,
            "external_post_id": publication.external_post_id,
            "last_error": publication.last_error,
        }
        for publication in publications
    ]


@router.post("/connect/{provider}")
def start_social_connection(
    provider: Literal["meta", "x"],
    session: Session = Depends(get_db),
    uid: str = Depends(require_admin),
) -> dict[str, str]:
    if provider == "meta":
        _secret_box()
        app_id = _required_env("META_APP_ID")
        _required_env("META_APP_SECRET")
        config_id = _required_env("META_LOGIN_CONFIG_ID")
        _required_env("META_GRAPH_API_VERSION")
        redirect_uri = _redirect_uri("meta")
        state = _oauth_state(session, "meta", uid)
        params = {
            "client_id": app_id,
            "redirect_uri": redirect_uri,
            "response_type": "code",
            "config_id": config_id,
            "state": state,
        }
        return {"authorization_url": f"https://www.facebook.com/dialog/oauth?{urlencode(params)}"}

    _secret_box()
    client_id = _required_env("X_CLIENT_ID")
    redirect_uri = _redirect_uri("x")
    verifier = secrets.token_urlsafe(64)
    challenge = base64.urlsafe_b64encode(
        hashlib.sha256(verifier.encode("ascii")).digest()
    ).rstrip(b"=").decode("ascii")
    state = _oauth_state(session, "x", uid, verifier)
    params = {
        "response_type": "code",
        "client_id": client_id,
        "redirect_uri": redirect_uri,
        "scope": "tweet.read tweet.write users.read offline.access",
        "state": state,
        "code_challenge": challenge,
        "code_challenge_method": "S256",
    }
    return {"authorization_url": f"https://x.com/i/oauth2/authorize?{urlencode(params)}"}


@router.get("/callback/{provider}", response_class=HTMLResponse)
def social_oauth_callback(
    provider: Literal["meta", "x"],
    code: str | None = None,
    state: str | None = None,
    error: str | None = None,
    session: Session = Depends(get_db),
) -> HTMLResponse:
    if error:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Social account authorization was not completed.")
    if not code or not state:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Authorization response is missing required values.")
    try:
        if provider == "meta":
            _finish_meta_oauth(session, code, state)
        else:
            _finish_x_oauth(session, code, state)
    except HTTPException:
        raise
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as exception:
        diagnostic = str(exception) if isinstance(exception, RuntimeError) else type(exception).__name__
        logger.error("Social OAuth callback failed: provider=%s error=%s", provider, diagnostic)
        raise HTTPException(
            status.HTTP_502_BAD_GATEWAY,
            "The social platform could not complete authorization. Check configuration and reconnect.",
        ) from exception
    return HTMLResponse(
        "<!doctype html><title>Fendly social account connected</title>"
        "<p>Fendly social account connected. You may close this window.</p>"
    )


@router.delete("/account/{provider}")
def disconnect_social_account(
    provider: Literal["facebook", "instagram", "x"],
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> dict[str, str]:
    providers = ("facebook", "instagram") if provider in {"facebook", "instagram"} else ("x",)
    accounts = session.scalars(
        select(SocialAccount).where(SocialAccount.provider.in_(providers))
    ).all()
    for account in accounts:
        session.delete(account)
    session.commit()
    return {
        "message": "Fendly stopped using the stored authorization. Revoke Fendly in the platform's app settings to revoke platform-side access."
    }
