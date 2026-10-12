import base64
import hashlib
import logging
import os
import re
import time
from collections.abc import Callable
from urllib.parse import quote, urlsplit

from cryptography.fernet import Fernet, InvalidToken
import httpx

from models import AdoptionListing, SocialAccount


logger = logging.getLogger(__name__)
DEFAULT_GRAPH_VERSION = "v23.0"
GRAPH_VERSION_PATTERN = re.compile(r"^v\d{1,2}\.0$")
GRAPH_ID_PATTERN = re.compile(r"^\d+$")
INSTAGRAM_CONTAINER_MAX_ATTEMPTS = 15
INSTAGRAM_CONTAINER_POLL_INTERVAL_SECONDS = 2


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


def _meta_error_message(response: httpx.Response, access_token: str) -> str:
    try:
        error = response.json().get("error", {})
    except (ValueError, AttributeError):
        error = {}
    if not isinstance(error, dict):
        error = {}
    message = error.get("message", "")
    if not isinstance(message, str):
        message = ""
    if error.get("code") == 190 or any(
        phrase in message.casefold()
        for phrase in ("session has expired", "access token has expired", "error validating access token")
    ):
        return (
            "Meta rejected the expired or revoked access token. Replace the server-side "
            "Meta Page access token with a newly generated token that has Facebook Page "
            "and Instagram publishing permissions, then retry publishing."
        )
    return _redact_meta_error(
        message or f"Meta Graph API returned HTTP {response.status_code}",
        access_token,
    )[:400]


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
        message = _meta_error_message(exc.response, access_token)
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


def _graph_post(
    endpoint: str,
    payload: dict[str, str],
    *,
    prefer_page_post_id: bool = False,
) -> str:
    try:
        response = httpx.post(endpoint, data=payload, timeout=30.0)
        response.raise_for_status()
        result = response.json()
    except httpx.HTTPStatusError as exc:
        access_token = payload.get("access_token", "")
        message = _meta_error_message(exc.response, access_token)
        if "publish_actions" in message.casefold():
            message = (
                "Meta rejected Facebook publishing because publish_actions is deprecated. "
                "Use a Facebook Page access token with pages_manage_posts granted to the "
                "Meta app and the Page task."
            )
        raise RuntimeError(message[:400]) from exc
    except (httpx.HTTPError, ValueError) as exc:
        raise RuntimeError("Meta Graph API request failed") from exc
    identifier = None
    if isinstance(result, dict):
        identifier = result.get("post_id") if prefer_page_post_id else None
        identifier = identifier or result.get("id")
    if not isinstance(identifier, str) or not identifier:
        raise RuntimeError("Meta Graph API did not return a publication ID")
    return identifier


def _wait_for_instagram_container(
    container_id: str,
    access_token: str,
    graph_version: str,
) -> None:
    endpoint = (
        f"https://graph.facebook.com/{graph_version}/{quote(container_id, safe='')}"
    )
    for attempt in range(INSTAGRAM_CONTAINER_MAX_ATTEMPTS):
        try:
            response = httpx.get(
                endpoint,
                params={
                    "fields": "status_code,status",
                    "access_token": access_token,
                },
                timeout=30.0,
            )
            response.raise_for_status()
            result = response.json()
        except httpx.HTTPStatusError as exc:
            message = _meta_error_message(exc.response, access_token)
            raise RuntimeError(
                _redact_meta_error(message[:400], access_token)
            ) from exc
        except (httpx.HTTPError, ValueError) as exc:
            raise RuntimeError("Could not check Instagram media processing status") from exc

        status_code = result.get("status_code") if isinstance(result, dict) else None
        if status_code == "FINISHED":
            return
        if status_code in {"ERROR", "EXPIRED"}:
            status_message = result.get("status", "") if isinstance(result, dict) else ""
            detail = f": {status_message}" if isinstance(status_message, str) and status_message else ""
            raise RuntimeError(
                f"Instagram media processing ended with status {status_code}{detail}"
            )
        if status_code != "IN_PROGRESS":
            raise RuntimeError(
                "Meta did not return a recognized Instagram media processing status"
            )
        if attempt < INSTAGRAM_CONTAINER_MAX_ATTEMPTS - 1:
            time.sleep(INSTAGRAM_CONTAINER_POLL_INTERVAL_SECONDS)

    raise RuntimeError(
        "Instagram media is still processing; retry publishing in a few moments"
    )


def _graph_delete(endpoint: str, access_token: str) -> None:
    try:
        response = httpx.delete(
            endpoint,
            params={"access_token": access_token},
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
        raise RuntimeError("Meta Graph API deletion request failed") from exc
    if not isinstance(result, dict) or result.get("success") is not True:
        raise RuntimeError("Meta did not confirm removal of the published post")


def delete_adoption_publications(
    listing: AdoptionListing,
    on_deletion: Callable[[str], None],
    *,
    facebook_account: SocialAccount | None = None,
    instagram_account: SocialAccount | None = None,
) -> list[str]:
    errors = []
    try:
        _, graph_version = _graph_configuration()
    except ValueError as exc:
        return [str(exc)]
    for provider, post_id, account in (
        ("facebook", listing.facebook_post_id, facebook_account),
        ("instagram", listing.instagram_post_id, instagram_account),
    ):
        if not post_id:
            continue
        try:
            credentials = _connection_credentials(provider, account)
            if credentials is None:
                raise RuntimeError(f"No connected {provider.title()} account is available")
            account_id, access_token = credentials
            if (
                provider == "instagram"
                and account is not None
                and account.deletion_access_token_encrypted
            ):
                access_token = _decrypt_connected_token(account.deletion_access_token_encrypted)
            if not GRAPH_ID_PATTERN.fullmatch(account_id):
                raise RuntimeError(f"The saved {provider.title()} account ID is invalid")
            _graph_delete(
                f"https://graph.facebook.com/{graph_version}/{quote(post_id, safe='')}",
                access_token,
            )
            on_deletion(provider)
        except RuntimeError as exc:
            safe_error = _redact_meta_error(str(exc), os.getenv("META_PAGE_ACCESS_TOKEN", "").strip())
            errors.append(f"{provider.title()}: {safe_error}")
            logger.warning(
                "Adoption listing %s %s post deletion failed: %s",
                listing.id,
                provider.title(),
                safe_error[:400],
            )
    return errors


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

    image_url = listing.social_post_image_url or listing.images[0]
    years, remaining_months = divmod(listing.age_months, 12)
    age_label = (
        f"{years} yr {remaining_months} mo"
        if years and remaining_months
        else f"{years} {'year' if years == 1 else 'years'}"
        if years
        else f"{remaining_months} {'month' if remaining_months == 1 else 'months'}"
    )
    breed = f" · {listing.breed.strip()}" if listing.breed and listing.breed.strip() else ""
    description = " ".join(listing.description.split())
    if len(description) > 800:
        description = description[:797].rsplit(" ", 1)[0] + "..."
    caption = (
        f"🐾 A new best friend is waiting to meet you! 🐾\n\n"
        f"Meet {listing.pet_name} — our lovely {listing.species.title()}{breed}.\n"
        f"🎂 {age_label}  ·  📍 {listing.location_city}\n\n"
        f"{description}\n\n"
        f"Give {listing.pet_name} a loving home. Learn more and apply in Fendly:\n"
        f"{download_url}\n\n"
        f"#AdoptDontShop #PetAdoption #AdoptWithFendly"
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
                prefer_page_post_id=True,
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
            _wait_for_instagram_container(
                container_id,
                access_token,
                graph_version,
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
