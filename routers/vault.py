import base64
import binascii
import os
from datetime import date
from io import BytesIO
from pathlib import Path
from urllib.parse import quote, urlsplit
from uuid import uuid4

import httpx
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from fastapi import APIRouter, Depends, File, Form, HTTPException, Response, UploadFile, status
from PIL import Image, UnidentifiedImageError
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import VaultItem

router = APIRouter(prefix="/api/v1/vault", tags=["vault"])

MAX_IMAGE_BYTES = 10 * 1024 * 1024
MIME_FORMATS = {
    "image/jpeg": ("JPEG", ".jpg"),
    "image/png": ("PNG", ".png"),
    "image/webp": ("WEBP", ".webp"),
}


def _encryption_key() -> bytes:
    encoded = os.getenv("VAULT_ENCRYPTION_KEY", "")
    try:
        key = base64.b64decode(encoded, validate=True)
    except (binascii.Error, ValueError):
        key = b""
    if len(key) != 32:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Vault encryption is not configured",
        )
    return key


def _storage_backend() -> tuple[str, str | Path, str | None]:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    bucket = os.getenv("VAULT_STORAGE_BUCKET", "").strip()
    if supabase_url or service_key or bucket:
        parsed = urlsplit(supabase_url)
        if (
            not supabase_url
            or not service_key
            or not bucket
            or parsed.scheme != "https"
            or not parsed.netloc
        ):
            raise HTTPException(
                status_code=503,
                detail="Vault object storage is not configured",
            )
        return "supabase", supabase_url, f"{service_key}\n{bucket}"

    directory = Path(os.getenv("VAULT_LOCAL_STORAGE_DIR", "private_vault_storage")).resolve()
    static_root = Path("static").resolve()
    if directory == static_root or static_root in directory.parents:
        raise HTTPException(
            status_code=503,
            detail="Vault local storage must be outside the public static directory",
        )
    return "local", directory, None


def _storage_headers(credentials: str) -> tuple[dict[str, str], str]:
    service_key, bucket = credentials.split("\n", 1)
    return (
        {
            "Authorization": f"Bearer {service_key}",
            "apikey": service_key,
            "Content-Type": "application/octet-stream",
        },
        bucket,
    )


def _object_url(base_url: str, bucket: str, object_key: str) -> str:
    return (
        f"{base_url}/storage/v1/object/{quote(bucket, safe='')}/"
        f"{quote(object_key, safe='')}"
    )


def _write_encrypted_object(object_key: str, encrypted: bytes) -> None:
    backend, location, credentials = _storage_backend()
    if backend == "supabase":
        assert isinstance(location, str) and credentials
        headers, bucket = _storage_headers(credentials)
        headers["x-upsert"] = "false"
        try:
            response = httpx.post(
                _object_url(location, bucket, object_key),
                headers=headers,
                content=encrypted,
                timeout=30.0,
            )
            response.raise_for_status()
        except httpx.HTTPError as exc:
            raise HTTPException(status_code=503, detail="Vault storage is unavailable") from exc
        return

    assert isinstance(location, Path)
    try:
        location.mkdir(mode=0o700, parents=True, exist_ok=True)
        target = (location / object_key).resolve()
        if target.parent != location:
            raise HTTPException(status_code=503, detail="Vault storage path is invalid")
        with target.open("xb") as stored:
            os.chmod(target, 0o600)
            stored.write(encrypted)
    except HTTPException:
        raise
    except OSError as exc:
        raise HTTPException(status_code=503, detail="Vault storage is unavailable") from exc


def _read_encrypted_object(object_key: str) -> bytes:
    backend, location, credentials = _storage_backend()
    if backend == "supabase":
        assert isinstance(location, str) and credentials
        headers, bucket = _storage_headers(credentials)
        headers.pop("Content-Type", None)
        try:
            response = httpx.get(
                _object_url(location, bucket, object_key),
                headers=headers,
                timeout=30.0,
            )
            response.raise_for_status()
            return response.content
        except httpx.HTTPError as exc:
            raise HTTPException(status_code=503, detail="Vault storage is unavailable") from exc

    assert isinstance(location, Path)
    target = (location / object_key).resolve()
    if target.parent != location:
        raise HTTPException(status_code=503, detail="Vault storage path is invalid")
    try:
        return target.read_bytes()
    except OSError as exc:
        raise HTTPException(status_code=503, detail="Vault storage is unavailable") from exc


def _delete_encrypted_object(object_key: str) -> None:
    backend, location, credentials = _storage_backend()
    if backend == "supabase":
        assert isinstance(location, str) and credentials
        headers, bucket = _storage_headers(credentials)
        headers.pop("Content-Type", None)
        try:
            response = httpx.delete(
                _object_url(location, bucket, object_key),
                headers=headers,
                timeout=30.0,
            )
            response.raise_for_status()
        except httpx.HTTPError as exc:
            raise HTTPException(status_code=503, detail="Vault storage is unavailable") from exc
        return

    assert isinstance(location, Path)
    target = (location / object_key).resolve()
    if target.parent != location:
        raise HTTPException(status_code=503, detail="Vault storage path is invalid")
    try:
        target.unlink(missing_ok=True)
    except OSError as exc:
        raise HTTPException(status_code=503, detail="Vault storage is unavailable") from exc


def _validate_image(data: bytes, mime_type: str | None) -> tuple[str, str]:
    normalized_mime = (mime_type or "").split(";", 1)[0].strip().lower()
    expected = MIME_FORMATS.get(normalized_mime)
    if expected is None:
        raise HTTPException(status_code=415, detail="Only JPEG, PNG, and WebP images are supported")
    try:
        with Image.open(BytesIO(data)) as image:
            image_format = image.format
            width, height = image.size
            if width <= 0 or height <= 0 or width * height > 40_000_000:
                raise HTTPException(status_code=413, detail="Image dimensions are too large")
            image.verify()
        with Image.open(BytesIO(data)) as image:
            image.load()
    except HTTPException:
        raise
    except (UnidentifiedImageError, OSError, ValueError, Image.DecompressionBombError) as exc:
        raise HTTPException(status_code=422, detail="Uploaded file is not a valid image") from exc
    if image_format != expected[0]:
        raise HTTPException(status_code=415, detail="Image content does not match its MIME type")
    return normalized_mime, expected[1]


def _serialize(item: VaultItem) -> dict[str, object | None]:
    return {
        "id": item.id,
        "store": item.store,
        "purchase_date": item.purchase_date.isoformat() if item.purchase_date else None,
        "item": item.item,
        "serial_imei": item.serial_imei,
        "warranty_months": item.warranty_months,
        "warranty_expiry": item.warranty_expiry.isoformat() if item.warranty_expiry else None,
        "created_at": item.created_at.isoformat() if item.created_at else None,
    }


@router.post("/items", status_code=status.HTTP_201_CREATED)
def upload_vault_item(
    file: UploadFile = File(...),
    store: str | None = Form(default=None, max_length=160),
    purchase_date: date | None = Form(default=None),
    item: str | None = Form(default=None, max_length=160),
    serial_imei: str | None = Form(default=None, max_length=128),
    warranty_months: int | None = Form(default=None, ge=0, le=1200),
    warranty_expiry: date | None = Form(default=None),
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object | None]:
    key = _encryption_key()
    _storage_backend()
    if warranty_months is not None and warranty_months > 1200:
        raise HTTPException(status_code=422, detail="warranty_months must not exceed 1200")
    image = file.file.read(MAX_IMAGE_BYTES + 1)
    if len(image) > MAX_IMAGE_BYTES:
        raise HTTPException(status_code=413, detail="Image exceeds the 10 MiB limit")
    mime_type, extension = _validate_image(image, file.content_type)

    item_id = str(uuid4())
    object_key = f"{uuid4().hex}.enc"
    nonce = os.urandom(12)
    encrypted = nonce + AESGCM(key).encrypt(
        nonce,
        image,
        f"{uid}:{item_id}".encode("utf-8"),
    )
    record = VaultItem(
        id=item_id,
        firebase_uid=uid,
        object_key=object_key,
        mime_type=mime_type,
        store=store.strip() or None if store else None,
        purchase_date=purchase_date,
        item=item.strip() or None if item else None,
        serial_imei=serial_imei.strip() or None if serial_imei else None,
        warranty_months=warranty_months,
        warranty_expiry=warranty_expiry,
    )
    _write_encrypted_object(object_key, encrypted)
    try:
        session.add(record)
        session.commit()
        session.refresh(record)
    except Exception as exc:
        session.rollback()
        try:
            _delete_encrypted_object(object_key)
        except HTTPException:
            pass
        raise HTTPException(status_code=503, detail="Vault metadata could not be saved") from exc
    return _serialize(record)


@router.get("/items")
def list_vault_items(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[dict[str, object | None]]:
    items = session.scalars(
        select(VaultItem)
        .where(VaultItem.firebase_uid == uid)
        .order_by(desc(VaultItem.created_at))
    ).all()
    return [_serialize(entry) for entry in items]


@router.get("/items/{item_id}/image")
def download_vault_image(
    item_id: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> Response:
    entry = session.scalar(
        select(VaultItem).where(
            VaultItem.id == item_id,
            VaultItem.firebase_uid == uid,
        )
    )
    if entry is None:
        raise HTTPException(status_code=404, detail="Vault item not found")
    key = _encryption_key()
    encrypted = _read_encrypted_object(entry.object_key)
    try:
        if len(encrypted) < 12 + 16:
            raise ValueError("Encrypted image is truncated")
        image = AESGCM(key).decrypt(
            encrypted[:12],
            encrypted[12:],
            f"{uid}:{entry.id}".encode("utf-8"),
        )
    except Exception as exc:
        raise HTTPException(status_code=503, detail="Vault image could not be decrypted") from exc
    extension = MIME_FORMATS.get(entry.mime_type, ("", ".bin"))[1]
    return Response(
        content=image,
        media_type=entry.mime_type,
        headers={
            "Content-Disposition": f'attachment; filename="vault-{entry.id}{extension}"',
            "Cache-Control": "no-store",
            "X-Content-Type-Options": "nosniff",
        },
    )


@router.delete("/items/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_vault_item(
    item_id: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> Response:
    entry = session.scalar(
        select(VaultItem).where(
            VaultItem.id == item_id,
            VaultItem.firebase_uid == uid,
        )
    )
    if entry is None:
        raise HTTPException(status_code=404, detail="Vault item not found")
    _delete_encrypted_object(entry.object_key)
    session.delete(entry)
    try:
        session.commit()
    except Exception as exc:
        session.rollback()
        raise HTTPException(status_code=503, detail="Vault item could not be deleted") from exc
    return Response(status_code=status.HTTP_204_NO_CONTENT)
