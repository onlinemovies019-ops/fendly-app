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

import httpx
from cryptography.fernet import Fernet, InvalidToken
from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException, Query, status
from fastapi.responses import HTMLResponse
from sqlalchemy import delete, or_, select, update
from sqlalchemy.orm import Session

from app_links import fendly_report_url
from database import SessionLocal, get_db
from models import FoundItem, LostItem, SocialAccount, SocialOAuthState, SocialPublication
from routers.admin import require_admin

router = APIRouter(prefix="/api/social", tags=["social publishing"])
logger = logging.getLogger(__name__)
PROVIDER_NAMES = ("facebook", "instagram")
META_OAUTH_SCOPES = (
    "public_profile",
    "pages_show_list",
    "pages_read_engagement",
    "pages_manage_posts",
    "instagram_basic",
    "instagram_content_publish",
    "instagram_manage_contents",
)
LEGACY_POSTER_FAILURE = (
    "Fendly community poster could not be generated; "
    "the original report photo was not published."
)
UNEXPECTED_PUBLISHING_FAILURE = (
    "Unexpected publishing error. Check the platform before retrying "
    "to avoid a duplicate public post."
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
    raise HTTPException(
        status.HTTP_410_GONE,
        "Automatic social-media publication has been permanently disabled.",
    )


def _provider_response(
    response: httpx.Response,
    operation: str = "Meta API request",
) -> dict[str, object]:
    if response.is_error:
        try:
            payload: object = response.json()
        except ValueError:
            payload = None
        error = payload.get("error") if isinstance(payload, dict) else None
        details: list[str] = []
        if isinstance(error, dict):
            message = error.get("message")
            code = error.get("code")
            subcode = error.get("error_subcode")
            trace_id = error.get("fbtrace_id")
            if isinstance(code, (int, str)):
                details.append(f"code {code}")
            if isinstance(subcode, (int, str)):
                details.append(f"subcode {subcode}")
            if isinstance(message, str) and message.strip():
                details.append(message.strip().replace("\n", " ")[:250])
            if isinstance(trace_id, str) and trace_id.strip():
                details.append(f"trace {trace_id.strip()[:80]}")
        detail = f": {'; '.join(details)}" if details else ""
        raise RuntimeError(
            f"Meta {operation} failed (HTTP {response.status_code}){detail}"
        )
    try:
        result: object = response.json()
    except ValueError as error:
        raise RuntimeError("Social platform returned an invalid response") from error
    return _response_object(result)


def publish_publication(publication_id: str) -> None:
    with SessionLocal() as session:
        publication = session.get(SocialPublication, publication_id)
        if publication is None:
            return
        if publication.status in {"pending", "processing"}:
            publication.status = "cancelled"
            publication.next_attempt_at = 0
            publication.last_error = "Automatic social-media publication has been disabled."
            session.commit()


def remove_report_publications(
    session: Session,
    report_id: str,
    report_type: str,
) -> list[str]:
    publications = session.scalars(
        select(SocialPublication).where(
            SocialPublication.report_id == report_id,
            SocialPublication.report_type == report_type,
            SocialPublication.provider.in_(PROVIDER_NAMES),
        )
    ).all()
    failures: list[str] = []
    for publication in publications:
        if publication.status == "processing" and not publication.external_post_id:
            failures.append(
                f"{publication.provider} publication is still in progress; retry removal shortly."
            )
            continue
        if not publication.external_post_id:
            if publication.status == "failed" and (
                publication.last_error == UNEXPECTED_PUBLISHING_FAILURE
                or "Not retried automatically to avoid duplicate public posts."
                in (publication.last_error or "")
            ):
                account = session.get(SocialAccount, publication.provider)
                if account is None:
                    error_text = (
                        f"{publication.provider} post may have been published without "
                        "a saved post ID, and the social account is disconnected."
                    )
                else:
                    post_ids, feed_complete = _find_report_publication_post_ids(
                        account,
                        report_id,
                    )
                    if feed_complete:
                        removal_error = _remove_meta_posts_for_report_link(
                            account,
                            publication.provider,
                            report_id,
                            post_ids,
                        )
                        if removal_error is None:
                            session.delete(publication)
                            continue
                        error_text = removal_error
                    else:
                        error_text = (
                            f"{publication.provider} post may have been published without "
                            "a saved post ID. Meta's feed could not be fully checked, so "
                            "removal was not confirmed."
                        )
                publication.status = "failed"
                publication.next_attempt_at = 0
                publication.last_error = error_text[:500]
                failures.append(error_text)
                logger.error(
                    "Could not reconcile legacy %s publication for report %s: %s",
                    publication.provider,
                    report_id,
                    error_text,
                )
                continue
            session.delete(publication)
            continue

        account = session.get(SocialAccount, publication.provider)
        if account is None:
            error_text = f"{publication.provider} post could not be removed: account is disconnected."
        else:
            external_post_ids, feed_complete = _list_external_post_ids(
                account,
                {publication.external_post_id},
            )
            if feed_complete and publication.external_post_id not in external_post_ids:
                post_ids, search_complete = _find_report_publication_post_ids(
                    account,
                    report_id,
                )
                if search_complete:
                    removal_error = _remove_meta_posts_for_report_link(
                        account,
                        publication.provider,
                        report_id,
                        post_ids,
                    )
                    if removal_error is None:
                        session.delete(publication)
                        continue
                    error_text = removal_error
                else:
                    error_text = (
                        f"{publication.provider} post could not be found by its saved ID, "
                        "and Meta's report-link search was inconclusive."
                    )
                publication.status = "failed"
                publication.next_attempt_at = 0
                publication.last_error = error_text[:500]
                failures.append(error_text)
                continue
            availability = (
                "available"
                if publication.external_post_id in external_post_ids
                else _check_external_post(account, publication.external_post_id)
            )
            if availability == "unavailable":
                post_ids, search_complete = _find_report_publication_post_ids(
                    account,
                    report_id,
                )
                if search_complete:
                    removal_error = _remove_meta_posts_for_report_link(
                        account,
                        publication.provider,
                        report_id,
                        post_ids,
                    )
                    if removal_error is None:
                        session.delete(publication)
                        continue
                    error_text = removal_error
                    publication.status = "failed"
                    publication.next_attempt_at = 0
                    publication.last_error = error_text[:500]
                    failures.append(error_text)
                    continue
            try:
                _delete_meta_post(account, publication.provider, publication.external_post_id)
                session.delete(publication)
                continue
            except HTTPException as error:
                error_text = (
                    f"{publication.provider} post could not be removed: "
                    f"{str(error.detail)[:300]}"
                )
            except httpx.HTTPError as error:
                logger.warning(
                    "Meta request for %s post deletion failed with %s",
                    publication.provider,
                    type(error).__name__,
                )
                error_text = (
                    f"{publication.provider} post could not be removed: "
                    "Meta API network request failed."
                )
            except (RuntimeError, ValueError, TypeError) as error:
                error_text = (
                    f"{publication.provider} post could not be removed: {str(error)[:300]}"
                )

            post_ids, search_complete = _find_report_publication_post_ids(
                account,
                report_id,
            )
            if search_complete:
                removal_error = _remove_meta_posts_for_report_link(
                    account,
                    publication.provider,
                    report_id,
                    post_ids,
                )
                if removal_error is None:
                    session.delete(publication)
                    continue
                error_text = removal_error

        publication.status = "failed"
        publication.next_attempt_at = 0
        publication.last_error = error_text[:500]
        failures.append(error_text)
        logger.error(
            "Admin removed report %s but could not remove its %s post %s: %s",
            report_id,
            publication.provider,
            publication.external_post_id,
            error_text,
        )
    session.flush()
    return failures


def _legacy_social_report_pairs(session: Session) -> list[tuple[str, str]]:
    pairs = {
        (str(report_id), str(report_type).lower())
        for report_id, report_type in session.execute(
            select(SocialPublication.report_id, SocialPublication.report_type).distinct()
        ).all()
    }
    pairs.update(
        (str(report_id), "lost")
        for report_id in session.scalars(
            select(LostItem.id).where(
                or_(LostItem.social_share_consent.is_(True), LostItem.social_poster_url.is_not(None))
            )
        ).all()
    )
    pairs.update(
        (str(report_id), "found")
        for report_id in session.scalars(
            select(FoundItem.id).where(
                or_(FoundItem.social_share_consent.is_(True), FoundItem.social_poster_url.is_not(None))
            )
        ).all()
    )
    return sorted(pairs)


def remove_legacy_social_posts(
    session: Session,
    *,
    limit: int = 100,
) -> dict[str, object]:
    pending_pairs = _legacy_social_report_pairs(session)
    batch = pending_pairs[:limit]
    accounts = session.scalars(
        select(SocialAccount).where(SocialAccount.provider.in_(PROVIDER_NAMES))
    ).all()
    failures: list[dict[str, str]] = []
    removed = 0

    for report_id, report_type in batch:
        report_failures = remove_report_publications(session, report_id, report_type)
        if not report_failures and not accounts:
            report_failures.append(
                "No connected Meta account is available to verify and remove legacy posts."
            )
        if not report_failures:
            for account in accounts:
                post_ids, feed_complete = _find_report_publication_post_ids(account, report_id)
                if not feed_complete:
                    report_failures.append(
                        f"{account.provider} feed could not be fully checked for report {report_id}."
                    )
                    continue
                removal_error = _remove_meta_posts_for_report_link(
                    account,
                    account.provider,
                    report_id,
                    post_ids,
                )
                if removal_error:
                    report_failures.append(removal_error)

        if report_failures:
            failures.extend(
                {"report_id": report_id, "error": failure}
                for failure in report_failures
            )
            continue

        model = LostItem if report_type == "lost" else FoundItem
        report = session.get(model, report_id)
        if report is not None:
            report.social_share_consent = False
        removed += 1

    session.commit()
    remaining = len(_legacy_social_report_pairs(session))
    return {
        "processed": len(batch),
        "removed": removed,
        "remaining": remaining,
        "failures": failures,
    }


def _meta_post_is_unavailable(account: SocialAccount, external_post_id: str) -> bool:
    post_ids, feed_complete = _list_external_post_ids(account, {external_post_id})
    if feed_complete:
        return external_post_id not in post_ids
    return _check_external_post(account, external_post_id) == "unavailable"


def _find_report_publication_post_ids(
    account: SocialAccount,
    report_id: str,
) -> tuple[list[str], bool]:
    edge = "published_posts" if account.provider == "facebook" else "media"
    fields = "id,message" if account.provider == "facebook" else "id,caption"
    report_url = fendly_report_url(report_id)
    access_token = _decrypt_token(account.access_token_encrypted)
    post_ids: list[str] = []
    after: str | None = None
    seen_cursors: set[str] = set()

    for _ in range(100):
        params: dict[str, str | int] = {
            "fields": fields,
            "limit": 100,
            "access_token": access_token,
        }
        if after is not None:
            params["after"] = after
        try:
            response = httpx.get(
                _graph_url(f"{quote(account.account_id, safe='')}/{edge}"),
                params=params,
                timeout=15,
            )
            payload = _response_object(response.json())
        except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
            logger.warning(
                "Could not search Meta %s feed for report %s: %s",
                account.provider,
                report_id,
                type(error).__name__,
            )
            return post_ids, False
        if response.is_error or "error" in payload:
            logger.warning(
                "Meta %s feed search for report %s was inconclusive (HTTP %s)",
                account.provider,
                report_id,
                response.status_code,
            )
            return post_ids, False

        try:
            entries = _response_array(payload.get("data"))
        except (RuntimeError, TypeError):
            return post_ids, False
        for entry in entries:
            post_id = entry.get("id")
            message = entry.get("message") if account.provider == "facebook" else entry.get("caption")
            if (
                isinstance(post_id, str)
                and post_id
                and isinstance(message, str)
                and report_url in message
            ):
                post_ids.append(post_id)

        paging = payload.get("paging")
        if not isinstance(paging, dict) or not paging.get("next"):
            return post_ids, True
        cursors = paging.get("cursors")
        next_cursor = cursors.get("after") if isinstance(cursors, dict) else None
        if not isinstance(next_cursor, str) or not next_cursor or next_cursor in seen_cursors:
            return post_ids, False
        seen_cursors.add(next_cursor)
        after = next_cursor

    logger.warning("Meta feed search exceeded the pagination safety limit")
    return post_ids, False


def _remove_meta_posts_for_report_link(
    account: SocialAccount,
    provider: str,
    report_id: str,
    post_ids: list[str],
) -> str | None:
    failures: list[str] = []
    for post_id in post_ids:
        try:
            _delete_meta_post(account, provider, post_id)
        except (HTTPException, httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
            if not _meta_post_is_unavailable(account, post_id):
                failures.append(
                    f"{provider} post {post_id} could not be removed: {str(error)[:250]}"
                )
    return " ".join(failures) if failures else None


def process_due_publications() -> None:
    with SessionLocal() as session:
        session.execute(
            update(SocialPublication)
            .where(SocialPublication.status.in_(("pending", "processing")))
            .values(
                status="cancelled",
                next_attempt_at=0,
                last_error="Automatic social-media publication has been disabled.",
            )
        )
        session.commit()


def _delete_meta_post(
    account: SocialAccount,
    provider: str,
    external_post_id: str,
) -> None:
    encrypted_token = account.access_token_encrypted
    if provider == "instagram":
        encrypted_token = account.deletion_access_token_encrypted or ""
        if not encrypted_token:
            raise RuntimeError(
                "Instagram deletion needs a Facebook User access token, which is "
                "missing from this saved Meta connection. In Admin Workspace, open "
                "Meta accounts and select Reconnect Meta to authorize it again."
            )
    response = httpx.delete(
        _graph_url(quote(external_post_id, safe="")),
        params={"access_token": _decrypt_token(encrypted_token)},
        timeout=15,
    )
    try:
        result = _provider_response(response, f"{provider} post deletion")
    except RuntimeError as error:
        error_text = str(error)
        if provider == "facebook" and "subcode 33" in error_text.casefold():
            raise RuntimeError(
                f"{error_text}. Check that the connected user has the "
                "pages_manage_posts permission and a content-management task on "
                "the selected Page. Also verify the saved ID is the Page post ID "
                "(not a photo ID) and belongs to that Page."
            ) from error
        if provider == "instagram" and (
            "code 200" in error_text.casefold()
            or "permission" in error_text.casefold()
        ):
            raise RuntimeError(
                f"{error_text}. Meta did not grant instagram_manage_contents. If "
                "the permission is missing from the saved login configuration or "
                "has not been approved for this app, update the Meta configuration, "
                "approve it if required, and reconnect."
            ) from error
        raise
    if result.get("success") is not True:
        raise RuntimeError(f"Meta did not confirm deletion of the {provider} post")


def _publication_access_token(account: SocialAccount) -> str | None:
    if account.provider == "instagram":
        encrypted_token = account.deletion_access_token_encrypted
        if not encrypted_token:
            return None
        return _decrypt_token(encrypted_token)
    return _decrypt_token(account.access_token_encrypted)


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
    deletion_access_token: str | None = None,
) -> None:
    account = session.get(SocialAccount, provider)
    values: dict[str, object] = {
        "account_id": account_id,
        "account_name": account_name[:160],
        "access_token_encrypted": _encrypt_token(access_token),
        "deletion_access_token_encrypted": (
            _encrypt_token(deletion_access_token)
            if deletion_access_token is not None
            else None
        ),
        "updated_at": datetime.now(timezone.utc),
    }
    if account is None:
        account = SocialAccount(provider=provider, **values)
        session.add(account)
    else:
        for key, value in values.items():
            setattr(account, key, value)


def _debug_meta_token_scopes(
    user_token: str,
    app_id: str,
    app_secret: str,
) -> set[str]:
    debug_result = _provider_response(
        httpx.get(
            "https://graph.facebook.com/v20.0/debug_token",
            params={
                "input_token": user_token,
                "access_token": f"{app_id}|{app_secret}",
            },
            timeout=20,
        ),
        "Meta token permission verification",
    )
    token_data = _response_object(debug_result.get("data"))
    raw_scopes = token_data.get("scopes")
    granted = (
        {scope for scope in raw_scopes if isinstance(scope, str)}
        if isinstance(raw_scopes, list)
        else set()
    )
    logger.info("Meta OAuth token granted scopes: %s", ", ".join(sorted(granted)))
    return granted


def _validate_meta_deletion_permissions(granted: set[str], has_instagram: bool) -> bool:
    if "pages_manage_posts" not in granted:
        logger.warning(
            "Meta OAuth token is missing pages_manage_posts; Facebook post deletion "
            "will not work."
        )
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            "Meta authorization is missing pages_manage_posts. Add it to the "
            "Meta Login for Business configuration, "
            "complete App Review/Advanced Access if Meta requires it, then reconnect.",
        )
    if has_instagram and "instagram_manage_contents" not in granted:
        logger.warning(
            "Meta OAuth token is missing instagram_manage_contents; Instagram media "
            "deletion is unavailable for this connection. Check the Login for Business "
            "configuration and required app approval."
        )
        return False
    if has_instagram and "instagram_basic" not in granted:
        logger.warning(
            "Meta OAuth token is missing instagram_basic; Instagram media lookup "
            "and deletion cannot be confirmed."
        )
        return False
    if has_instagram and "instagram_content_publish" not in granted:
        logger.warning(
            "Meta OAuth token is missing instagram_content_publish; Instagram "
            "publishing is unavailable for this connection."
        )
        return False
    return True


def _finish_meta_oauth(session: Session, code: str, state: str) -> bool:
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
    token_result = _provider_response(response, "OAuth token exchange")
    user_token = token_result.get("access_token")
    if not isinstance(user_token, str) or not user_token:
        raise RuntimeError("Meta did not return an access token")
    long_lived_result = _provider_response(
        httpx.get(
            _graph_url("oauth/access_token"),
            params={
                "grant_type": "fb_exchange_token",
                "client_id": app_id,
                "client_secret": app_secret,
                "fb_exchange_token": user_token,
            },
            timeout=20,
        ),
        "Long-lived OAuth token exchange",
    )
    long_lived_user_token = long_lived_result.get("access_token")
    if not isinstance(long_lived_user_token, str) or not long_lived_user_token:
        raise RuntimeError("Meta did not return a long-lived user access token")
    user_token = long_lived_user_token
    granted_scopes = _debug_meta_token_scopes(user_token, app_id, app_secret)
    pages_result = _provider_response(
        httpx.get(
            _graph_url("me/accounts"),
            params={
                "fields": "id,name,access_token,instagram_business_account{id,username}",
                "access_token": user_token,
            },
            timeout=20,
        ),
        "Facebook Page discovery",
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
    instagram_value = page.get("instagram_business_account")
    instagram: dict[str, object] | None = (
        _response_object(instagram_value) if instagram_value is not None else None
    )
    instagram_id = instagram.get("id") if instagram is not None else None
    instagram_deletion_available = _validate_meta_deletion_permissions(
        granted_scopes,
        isinstance(instagram_id, str),
    )
    page_name = str(page.get("name") or "Fendly Facebook Page")
    _upsert_social_account(session, "facebook", page_id, page_name, page_token)
    if isinstance(instagram_id, str) and instagram is not None:
        _upsert_social_account(
            session,
            "instagram",
            instagram_id,
            str(instagram.get("username") or "Fendly Instagram"),
            page_token,
            deletion_access_token=user_token,
        )
    else:
        session.execute(delete(SocialAccount).where(SocialAccount.provider == "instagram"))
    session.commit()
    return instagram_deletion_available


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
    report: LostItem | FoundItem | None = None,
    published_image_url: str | None = None,
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
        "poster_url": report.social_poster_url if report is not None else None,
        "published_image_url": published_image_url,
        "report_image_url": report.image_url if report is not None else None,
        "report_title": (
            (report.title_en or report.title) if report is not None else None
        ),
        "report_category": (
            (report.category_en or report.category) if report is not None else None
        ),
    }


def _publication_report(
    session: Session,
    publication: SocialPublication,
) -> LostItem | FoundItem | None:
    preferred_model = LostItem if publication.report_type.casefold() == "lost" else FoundItem
    report = session.get(preferred_model, publication.report_id)
    if report is not None:
        return report
    fallback_model = FoundItem if preferred_model is LostItem else LostItem
    return session.get(fallback_model, publication.report_id)


def _published_post_image_url(
    account: SocialAccount | None,
    publication: SocialPublication,
    report: LostItem | FoundItem | None = None,
) -> str | None:
    if (
        account is None
        or not publication.external_post_id
        or publication.status != "published"
        or (report is not None and report.social_poster_url)
    ):
        return None

    image_fields = (
        "full_picture,picture"
        if publication.provider == "facebook"
        else "media_url,thumbnail_url"
    )
    try:
        response = httpx.get(
            _graph_url(quote(publication.external_post_id, safe="")),
            params={
                "fields": image_fields,
                "access_token": _decrypt_token(account.access_token_encrypted),
            },
            timeout=15,
        )
        if response.is_error:
            logger.warning(
                "Could not retrieve image for published %s post (HTTP %s)",
                publication.provider,
                response.status_code,
            )
            return None
        payload = _response_object(response.json())
    except HTTPException as error:
        logger.warning(
            "Could not retrieve image for published %s post: %s",
            publication.provider,
            error.detail,
        )
        return None
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
        logger.warning(
            "Could not retrieve image for published %s post: %s",
            publication.provider,
            type(error).__name__,
        )
        return None

    field_names = (
        ("full_picture", "picture")
        if publication.provider == "facebook"
        else ("media_url", "thumbnail_url")
    )
    for field_name in field_names:
        image_url = payload.get(field_name)
        if isinstance(image_url, str) and image_url.startswith("https://"):
            return image_url
    return None


def _publication_response(
    session: Session,
    publication: SocialPublication,
    accounts: dict[str, SocialAccount],
    platform_status: str = "not_checked",
) -> dict[str, str | int | None]:
    report = _publication_report(session, publication)
    return _publication_summary(
        publication,
        platform_status,
        report,
        _published_post_image_url(
            accounts.get(publication.provider),
            publication,
            report,
        ),
    )


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
        access_token = _publication_access_token(account)
        if access_token is None:
            logger.warning(
                "Meta %s post lookup skipped because its access token is missing",
                account.provider,
            )
            return "check_failed"
        response = httpx.get(
            _graph_url(quote(external_post_id, safe="")),
            params={
                "fields": "id",
                "access_token": access_token,
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
    accounts = {
        account.provider: account
        for account in session.scalars(
            select(SocialAccount).where(SocialAccount.provider.in_(PROVIDER_NAMES))
        ).all()
    }
    return [
        _publication_response(session, publication, accounts)
        for publication in publications
    ]


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
        refreshed.append(
            _publication_response(
                session,
                publication,
                accounts,
                platform_status,
            )
        )
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
        "scope": ",".join(META_OAUTH_SCOPES),
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
        instagram_deletion_available = _finish_meta_oauth(session, code, state)
    except HTTPException:
        raise
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as exception:
        diagnostic = str(exception) if isinstance(exception, RuntimeError) else type(exception).__name__
        logger.error("Social OAuth callback failed: provider=meta error=%s", diagnostic)
        raise HTTPException(
            status.HTTP_502_BAD_GATEWAY,
            "The social platform could not complete authorization. Check configuration and reconnect.",
        ) from exception
    if not instagram_deletion_available:
        return HTMLResponse(
            "<!doctype html><title>Fendly social account connected</title>"
            "<p>Fendly connected successfully, but Meta did not grant "
            "all required Instagram permissions, including instagram_content_publish, "
            "instagram_basic, and instagram_manage_contents. Check the Login for "
            "Business configuration and app approval, then reconnect. Instagram "
            "publishing or post deletion may remain unavailable until Meta grants "
            "the missing permissions. You may close this window.</p>"
        )
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
