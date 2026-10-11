import base64
import hashlib
import logging
import os
import re
from typing import cast
from urllib.parse import quote

import httpx
from cryptography.fernet import Fernet, InvalidToken
from sqlalchemy import or_, select, update
from sqlalchemy.orm import Session

from app_links import fendly_report_url
from models import FoundItem, LostItem, SocialAccount, SocialPublication


logger = logging.getLogger(__name__)
PROVIDER_NAMES = ("facebook", "instagram")


def _decrypt_token(token: str) -> str:
    secret = os.getenv("APP_SECRET_KEY", "")
    if len(secret) < 32:
        raise RuntimeError("APP_SECRET_KEY must contain at least 32 characters")
    key = base64.urlsafe_b64encode(hashlib.sha256(secret.encode("utf-8")).digest())
    try:
        return Fernet(key).decrypt(token.encode("ascii")).decode("utf-8")
    except (InvalidToken, UnicodeError, ValueError) as error:
        raise RuntimeError("Stored Meta authorization is invalid") from error


def _graph_url(path: str) -> str:
    version = os.getenv("META_GRAPH_API_VERSION", "").strip()
    if not re.fullmatch(r"v\d+\.\d+", version):
        raise RuntimeError("META_GRAPH_API_VERSION is not configured correctly")
    return f"https://graph.facebook.com/{version}/{path.lstrip('/')}"


def _response_object(value: object) -> dict[str, object]:
    if not isinstance(value, dict):
        raise RuntimeError("Meta returned an invalid response")
    return cast(dict[str, object], value)


def _response_array(value: object) -> list[dict[str, object]]:
    if not isinstance(value, list):
        raise RuntimeError("Meta returned an invalid feed")
    return [_response_object(entry) for entry in cast(list[object], value)]


def _provider_response(response: httpx.Response, operation: str) -> dict[str, object]:
    try:
        payload = _response_object(response.json())
    except (ValueError, RuntimeError) as error:
        raise RuntimeError("Meta returned an invalid response") from error
    if response.is_error or "error" in payload:
        error = payload.get("error")
        error_body = cast(dict[str, object], error) if isinstance(error, dict) else {}
        message = error_body.get("message")
        detail = f": {message}" if isinstance(message, str) else ""
        raise RuntimeError(f"Meta {operation} failed (HTTP {response.status_code}){detail}")
    return payload


def _find_report_publication_post_ids(
    account: SocialAccount,
    report_id: str,
) -> tuple[list[str], bool]:
    edge = "published_posts" if account.provider == "facebook" else "media"
    fields = "id,message" if account.provider == "facebook" else "id,caption"
    report_url = fendly_report_url(report_id)
    token = _decrypt_token(account.access_token_encrypted)
    post_ids: list[str] = []
    after: str | None = None
    seen_cursors: set[str] = set()

    for _ in range(100):
        params: dict[str, str | int] = {
            "fields": fields,
            "limit": 100,
            "access_token": token,
        }
        if after:
            params["after"] = after
        try:
            response = httpx.get(
                _graph_url(f"{quote(account.account_id, safe='')}/{edge}"),
                params=params,
                timeout=15,
            )
            payload = _provider_response(response, "legacy-post search")
            entries = _response_array(payload.get("data"))
        except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
            logger.warning("Legacy Meta post search failed: %s", type(error).__name__)
            return post_ids, False

        for entry in entries:
            post_id = entry.get("id")
            text = entry.get("message") if account.provider == "facebook" else entry.get("caption")
            if isinstance(post_id, str) and isinstance(text, str) and report_url in text:
                post_ids.append(post_id)
        paging_value = payload.get("paging")
        if not isinstance(paging_value, dict):
            return post_ids, True
        paging = cast(dict[str, object], paging_value)
        if not paging.get("next"):
            return post_ids, True
        cursors_value = paging.get("cursors")
        cursors = (
            cast(dict[str, object], cursors_value)
            if isinstance(cursors_value, dict)
            else {}
        )
        next_cursor = cursors.get("after")
        if not isinstance(next_cursor, str) or not next_cursor or next_cursor in seen_cursors:
            return post_ids, False
        seen_cursors.add(next_cursor)
        after = next_cursor
    return post_ids, False


def _delete_meta_post(
    account: SocialAccount,
    provider: str,
    external_post_id: str,
) -> None:
    encrypted_token = account.access_token_encrypted
    if provider == "instagram":
        encrypted_token = account.deletion_access_token_encrypted or ""
        if not encrypted_token:
            raise RuntimeError("Instagram deletion authorization is unavailable")
    response = httpx.delete(
        _graph_url(quote(external_post_id, safe="")),
        params={"access_token": _decrypt_token(encrypted_token)},
        timeout=15,
    )
    result = _provider_response(response, f"{provider} post deletion")
    if result.get("success") is not True:
        raise RuntimeError(f"Meta did not confirm deletion of the {provider} post")


def _delete_report_posts_for_link(
    account: SocialAccount,
    post_ids: list[str],
) -> list[str]:
    failures: list[str] = []
    for post_id in post_ids:
        try:
            _delete_meta_post(account, account.provider, post_id)
        except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
            failures.append(
                f"{account.provider} post {post_id} could not be removed: {str(error)[:250]}"
            )
    return failures


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
    accounts = {
        account.provider: account
        for account in session.scalars(
            select(SocialAccount).where(SocialAccount.provider.in_(PROVIDER_NAMES))
        ).all()
    }
    failures: list[str] = []
    for publication in publications:
        if publication.status == "removed":
            session.delete(publication)
            continue
        account = accounts.get(publication.provider)
        if publication.external_post_id:
            if account is None:
                failures.append(f"{publication.provider} deletion account is unavailable.")
                continue
            try:
                _delete_meta_post(
                    account,
                    publication.provider,
                    publication.external_post_id,
                )
            except (httpx.HTTPError, RuntimeError, ValueError, TypeError) as error:
                failures.append(
                    f"{publication.provider} post could not be removed: {str(error)[:250]}"
                )
                continue
        elif publication.status == "processing":
            failures.append(
                f"{publication.provider} publication is still processing; retry shortly."
            )
            continue
        session.delete(publication)

    model = LostItem if report_type == "lost" else FoundItem
    report = session.get(model, report_id)
    needs_legacy_search = bool(publications) or bool(
        report
        and (report.social_share_consent or report.social_poster_url)
    )
    if needs_legacy_search:
        for provider, account in accounts.items():
            post_ids, complete = _find_report_publication_post_ids(account, report_id)
            if not complete:
                failures.append(
                    f"{provider} feed could not be completely checked for legacy posts."
                )
            else:
                failures.extend(_delete_report_posts_for_link(account, post_ids))
    session.flush()
    return failures


def _legacy_social_report_pairs(session: Session) -> list[tuple[str, str]]:
    pairs = {
        (str(report_id), str(report_type).lower())
        for report_id, report_type in session.execute(
                select(
                    SocialPublication.report_id,
                    SocialPublication.report_type,
                )
                .distinct()
                .where(SocialPublication.status != "removed")
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
    batch = _legacy_social_report_pairs(session)[:limit]
    accounts = session.scalars(
        select(SocialAccount).where(SocialAccount.provider.in_(PROVIDER_NAMES))
    ).all()
    failures: list[dict[str, str]] = []
    removed = 0

    for report_id, report_type in batch:
        report_failures = remove_report_publications(session, report_id, report_type)
        if not accounts:
            report_failures.append(
                "No saved Meta deletion account is available to check for legacy posts."
            )
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

    session.execute(
        update(SocialPublication)
        .where(SocialPublication.status.in_(("pending", "processing")))
        .values(status="cancelled", next_attempt_at=0)
    )
    session.commit()
    return {
        "processed": len(batch),
        "removed": removed,
        "remaining": len(_legacy_social_report_pairs(session)),
        "failures": failures,
    }
