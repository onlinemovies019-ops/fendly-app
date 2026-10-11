import asyncio
import os
from urllib.parse import quote, urlsplit

from supabase import Client, create_client


BUCKET_NAME = "adoption-images"


def _storage_client() -> tuple[str, Client]:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    parsed = urlsplit(supabase_url)
    if (
        parsed.scheme != "https"
        or not parsed.netloc
        or not service_role_key
    ):
        raise RuntimeError("Adoption image storage is not configured")
    return supabase_url, create_client(supabase_url, service_role_key)


def _upload_public_image(data: bytes, object_key: str, content_type: str) -> str:
    supabase_url, client = _storage_client()
    client.storage.from_(BUCKET_NAME).upload(
        object_key,
        data,
        file_options={"content-type": content_type, "upsert": "false"},
    )
    public_url = client.storage.from_(BUCKET_NAME).get_public_url(object_key)
    if not isinstance(public_url, str) or not public_url.startswith(f"{supabase_url}/"):
        raise RuntimeError("Supabase Storage did not return a valid adoption image URL")
    return public_url


async def store_adoption_image(data: bytes, object_key: str, content_type: str) -> str:
    safe_key = quote(object_key, safe="/")
    if not safe_key or safe_key.startswith("/") or ".." in safe_key.split("/"):
        raise ValueError("Adoption image object key is invalid")
    return await asyncio.to_thread(_upload_public_image, data, safe_key, content_type)


def delete_adoption_image(image_url: str) -> None:
    supabase_url, client = _storage_client()
    parsed = urlsplit(image_url)
    expected_path = f"/storage/v1/object/public/{BUCKET_NAME}/"
    if (parsed.scheme, parsed.netloc) != (
        urlsplit(supabase_url).scheme,
        urlsplit(supabase_url).netloc,
    ) or not parsed.path.startswith(expected_path):
        raise ValueError("Adoption image URL is not a supported public object")
    object_key = parsed.path[len(expected_path):]
    if not object_key:
        raise ValueError("Adoption image URL has no object key")
    client.storage.from_(BUCKET_NAME).remove([object_key])


def verify_adoption_bucket() -> None:
    _, client = _storage_client()
    bucket = client.storage.get_bucket(BUCKET_NAME)
    if bucket.public is not True:
        raise RuntimeError(f"Supabase Storage bucket {BUCKET_NAME!r} must be public")
