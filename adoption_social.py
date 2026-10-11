import base64
import hashlib
import logging
import os
import re
from collections.abc import Callable
from urllib.parse import urlsplit

from cryptography.fernet import Fernet, InvalidToken
import httpx

from models import AdoptionListing, SocialAccount


logger = logging.getLogger(__name__)
DEFAULT_GRAPH_VERSION = "v23.0"
GRAPH_VERSION_PATTERN = re.compile(r"^v\d{1,2}\.0$")
GRAPH_ID_PATTERN = re.compile(r"^\d+$")


def _graph_configuration() -> tuple[str, str]:
    download_url = os.getenv(
        "APP_DOWNLOAD_URL",
        "https://play.google.com/store/apps/details?id=com.example.fendly",
    ).strip()
    graph_version = os.getenv("META_GRAPH_API_VERSION", DEFAULT_GRAPH_VERSION).strip()
    parsed_download_url = urlsplit(download_url)
    if parsed_download_url.scheme != "https" or not parsed_download_url.netloc:
        raise ValueError("APP_DOWNLOAD_URL must be an HTTPS URL")
    if not GRAPH_VERSION_PATTERN.fullmatch(graph_version):
        raise ValueError("META_GRAPH_API_VERSION must look like v23.0")
    return download_url, graph_version


def _decrypt_connected_token(encrypted_token: str) -> str:
    secret = os.getenv("APP_SECRET_KEY", "")
    if len(secret) < 32:
        raise RuntimeError("APP_SECRET_KEY is not configured for the saved Meta connection")
    key = base64.urlsafe_b64encode(hashlib.sha256(secret.encode("utf-8")).digest())
    try:
        return Fernet(key).decrypt(encrypted_token.encode("ascii")).decode("utf-8")
    except (InvalidToken, UnicodeError, ValueError) as exc:
        raise RuntimeError("The saved Meta connection is invalid; reconnect Meta") from exc


def _connection_credentials(
    provider: str,
    account: SocialAccount | None,
) -> tuple[str, str] | None:
    id_environment_name = (
        "META_FACEBOOK_PAGE_ID" if provider == "facebook" else "META_INSTAGRAM_ACCOUNT_ID"
    )
    direct_id = os.getenv(id_environment_name, "").strip()
    if provider == "facebook" and not direct_id:
        direct_id = os.getenv("META_PAGE_ID", "").strip()
    direct_token = os.getenv("META_PAGE_ACCESS_TOKEN", "").strip()
    if direct_token:
        if provider == "facebook" and not direct_id and account is not None:
            direct_id = account.account_id
        if provider == "facebook" and direct_id:
            if not GRAPH_ID_PATTERN.fullmatch(direct_id):
                raise RuntimeError(
                    f"{id_environment_name} must be the numeric Facebook Page ID, "
                    "not an access token"
                )
            return direct_id, direct_token
        if provider == "instagram" and direct_id and GRAPH_ID_PATTERN.fullmatch(direct_id):
            return direct_id, direct_token
        if provider == "instagram":
            page_id = (
                os.getenv("META_FACEBOOK_PAGE_ID", "").strip()
                or os.getenv("META_PAGE_ID", "").strip()
            )
            if page_id and GRAPH_ID_PATTERN.fullmatch(page_id):
                return _instagram_account_for_page(page_id, direct_token), direct_token
            if account is not None and GRAPH_ID_PATTERN.fullmatch(account.account_id):
                return account.account_id, direct_token
            if direct_id:
                raise RuntimeError(
                    "META_INSTAGRAM_ACCOUNT_ID must be the numeric Instagram account ID, "
                    "not an access token; configure META_PAGE_ID to discover the linked account"
                )
        if direct_id:
            raise RuntimeError(f"{id_environment_name} is required as a numeric ID")
        if account is None or not account.account_id:
            raise RuntimeError(
                f"{id_environment_name} or a saved Meta account is required with "
                "META_PAGE_ACCESS_TOKEN"
            )
    if account is not None and account.account_id and account.access_token_encrypted:
        if not GRAPH_ID_PATTERN.fullmatch(account.account_id):
            raise RuntimeError(
                f"The saved {provider.title()} account ID is invalid; reconnect Meta"
            )
        return account.account_id, _decrypt_connected_token(account.access_token_encrypted)
    if direct_id and not direct_token:
        raise RuntimeError(
            "META_PAGE_ACCESS_TOKEN is required with the configured Meta account ID"
        )
    return None


def _redact_meta_error(message: str, access_token: str) -> str:
    if access_token:
        message = message.replace(access_token, "[redacted Meta access token]")
    return re.sub(r"\bEAA[A-Za-z0-9]{20,}\b", "[redacted Meta access token]", message)


def _instagram_account_for_page(page_id: str, access_token: str) -> str:
    graph_version = os.getenv("META_GRAPH_API_VERSION", DEFAULT_GRAPH_VERSION).strip()
    endpoint = f"https://graph.facebook.com/{graph_version}/{page_id}"
    try:
        response = httpx.get(
            endpoint,
            params={
                "fields": "instagram_business_account{id}",
                "access_token": access_token,
            },
            timeout=20.0,
        )
        response.raise_for_status()
        result = response.json()
    except httpx.HTTPStatusError as exc:
        try:
            graph_error = exc.response.json().get("error", {}).get("message", "")
        except (ValueError, AttributeError):
            graph_error = ""
        message = graph_error or f"Meta Graph API returned HTTP {exc.response.status_code}"
        raise RuntimeError(_redact_meta_error(message[:400], access_token)) from exc
    except (httpx.HTTPError, ValueError) as exc:
        raise RuntimeError("Could not discover the Instagram account linked to META_PAGE_ID") from exc

    instagram = result.get("instagram_business_account") if isinstance(result, dict) else None
    instagram_id = instagram.get("id") if isinstance(instagram, dict) else None
    if not isinstance(instagram_id, str) or not GRAPH_ID_PATTERN.fullmatch(instagram_id):
        raise RuntimeError(
            "META_PAGE_ID has no linked Instagram professional account; link one to the "
            "Facebook Page and grant instagram_basic and instagram_content_publish"
        )
    return instagram_id


def _graph_post(endpoint: str, payload: dict[str, str]) -> str:
    try:
        response = httpx.post(endpoint, data=payload, timeout=30.0)
        response.raise_for_status()
        result = response.json()
    except httpx.HTTPStatusError as exc:
        try:
            graph_error = exc.response.json().get("error", {}).get("message", "")
        except (ValueError, AttributeError):
            graph_error = ""
        message = graph_error or f"Meta Graph API returned HTTP {exc.response.status_code}"
        access_token = payload.get("access_token", "")
        message = _redact_meta_error(message, access_token)
        if "publish_actions" in message.casefold():
            message = (
                "Meta rejected Facebook publishing because publish_actions is deprecated. "
                "Use a Facebook Page access token with pages_manage_posts granted to the "
                "Meta app and the Page task."
            )
        raise RuntimeError(message[:400]) from exc
    except (httpx.HTTPError, ValueError) as exc:
        raise RuntimeError("Meta Graph API request failed") from exc
    identifier = result.get("id") if isinstance(result, dict) else None
    if not isinstance(identifier, str) or not identifier:
        raise RuntimeError("Meta Graph API did not return a publication ID")
    return identifier


def publish_adoption_listing(
    listing: AdoptionListing,
    on_publication: Callable[[str, str], None],
    *,
    facebook_account: SocialAccount | None = None,
    instagram_account: SocialAccount | None = None,
) -> tuple[dict[str, str], list[str]]:
    try:
        download_url, graph_version = _graph_configuration()
    except ValueError as exc:
        return {}, [str(exc)]
    if not listing.images:
        return {}, ["Add at least one public pet photo before requesting social posting."]

    image_url = listing.images[0]
    caption = (
        f"{listing.pet_name} is looking for a loving home!\n"
        f"{listing.species.title()}"
        f"{f' · {listing.breed}' if listing.breed else ''} · "
        f"{listing.age_months} months old\n"
        f"Location: {listing.location_city}\n\n"
        f"{listing.description}\n\n"
        f"Learn more and apply in Fendly: {download_url}"
    )
    if len(caption) > 2000:
        caption = caption[:1997].rstrip() + "..."

    publications: dict[str, str] = {}
    errors = []
    if not listing.facebook_post_id:
        try:
            credentials = _connection_credentials("facebook", facebook_account)
            if credentials is None:
                raise RuntimeError("No connected Facebook Page is available for adoption posts")
            page_id, access_token = credentials
            if not GRAPH_ID_PATTERN.fullmatch(page_id):
                raise RuntimeError(
                    "The Facebook Page ID must be numeric; do not enter an access token as the ID"
                )
            page_endpoint = f"https://graph.facebook.com/{graph_version}/{page_id}/photos"
            publications["facebook"] = _graph_post(
                page_endpoint,
                {
                    "url": image_url,
                    "caption": caption,
                    "access_token": access_token,
                },
            )
            on_publication("facebook", publications["facebook"])
        except RuntimeError as exc:
            errors.append(f"Facebook: {exc}")
            logger.warning(
                "Adoption listing %s Facebook publishing failed: %s",
                listing.id,
                str(exc)[:400],
            )
    if not listing.instagram_post_id:
        try:
            credentials = _connection_credentials("instagram", instagram_account)
            if credentials is None:
                raise RuntimeError("No connected Instagram account is available for adoption posts")
            instagram_id, access_token = credentials
            if not GRAPH_ID_PATTERN.fullmatch(instagram_id):
                raise RuntimeError(
                    "The Instagram account ID must be numeric; do not enter an access token as the ID"
                )
            instagram_media_endpoint = (
                f"https://graph.facebook.com/{graph_version}/{instagram_id}/media"
            )
            container_id = _graph_post(
                instagram_media_endpoint,
                {
                    "image_url": image_url,
                    "caption": caption,
                    "access_token": access_token,
                },
            )
            publications["instagram"] = _graph_post(
                f"https://graph.facebook.com/{graph_version}/{instagram_id}/media_publish",
                {
                    "creation_id": container_id,
                    "access_token": access_token,
                },
            )
            on_publication("instagram", publications["instagram"])
        except RuntimeError as exc:
            errors.append(f"Instagram: {exc}")
            logger.warning(
                "Adoption listing %s Instagram publishing failed: %s",
                listing.id,
                str(exc)[:400],
            )

    return publications, errors
