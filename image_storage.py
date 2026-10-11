import os
from urllib.parse import quote, unquote, urlsplit

import httpx


SIGNED_URL_TTL_SECONDS = 3600


def _storage_config() -> tuple[str, str, str]:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    bucket = os.getenv("SUPABASE_STORAGE_BUCKET", "uploads")
    if not supabase_url or not service_role_key:
        raise RuntimeError("Private report image storage is not configured")
    return supabase_url, service_role_key, bucket


def _storage_headers(service_role_key: str) -> dict[str, str]:
    return {
        "Authorization": f"Bearer {service_role_key}",
        "apikey": service_role_key,
        "Content-Type": "application/json",
    }


def _signed_url_from_response(
    supabase_url: str,
    response: httpx.Response,
) -> str:
    response.raise_for_status()
    payload = response.json()
    signed_url = payload.get("signedURL") if isinstance(payload, dict) else None
    if not isinstance(signed_url, str) or not signed_url:
        raise RuntimeError("Supabase Storage did not return a signed image URL")
    if signed_url.startswith("https://"):
        return signed_url
    if not signed_url.startswith("/"):
        signed_url = f"/{signed_url}"
    return f"{supabase_url}/storage/v1{signed_url}"


async def store_image(data: bytes, filename: str, content_type: str) -> str:
    supabase_url, service_role_key, bucket = _storage_config()
    encoded_filename = quote(filename, safe="/")
    headers = _storage_headers(service_role_key)
    headers["Content-Type"] = content_type
    headers["x-upsert"] = "false"

    async with httpx.AsyncClient(timeout=15) as client:
        upload_response = await client.post(
            f"{supabase_url}/storage/v1/object/{quote(bucket, safe='')}/{encoded_filename}",
            content=data,
            headers=headers,
        )
        upload_response.raise_for_status()
        signed_response = await client.post(
            f"{supabase_url}/storage/v1/object/sign/{quote(bucket, safe='')}/{encoded_filename}",
            json={"expiresIn": SIGNED_URL_TTL_SECONDS},
            headers=_storage_headers(service_role_key),
        )
    return _signed_url_from_response(supabase_url, signed_response)


def refresh_private_image_url(image_url: str | None) -> str | None:
    if not image_url:
        return image_url
    parsed = urlsplit(image_url)
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    if not supabase_url or not service_role_key:
        if parsed.scheme == "storage":
            raise RuntimeError("Private report image storage is not configured")
        return image_url

    if parsed.scheme == "storage" and parsed.netloc:
        bucket, object_key = unquote(parsed.netloc), unquote(parsed.path.lstrip("/"))
    else:
        base = urlsplit(supabase_url)
        if (parsed.scheme, parsed.netloc) != (base.scheme, base.netloc):
            return image_url
        prefix = "/storage/v1/object/"
        if not parsed.path.startswith(prefix):
            return image_url
        parts = parsed.path[len(prefix):].split("/", 2)
        if len(parts) != 3 or parts[0] not in {"public", "sign"}:
            return image_url
        bucket, object_key = unquote(parts[1]), unquote(parts[2])

    signed_response = httpx.post(
        f"{supabase_url}/storage/v1/object/sign/"
        f"{quote(bucket, safe='')}/{quote(object_key, safe='/')}",
        json={"expiresIn": SIGNED_URL_TTL_SECONDS},
        headers=_storage_headers(service_role_key),
        timeout=15,
    )
    return _signed_url_from_response(supabase_url, signed_response)


def private_image_reference(image_url: str | None) -> str | None:
    if not image_url:
        return image_url

    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    parsed = urlsplit(image_url)
    if parsed.scheme == "storage" and parsed.netloc:
        return image_url
    if not supabase_url:
        return image_url

    base = urlsplit(supabase_url)
    if (parsed.scheme, parsed.netloc) != (base.scheme, base.netloc):
        return image_url
    prefix = "/storage/v1/object/"
    if not parsed.path.startswith(prefix):
        return image_url
    parts = parsed.path[len(prefix):].split("/", 2)
    if len(parts) != 3 or parts[0] not in {"public", "sign"}:
        return image_url

    bucket, object_key = unquote(parts[1]), unquote(parts[2])
    return f"storage://{quote(bucket, safe='')}/{quote(object_key, safe='/')}"
