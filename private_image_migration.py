import asyncio
import hashlib
import io
import logging
import os
import re
import time
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import unquote, urlsplit
from uuid import uuid4

import httpx
from PIL import Image, UnidentifiedImageError
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from image_storage import (
    BUCKET_NAME,
    delete_storage_image,
    download_storage_image,
    private_image_reference,
    store_image,
)
from models import (
    FoundItem,
    LostItem,
    PrivateImageMigrationCleanup,
    User,
)

logger = logging.getLogger(__name__)
MAX_IMAGE_BYTES = 10 * 1024 * 1024
IMAGE_FORMATS = {
    "JPEG": ("image/jpeg", "jpg"),
    "PNG": ("image/png", "png"),
    "WEBP": ("image/webp", "webp"),
}
LOCAL_UPLOAD_NAME = re.compile(r"[0-9a-f]{32}\.(?:jpg|png|webp)")
CLOUDINARY_VERSION = re.compile(r"v\d+")


@dataclass
class LegacyImage:
    key: str
    kind: str
    locator: str
    urls: set[str] = field(default_factory=set)


def _cloudinary_source(url: str) -> tuple[str, str] | None:
    parsed = urlsplit(url)
    cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME", "").strip()
    if parsed.scheme != "https" or parsed.hostname != "res.cloudinary.com" or not cloud_name:
        return None
    segments = [unquote(part) for part in parsed.path.split("/") if part]
    if len(segments) < 5 or segments[0] != cloud_name or segments[1:3] != ["image", "upload"]:
        return None
    upload_parts = segments[3:]
    version_index = next(
        (index for index, part in enumerate(upload_parts) if CLOUDINARY_VERSION.fullmatch(part)),
        None,
    )
    if version_index is None or version_index == len(upload_parts) - 1:
        return None
    public_id = "/".join(upload_parts[version_index + 1 :])
    public_id = re.sub(r"\.[A-Za-z0-9]+$", "", public_id)
    if not public_id or ".." in public_id.split("/"):
        return None
    return cloud_name, public_id


def _local_upload_source(url: str) -> str | None:
    parsed = urlsplit(url)
    public_base = urlsplit(
        os.getenv("PUBLIC_BASE_URL", "https://fendly-api.onrender.com").rstrip("/")
    )
    if (parsed.scheme, parsed.netloc) != (public_base.scheme, public_base.netloc):
        return None
    segments = [unquote(part) for part in parsed.path.split("/") if part]
    if len(segments) != 3 or segments[:2] != ["static", "uploads"]:
        return None
    filename = segments[2]
    return filename if LOCAL_UPLOAD_NAME.fullmatch(filename) else None


def _supabase_source(url: str) -> tuple[str, str] | None:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    parsed = urlsplit(url)
    if parsed.scheme == "storage" and parsed.netloc:
        bucket = unquote(parsed.netloc)
        object_key = unquote(parsed.path.lstrip("/"))
        if bucket and object_key:
            return bucket, object_key
    if not supabase_url:
        return None
    base = urlsplit(supabase_url)
    if (parsed.scheme, parsed.netloc) != (base.scheme, base.netloc):
        return None
    parts = parsed.path.removeprefix("/storage/v1/object/").split("/", 2)
    if len(parts) != 3 or parts[0] not in {"public", "sign", "authenticated"}:
        return None
    bucket, object_key = unquote(parts[1]), unquote(parts[2])
    return (bucket, object_key) if bucket and object_key else None


def _asset_for_url(url: str) -> LegacyImage | None:
    if not url:
        return None
    supabase = _supabase_source(url)
    if supabase:
        bucket, object_key = supabase
        if bucket == BUCKET_NAME:
            return None
        return LegacyImage(
            key=f"supabase:{bucket}:{object_key}",
            kind="supabase",
            locator=f"{bucket}/{object_key}",
            urls={url},
        )
    cloudinary = _cloudinary_source(url)
    if cloudinary:
        cloud_name, public_id = cloudinary
        return LegacyImage(
            key=f"cloudinary:{cloud_name}:{public_id}",
            kind="cloudinary",
            locator=f"{cloud_name}/{public_id}",
            urls={url},
        )
    filename = _local_upload_source(url)
    if filename:
        return LegacyImage(
            key=f"local:{filename}",
            kind="local",
            locator=filename,
            urls={url},
        )
    return None


def _all_image_references(session: Session) -> list[tuple[object, str, str]]:
    references: list[tuple[object, str, str]] = []
    for model in (User, LostItem, FoundItem):
        for record in session.scalars(select(model)).all():
            for field_name in ("profile_photo_url", "image_url", "social_poster_url"):
                value = getattr(record, field_name, None)
                if isinstance(value, str) and value:
                    references.append((record, field_name, value))
            values = getattr(record, "image_urls", None)
            if isinstance(values, list):
                references.extend(
                    (record, "image_urls", value)
                    for value in values
                    if isinstance(value, str) and value
                )
    return references


def _collect_images(
    references: list[tuple[object, str, str]],
) -> tuple[dict[str, LegacyImage], int]:
    images: dict[str, LegacyImage] = {}
    unsupported = 0
    for _, _, url in references:
        image = _asset_for_url(url)
        if image is None:
            source = _supabase_source(url)
            if source and source[0] == BUCKET_NAME:
                continue
            unsupported += 1
            continue
        existing = images.get(image.key)
        if existing is None:
            images[image.key] = image
        else:
            existing.urls.add(url)
    return images, unsupported


async def _download_image(image: LegacyImage) -> tuple[bytes, str, str]:
    if image.kind == "local":
        filename = image.locator
        if not LOCAL_UPLOAD_NAME.fullmatch(filename):
            raise ValueError("Legacy upload filename is invalid")
        upload_root = Path(os.getenv("UPLOAD_DIR", "static/uploads")).resolve()
        path = (upload_root / filename).resolve()
        if path.parent != upload_root or not path.is_file():
            raise FileNotFoundError("Legacy upload file is unavailable on this server")
        data = await asyncio.to_thread(path.read_bytes)
    elif image.kind == "supabase":
        bucket, object_key = image.locator.split("/", 1)
        data = await asyncio.to_thread(download_storage_image, bucket, object_key)
    elif image.kind == "cloudinary":
        cloud_name, public_id = image.locator.split("/", 1)
        url = next(iter(image.urls))
        parsed = urlsplit(url)
        if parsed.hostname != "res.cloudinary.com" or cloud_name != os.getenv(
            "CLOUDINARY_CLOUD_NAME", ""
        ):
            raise ValueError("Cloudinary image origin does not match the configured account")
        async with httpx.AsyncClient(timeout=20, follow_redirects=False) as client:
            async with client.stream("GET", url) as response:
                response.raise_for_status()
                content_length = response.headers.get("content-length")
                if content_length and int(content_length) > MAX_IMAGE_BYTES:
                    raise ValueError("Legacy image exceeds the 10 MB migration limit")
                chunks: list[bytes] = []
                total = 0
                async for chunk in response.aiter_bytes():
                    total += len(chunk)
                    if total > MAX_IMAGE_BYTES:
                        raise ValueError("Legacy image exceeds the 10 MB migration limit")
                    chunks.append(chunk)
                data = b"".join(chunks)
    else:
        raise ValueError("Unsupported image source")

    if len(data) > MAX_IMAGE_BYTES:
        raise ValueError("Legacy image exceeds the 10 MB migration limit")
    try:
        with Image.open(io.BytesIO(data)) as checked:
            checked.verify()
            image_format = checked.format
    except (UnidentifiedImageError, OSError) as error:
        raise ValueError("Legacy image is not a valid supported image") from error
    if image_format not in IMAGE_FORMATS:
        raise ValueError("Legacy image format is not supported")
    content_type, extension = IMAGE_FORMATS[image_format]
    return data, content_type, extension


def _replace_references(
    session: Session,
    old_urls: set[str],
    new_url: str,
) -> int:
    replaced = 0
    for record, field_name, value in _all_image_references(session):
        if value not in old_urls:
            continue
        if field_name == "image_urls":
            current = getattr(record, field_name)
            setattr(
                record,
                field_name,
                [new_url if entry in old_urls else entry for entry in current],
            )
        else:
            setattr(record, field_name, new_url)
        replaced += 1
    return replaced


def _cleanup_key(kind: str, locator: str) -> str:
    return hashlib.sha256(f"{kind}:{locator}".encode("utf-8")).hexdigest()


def _persist_cleanup_task(
    session: Session,
    kind: str,
    locator: str,
) -> None:
    key = _cleanup_key(kind, locator)
    task = session.get(PrivateImageMigrationCleanup, key)
    if task is None:
        session.add(
            PrivateImageMigrationCleanup(
                source_key=key,
                source_kind=kind,
                source_locator=locator,
            )
        )
    else:
        task.cleaned_at = None
        task.last_error = None


def _delete_cloudinary_asset(locator: str) -> None:
    cloud_name, public_id = locator.split("/", 1)
    configured_cloud = os.getenv("CLOUDINARY_CLOUD_NAME", "")
    api_key = os.getenv("CLOUDINARY_API_KEY", "")
    api_secret = os.getenv("CLOUDINARY_API_SECRET", "")
    if not api_key or not api_secret or cloud_name != configured_cloud:
        raise RuntimeError("Cloudinary deletion credentials are not configured")
    timestamp = int(time.time())
    signature = hashlib.sha1(
        f"public_id={public_id}&timestamp={timestamp}{api_secret}".encode("utf-8")
    ).hexdigest()
    response = httpx.post(
        f"https://api.cloudinary.com/v1_1/{cloud_name}/image/destroy",
        data={
            "public_id": public_id,
            "timestamp": timestamp,
            "api_key": api_key,
            "signature": signature,
        },
        timeout=15,
    )
    response.raise_for_status()
    result = response.json().get("result")
    if result not in {"ok", "not found"}:
        raise RuntimeError("Cloudinary did not confirm image removal")


def _delete_local_asset(filename: str) -> None:
    if not LOCAL_UPLOAD_NAME.fullmatch(filename):
        raise ValueError("Legacy upload filename is invalid")
    upload_root = Path(os.getenv("UPLOAD_DIR", "static/uploads")).resolve()
    path = (upload_root / filename).resolve()
    if path.parent != upload_root:
        raise ValueError("Legacy upload is outside the configured upload directory")
    path.unlink(missing_ok=True)


def _image_key_is_referenced(session: Session, key: str) -> bool:
    images, _ = _collect_images(_all_image_references(session))
    return key in images


def _pending_cleanup_count(session: Session) -> int:
    return int(
        session.scalar(
            select(func.count()).select_from(PrivateImageMigrationCleanup).where(
                PrivateImageMigrationCleanup.cleaned_at.is_(None)
            )
        )
        or 0
    )


def _process_pending_cleanup(session: Session, limit: int) -> tuple[int, int, list[str]]:
    tasks = session.scalars(
        select(PrivateImageMigrationCleanup)
        .where(PrivateImageMigrationCleanup.cleaned_at.is_(None))
        .order_by(PrivateImageMigrationCleanup.created_at)
        .limit(limit)
    ).all()
    removed = 0
    failures: list[str] = []
    for task in tasks:
        if task.source_kind == "cloudinary":
            cloud_name, public_id = task.source_locator.split("/", 1)
            reference_key = f"cloudinary:{cloud_name}:{public_id}"
        elif task.source_kind == "supabase":
            bucket, object_key = task.source_locator.split("/", 1)
            reference_key = f"supabase:{bucket}:{object_key}"
        else:
            reference_key = f"local:{task.source_locator}"
        if _image_key_is_referenced(session, reference_key):
            continue
        try:
            if task.source_kind == "cloudinary":
                _delete_cloudinary_asset(task.source_locator)
            elif task.source_kind == "supabase":
                bucket, object_key = task.source_locator.split("/", 1)
                delete_storage_image(bucket, object_key)
            elif task.source_kind == "local":
                _delete_local_asset(task.source_locator)
            else:
                raise ValueError("Pending image cleanup has an unknown source type")
            task.cleaned_at = datetime.now(timezone.utc)
            task.last_error = None
            removed += 1
        except Exception as error:
            logger.exception("Could not remove migrated %s image", task.source_kind)
            task.last_error = f"{type(error).__name__}: {str(error)[:440]}"
            failures.append(task.last_error)
    session.commit()
    return removed, _pending_cleanup_count(session), failures


async def migrate_private_images(
    session: Session,
    *,
    limit: int = 10,
    apply: bool = False,
) -> dict[str, object]:
    references = _all_image_references(session)
    images, unsupported = _collect_images(references)
    ordered_images = list(images.values())
    candidates = {
        "candidate_assets": len(ordered_images),
        "assets_in_batch": min(len(ordered_images), limit),
        "unsupported_references": unsupported,
    }
    if not apply:
        return {
            **candidates,
            "migrated_assets": 0,
            "migrated_references": 0,
            "removed_sources": 0,
            "pending_source_cleanup": _pending_cleanup_count(session),
            "failures": [],
        }

    migrated_assets = 0
    migrated_references = 0
    failures: list[str] = []
    for image in ordered_images[:limit]:
        try:
            data, content_type, extension = await _download_image(image)
            signed_url = await store_image(
                data,
                f"{uuid4().hex}.{extension}",
                content_type,
            )
            new_url = private_image_reference(signed_url)
            if not new_url or not new_url.startswith(f"storage://{BUCKET_NAME}/"):
                raise RuntimeError("Image copy did not produce a private storage reference")
            migrated_references += _replace_references(session, image.urls, new_url)
            if image.kind in {"cloudinary", "local", "supabase"}:
                _persist_cleanup_task(session, image.kind, image.locator)
            session.commit()
            migrated_assets += 1
        except Exception as error:
            session.rollback()
            logger.exception("Could not migrate a %s image", image.kind)
            failures.append(f"{image.kind}: {type(error).__name__}: {str(error)[:300]}")

    try:
        removed, pending, cleanup_failures = _process_pending_cleanup(session, limit)
        failures.extend(cleanup_failures)
    except Exception as error:
        session.rollback()
        logger.exception("Could not process private image cleanup queue")
        removed, pending = 0, 1
        failures.append(f"cleanup: {type(error).__name__}: {str(error)[:300]}")

    remaining_images, unsupported = _collect_images(_all_image_references(session))
    return {
        **candidates,
        "migrated_assets": migrated_assets,
        "migrated_references": migrated_references,
        "removed_sources": removed,
        "remaining_assets": len(remaining_images),
        "unsupported_references": unsupported,
        "pending_source_cleanup": pending,
        "failures": failures,
    }
