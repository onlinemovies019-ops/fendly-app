import logging
import os
import re
from collections.abc import Callable
from urllib.parse import urlsplit

import httpx

from models import AdoptionListing


logger = logging.getLogger(__name__)
DEFAULT_GRAPH_VERSION = "v23.0"
GRAPH_VERSION_PATTERN = re.compile(r"^v\d{1,2}\.0$")


def _graph_configuration() -> tuple[str, str, str, str, str] | None:
    page_id = os.getenv("META_FACEBOOK_PAGE_ID", "").strip()
    instagram_id = os.getenv("META_INSTAGRAM_ACCOUNT_ID", "").strip()
    access_token = os.getenv("META_PAGE_ACCESS_TOKEN", "").strip()
    download_url = os.getenv("APP_DOWNLOAD_URL", "").strip()
    graph_version = os.getenv("META_GRAPH_API_VERSION", DEFAULT_GRAPH_VERSION).strip()
    if not (page_id and instagram_id and access_token and download_url):
        return None
    parsed_download_url = urlsplit(download_url)
    if parsed_download_url.scheme != "https" or not parsed_download_url.netloc:
        raise ValueError("APP_DOWNLOAD_URL must be an HTTPS URL")
    if not GRAPH_VERSION_PATTERN.fullmatch(graph_version):
        raise ValueError("META_GRAPH_API_VERSION must look like v23.0")
    return page_id, instagram_id, access_token, download_url, graph_version


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
) -> tuple[dict[str, str], list[str]]:
    try:
        config = _graph_configuration()
    except ValueError as exc:
        return {}, [str(exc)]
    if config is None:
        return {}, [
            "Meta auto-posting is not configured; set the Facebook Page ID, "
            "Instagram account ID, Page access token, and app download URL."
        ]
    if not listing.images:
        return {}, ["Add at least one public pet photo before requesting social posting."]

    page_id, instagram_id, access_token, download_url, graph_version = config
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
    page_endpoint = f"https://graph.facebook.com/{graph_version}/{page_id}/photos"
    instagram_media_endpoint = f"https://graph.facebook.com/{graph_version}/{instagram_id}/media"
    if not listing.facebook_post_id:
        try:
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
            logger.warning("Adoption listing %s Facebook publishing failed", listing.id)
    if not listing.instagram_post_id:
        try:
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
            logger.warning("Adoption listing %s Instagram publishing failed", listing.id)

    return publications, errors
