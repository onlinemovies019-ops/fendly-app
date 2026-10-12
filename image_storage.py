import asyncio
import os
from collections.abc import Sequence
from urllib.parse import quote, unquote, urlsplit

from supabase import Client, create_client


BUCKET_NAME = "report-images"
LEGACY_BUCKET_NAMES = {"uploads"}
SIGNED_URL_TTL_SECONDS = 3600


def _storage_config() -> tuple[str, str]:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    if not supabase_url or not service_role_key:
        raise RuntimeError("Private report image storage is not configured")
    parsed = urlsplit(supabase_url)
    if parsed.scheme != "https" or not parsed.netloc:
        raise RuntimeError("SUPABASE_URL must be a valid HTTPS URL")
    return supabase_url, service_role_key


def _storage_client() -> Client:
    supabase_url, service_role_key = _storage_config()
    return create_client(supabase_url, service_role_key)


def _storage_object(image_url: str) -> tuple[str, str] | None:
    parsed = urlsplit(image_url)
    if parsed.scheme == "storage" and parsed.netloc:
        bucket = unquote(parsed.netloc)
        object_key = unquote(parsed.path.lstrip("/"))
    else:
        supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
        if not supabase_url:
            return None
        base = urlsplit(supabase_url)
        if (parsed.scheme, parsed.netloc) != (base.scheme, base.netloc):
            return None
        prefix = "/storage/v1/object/"
        if not parsed.path.startswith(prefix):
            return None
        parts = parsed.path[len(prefix):].split("/", 2)
        if len(parts) != 3 or parts[0] not in {"public", "sign", "authenticated"}:
            return None
        bucket, object_key = unquote(parts[1]), unquote(parts[2])
    if not object_key or bucket not in {BUCKET_NAME, *LEGACY_BUCKET_NAMES}:
        return None
    return bucket, object_key


def _make_signed_url(image_url: str, client: Client | None = None) -> str:
    supabase_url, _ = _storage_config()
    storage_object = _storage_object(image_url)
    if storage_object is None:
        raise ValueError("Image URL is not a supported private Supabase object")
    bucket, object_key = storage_object
    storage_client = client or _storage_client()
    response = storage_client.storage.from_(bucket).create_signed_url(
        object_key,
        SIGNED_URL_TTL_SECONDS,
    )
    signed_url = response["signedURL"] or response["signedUrl"]
    if not isinstance(signed_url, str) or not signed_url:
        raise RuntimeError("Supabase Storage did not return a signed image URL")
    if signed_url.startswith("https://"):
        return signed_url
    return f"{supabase_url}/storage/v1/{signed_url.lstrip('/')}"


def _upload_and_sign(data: bytes, object_key: str, content_type: str) -> str:
    client = _storage_client()
    client.storage.from_(BUCKET_NAME).upload(
        object_key,
        data,
        file_options={
            "content-type": content_type,
            "upsert": "false",
        },
    )
    return _make_signed_url(
        f"storage://{BUCKET_NAME}/{quote(object_key, safe='/')}",
        client,
    )


async def store_image(data: bytes, filename: str, content_type: str) -> str:
    object_key = quote(filename, safe="/")
    if not object_key or object_key.startswith("/") or ".." in object_key.split("/"):
        raise ValueError("Image object key is invalid")
    return await asyncio.to_thread(_upload_and_sign, data, object_key, content_type)


def refresh_private_image_url(image_url: str | None) -> str | None:
    if not image_url:
        return image_url
    try:
        storage_object = _storage_object(image_url)
    except RuntimeError:
        if urlsplit(image_url).scheme == "storage":
            raise
        return image_url
    if storage_object is None:
        return image_url
    return _make_signed_url(image_url)


def refresh_private_image_urls(image_urls: Sequence[str | None]) -> list[str | None]:
    refreshed = list(image_urls)
    grouped_objects: dict[str, dict[str, list[int]]] = {}
    for index, image_url in enumerate(image_urls):
        if not image_url:
            continue
        try:
            storage_object = _storage_object(image_url)
        except RuntimeError:
            if urlsplit(image_url).scheme == "storage":
                raise
            continue
        if storage_object is None:
            continue
        bucket, object_key = storage_object
        grouped_objects.setdefault(bucket, {}).setdefault(object_key, []).append(index)

    if not grouped_objects:
        return refreshed

    supabase_url, _ = _storage_config()
    client = _storage_client()
    for bucket, objects in grouped_objects.items():
        signed_entries = client.storage.from_(bucket).create_signed_urls(
            list(objects),
            SIGNED_URL_TTL_SECONDS,
        )
        if len(signed_entries) != len(objects):
            raise RuntimeError("Supabase Storage returned an incomplete signed URL batch")
        for (object_key, indices), signed_entry in zip(objects.items(), signed_entries):
            if signed_entry.get("error"):
                raise RuntimeError("Supabase Storage could not sign a private report photo")
            signed_url = signed_entry.get("signedURL") or signed_entry.get("signedUrl")
            if not isinstance(signed_url, str) or not signed_url:
                raise RuntimeError("Supabase Storage did not return a signed image URL")
            signed_image_url = (
                signed_url if signed_url.startswith("https://")
                else f"{supabase_url}/storage/v1/{signed_url.lstrip('/')}"
            )
            for index in indices:
                refreshed[index] = signed_image_url
    return refreshed


def private_image_reference(image_url: str | None) -> str | None:
    if not image_url:
        return image_url
    storage_object = _storage_object(image_url)
    if storage_object is None:
        return image_url
    bucket, object_key = storage_object
    return f"storage://{quote(bucket, safe='')}/{quote(object_key, safe='/')}"


def download_storage_image(bucket: str, object_key: str) -> bytes:
    if bucket not in {BUCKET_NAME, *LEGACY_BUCKET_NAMES} or not object_key:
        raise ValueError("Image object is not in a supported private storage bucket")
    return _storage_client().storage.from_(bucket).download(object_key)


def get_private_image_for_moderation(image_url: str) -> tuple[bytes, str] | None:
    storage_object = _storage_object(image_url)
    if storage_object is None:
        return None
    bucket, object_key = storage_object
    extension = object_key.rsplit(".", 1)[-1].lower() if "." in object_key else ""
    content_type = {
        "jpg": "image/jpeg",
        "jpeg": "image/jpeg",
        "png": "image/png",
        "webp": "image/webp",
    }.get(extension)
    if content_type is None:
        raise ValueError("Private image has an unsupported file extension")
    return download_storage_image(bucket, object_key), content_type


def delete_storage_image(bucket: str, object_key: str) -> None:
    if bucket not in {BUCKET_NAME, *LEGACY_BUCKET_NAMES} or not object_key:
        raise ValueError("Image object is not in a supported private storage bucket")
    _storage_client().storage.from_(bucket).remove([object_key])


def delete_private_image_asset(image_url: str) -> bool:
    storage_object = _storage_object(image_url)
    if storage_object is None:
        return False
    delete_storage_image(*storage_object)
    return True


def verify_private_bucket(bucket_name: str = BUCKET_NAME) -> None:
    if not bucket_name:
        raise ValueError("Supabase Storage bucket name is required")
    bucket = _storage_client().storage.get_bucket(bucket_name)
    if bucket.public is not False:
        raise RuntimeError(f"Supabase Storage bucket {bucket_name!r} must be private")
