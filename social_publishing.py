import asyncio
import base64
import hashlib
import logging
import os
import re
import secrets
import time
from datetime import datetime, timezone
from typing import Literal, cast
from urllib.parse import quote, urlencode, urlsplit
from uuid import uuid4

import httpx
from cryptography.fernet import Fernet, InvalidToken
from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException, Query, status
from fastapi.responses import HTMLResponse
from sqlalchemy import delete, select, update
from sqlalchemy.orm import Session

from app_links import fendly_report_url
from database import SessionLocal, get_db
from models import FoundItem, LostItem, SocialAccount, SocialOAuthState, SocialPublication
from routers.admin import require_admin
from social_content import sanitize_public_title
from social_poster import render_report_poster
from image_storage import store_image

router = APIRouter(prefix="/api/social", tags=["social publishing"])
logger = logging.getLogger(__name__)
PROVIDER_NAMES = ("facebook", "instagram")
LEGACY_POSTER_FAILURE = (
    "Fendly community poster could not be generated; "
    "the original report photo was not published."
)


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


def _redirect_uri() -> str:
    configured = os.getenv("META_REDIRECT_URI", "").strip()
    if configured:
        parsed = urlsplit(configured)
        if parsed.scheme != "https" or not parsed.netloc:
            raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Social callback URL must use HTTPS")
        return configured
    base_url = os.getenv("PUBLIC_BASE_URL", "").rstrip("/")
    if not base_url.startswith("https://"):
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Social callback URL must use HTTPS")
    return f"{base_url}/api/social/callback/meta"


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


def _graph_url(path: str) -> str:
    version = _required_env("META_GRAPH_API_VERSION")
    if not re.fullmatch(r"v\d+\.\d+", version):
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "META_GRAPH_API_VERSION is invalid")
    return f"https://graph.facebook.com/{version}/{path.lstrip('/')}"


def _oauth_state(session: Session, uid: str) -> str:
    state = secrets.token_urlsafe(32)
    session.execute(delete(SocialOAuthState).where(SocialOAuthState.expires_at < int(time.time())))
    session.add(
        SocialOAuthState(
            state_digest=hashlib.sha256(state.encode("utf-8")).hexdigest(),
            provider="meta",
            created_by=uid,
            expires_at=int(time.time()) + 600,
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

    connected = [
        str(provider)
        for provider in session.scalars(
            select(SocialAccount.provider).where(SocialAccount.provider.in_(PROVIDER_NAMES))
        ).all()
    ]
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
        session.add(job)
        if job.status == "pending":
            queued_ids.append(job)
    session.flush()
    if background_tasks is not None:
        for job in queued_ids:
            background_tasks.add_task(publish_publication, job.id)


def _safe_caption(report: LostItem | FoundItem, report_type: str) -> str:
    title = sanitize_public_title(report.title, max_length=80)
    report_url = fendly_report_url(report.id)
    return (
        f"Fendly community alert: {report_type.upper()} — {title or 'reported item'}.\n"
        f"Help reunite it with its owner.\nFendly: {report_url}"
    )


async def _generate_and_store_poster(report: LostItem | FoundItem, report_type: str) -> str:
    poster_bytes = await render_report_poster(report, report_type)
    return await store_image(poster_bytes, f"{uuid4().hex}.jpg", "image/jpeg")


def _provider_response(response: httpx.Response) -> dict[str, object]:
    if response.is_error:
        raise RuntimeError(f"Social platform returned HTTP {response.status_code}")
    try:
        result: object = response.json()
    except ValueError as error:
        raise RuntimeError("Social platform returned an invalid response") from error
    return _response_object(result)


def _publish_facebook(
    account: SocialAccount,
    report: LostItem | FoundItem,
    caption: str,
) -> str:
    token = _decrypt_token(account.access_token_encrypted)
    if not report.social_poster_url:
        raise RuntimeError("Fendly community poster is not available; the report photo was not published.")
    result = _provider_response(
        httpx.post(
            _graph_url(f"{account.account_id}/photos"),
            data={"url": report.social_poster_url, "caption": caption, "access_token": token},
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
    if not report.social_poster_url:
        raise RuntimeError("Fendly community poster is not available; Instagram requires a generated poster.")
    token = _decrypt_token(account.access_token_encrypted)
    created = _provider_response(
        httpx.post(
            _graph_url(f"{account.account_id}/media"),
            data={"image_url": report.social_poster_url, "caption": caption, "access_token": token},
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
            if not report.social_poster_url:
                report.social_poster_url = asyncio.run(
                    _generate_and_store_poster(report, publication.report_type)
                )
                session.commit()
            caption = _safe_caption(report, publication.report_type)
            if publication.provider == "facebook":
                post_id = _publish_facebook(account, report, caption)
            elif publication.provider == "instagram":
                post_id = _publish_instagram(account, report, caption)
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
        session.execute(
            update(SocialPublication)
            .where(
                SocialPublication.status == "failed",
                SocialPublication.last_error == LEGACY_POSTER_FAILURE,
            )
            .values(status="pending", next_attempt_at=now, last_error=None)
        )
        session.commit()
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


def _consume_state(session: Session, state: str) -> None:
    digest = hashlib.sha256(state.encode("utf-8")).hexdigest()
    record = session.get(SocialOAuthState, digest)
    if record is None or record.provider != "meta" or record.expires_at < int(time.time()):
        if record is not None:
            session.delete(record)
            session.commit()
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "OAuth state is invalid or expired; restart connection.")
    session.delete(record)
    session.commit()


def _upsert_social_account(
    session: Session,
    provider: str,
    account_id: str,
    account_name: str,
    access_token: str,
) -> None:
    account = session.get(SocialAccount, provider)
    values: dict[str, object] = {
        "account_id": account_id,
        "account_name": account_name[:160],
        "access_token_encrypted": _encrypt_token(access_token),
        "updated_at": datetime.now(timezone.utc),
    }
    if account is None:
        account = SocialAccount(provider=provider, **values)
        session.add(account)
    else:
        for key, value in values.items():
            setattr(account, key, value)


def _finish_meta_oauth(session: Session, code: str, state: str) -> None:
    _consume_state(session, state)
    app_id = _required_env("META_APP_ID")
    app_secret = _required_env("META_APP_SECRET")
    redirect_uri = _redirect_uri()
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
    available_pages = [
        page for page in pages
        if isinstance(page.get("id"), str) and isinstance(page.get("access_token"), str)
    ]
    desired_page_id = os.getenv("META_PAGE_ID", "").strip()
    candidates = available_pages
    if desired_page_id:
        candidates = [page for page in candidates if page["id"] == desired_page_id]
    elif len(candidates) > 1:
        matching = [page for page in candidates if "fendly" in str(page.get("name", "")).casefold()]
        if matching:
            candidates = matching
    if len(candidates) != 1:
        available = ", ".join(
            f"{page['id']} ({str(page.get('name') or 'unnamed')[:80]})"
            for page in available_pages
        ) or "none"
        if not available_pages:
            reason = "Meta returned no Pages with access tokens"
        elif desired_page_id and not candidates:
            reason = "META_PAGE_ID did not match an available Page"
        else:
            reason = "Meta Page selection was ambiguous"
        raise RuntimeError(f"{reason}; available Pages: {available}")
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


def _publication_summary(
    publication: SocialPublication,
    platform_status: str = "not_checked",
) -> dict[str, str | int | None]:
    return {
        "report_id": publication.report_id,
        "report_type": publication.report_type,
        "provider": publication.provider,
        "status": publication.status,
        "attempt_count": publication.attempt_count,
        "external_post_id": publication.external_post_id,
        "last_error": publication.last_error,
        "platform_status": platform_status,
    }


def _list_external_post_ids(
    account: SocialAccount,
    expected_post_ids: set[str],
) -> tuple[set[str], bool]:
    edge = "published_posts" if account.provider == "facebook" else "media"
    account_id = quote(account.account_id, safe="")
    access_token = _decrypt_token(account.access_token_encrypted)
    post_ids: set[str] = set()
    after: str | None = None
    seen_cursors: set[str] = set()

    for _ in range(100):
        params: dict[str, str | int] = {
            "fields": "id",
            "limit": 100,
            "access_token": access_token,
        }
        if after is not None:
            params["after"] = after
        try:
            response = httpx.get(
                _graph_url(f"{account_id}/{edge}"),
                params=params,
                timeout=15,
            )
            payload = _response_object(response.json())
        except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
            logger.warning("Meta post feed check failed: %s", type(error).__name__)
            return post_ids, False

        if response.is_error or "error" in payload:
            logger.warning("Meta post feed check was inconclusive: HTTP %s", response.status_code)
            return post_ids, False

        try:
            entries = _response_array(payload.get("data"))
            for entry in entries:
                post_id = entry.get("id")
                if isinstance(post_id, str) and post_id:
                    post_ids.add(post_id)
        except (RuntimeError, TypeError):
            logger.warning("Meta post feed check returned an invalid response")
            return post_ids, False

        if expected_post_ids.issubset(post_ids):
            return post_ids, True

        paging = payload.get("paging")
        if not isinstance(paging, dict) or not paging.get("next"):
            return post_ids, True

        cursors = paging.get("cursors")
        next_cursor = cursors.get("after") if isinstance(cursors, dict) else None
        if not isinstance(next_cursor, str) or not next_cursor or next_cursor in seen_cursors:
            logger.warning("Meta post feed pagination was incomplete")
            return post_ids, False
        seen_cursors.add(next_cursor)
        after = next_cursor

    logger.warning("Meta post feed exceeded the pagination safety limit")
    return post_ids, False


def _check_external_post(account: SocialAccount, external_post_id: str) -> str:
    try:
        response = httpx.get(
            _graph_url(quote(external_post_id, safe="")),
            params={
                "fields": "id",
                "access_token": _decrypt_token(account.access_token_encrypted),
            },
            timeout=15,
        )
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
        logger.warning("Meta post lookup failed: %s", type(error).__name__)
        return "check_failed"

    if response.status_code == 404:
        return "unavailable"

    try:
        payload = _response_object(response.json())
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
        logger.warning("Meta post lookup failed: %s", type(error).__name__)
        return "check_failed"

    if not response.is_error and isinstance(payload.get("id"), str):
        return "available"

    error_value = payload.get("error")
    if isinstance(error_value, dict):
        error_payload = _response_object(error_value)
        if error_payload.get("code") == 100 and error_payload.get("error_subcode") == 33:
            return "unavailable"

    logger.warning("Meta post lookup was inconclusive: HTTP %s", response.status_code)
    return "check_failed"


@router.get("/publications")
def list_social_publications(
    limit: int = Query(default=50, ge=1, le=200),
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, str | int | None]]:
    publications = session.scalars(
        select(SocialPublication)
        .where(
            SocialPublication.provider.in_(PROVIDER_NAMES),
            SocialPublication.status != "removed",
        )
        .order_by(SocialPublication.created_at.desc())
        .limit(limit)
    ).all()
    return [_publication_summary(publication) for publication in publications]


@router.post("/publications/refresh")
def refresh_social_publications(
    limit: int = Query(default=20, ge=1, le=50),
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, str | int | None]]:
    publications = session.scalars(
        select(SocialPublication)
        .where(
            SocialPublication.provider.in_(PROVIDER_NAMES),
            SocialPublication.status != "removed",
        )
        .order_by(SocialPublication.created_at.desc())
        .limit(limit)
    ).all()
    accounts = {
        account.provider: account
        for account in session.scalars(
            select(SocialAccount).where(SocialAccount.provider.in_(PROVIDER_NAMES))
        ).all()
    }
    feed_results: dict[str, tuple[set[str], bool]] = {}
    post_lookup_results: dict[tuple[str, str], str] = {}
    for provider in PROVIDER_NAMES:
        provider_publications = [
            publication
            for publication in publications
            if publication.provider == provider
            and publication.status == "published"
            and publication.external_post_id
        ]
        account = accounts.get(provider)
        if provider_publications and account is not None:
            expected_post_ids = {
                publication.external_post_id
                for publication in provider_publications
                if publication.external_post_id
            }
            feed_results[provider] = _list_external_post_ids(account, expected_post_ids)
            post_ids, complete = feed_results[provider]
            if not complete:
                for post_id in expected_post_ids - post_ids:
                    post_lookup_results[(provider, post_id)] = _check_external_post(account, post_id)

    refreshed = []
    has_removed_publications = False
    for publication in publications:
        platform_status = "not_checked"
        if publication.status == "published" and publication.external_post_id:
            feed_result = feed_results.get(publication.provider)
            if feed_result is not None:
                post_ids, complete = feed_result
                if publication.external_post_id in post_ids:
                    platform_status = "available"
                elif complete:
                    platform_status = "unavailable"
                else:
                    platform_status = post_lookup_results.get(
                        (publication.provider, publication.external_post_id),
                        "check_failed",
                    )
            else:
                platform_status = "check_failed"
        if platform_status == "unavailable":
            publication.status = "removed"
            publication.last_error = "Post is no longer available to the connected Meta account."
            has_removed_publications = True
            continue
        refreshed.append(_publication_summary(publication, platform_status))
    if has_removed_publications:
        session.commit()
    return refreshed


@router.post("/connect/meta")
def start_social_connection(
    session: Session = Depends(get_db),
    uid: str = Depends(require_admin),
) -> dict[str, str]:
    _secret_box()
    app_id = _required_env("META_APP_ID")
    _required_env("META_APP_SECRET")
    config_id = _required_env("META_LOGIN_CONFIG_ID")
    _required_env("META_GRAPH_API_VERSION")
    redirect_uri = _redirect_uri()
    state = _oauth_state(session, uid)
    params = {
        "client_id": app_id,
        "redirect_uri": redirect_uri,
        "response_type": "code",
        "config_id": config_id,
        "state": state,
    }
    return {"authorization_url": f"https://www.facebook.com/dialog/oauth?{urlencode(params)}"}


@router.get("/callback/meta", response_class=HTMLResponse)
def social_oauth_callback(
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
        _finish_meta_oauth(session, code, state)
    except HTTPException:
        raise
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as exception:
        diagnostic = str(exception) if isinstance(exception, RuntimeError) else type(exception).__name__
        logger.error("Social OAuth callback failed: provider=meta error=%s", diagnostic)
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
    provider: Literal["facebook", "instagram"],
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> dict[str, str]:
    other_meta_provider = "instagram" if provider == "facebook" else "facebook"
    providers = (provider, other_meta_provider)
    accounts = session.scalars(
        select(SocialAccount).where(SocialAccount.provider.in_(providers))
    ).all()
    for account in accounts:
        session.delete(account)
    session.commit()
    return {
        "message": "Fendly stopped using the stored authorization. Revoke Fendly in the platform's app settings to revoke platform-side access."
    }
