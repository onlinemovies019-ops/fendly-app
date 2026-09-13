import io
import os
import re
from pathlib import Path
from uuid import uuid4

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status
import httpx
from PIL import Image, UnidentifiedImageError
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import FoundItem, LostItem
from schemas import ItemCreate, ItemResponse, MatchRequest, MatchResponse


router = APIRouter(prefix="/api", tags=["items"])
MAX_IMAGE_BYTES = 10 * 1024 * 1024
ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"}
WORD_PATTERN = re.compile(r"[a-z0-9]+")


def _save_item(payload: ItemCreate, session: Session, uid: str, model: type[LostItem] | type[FoundItem]):
    record = model(**payload.model_dump(), created_by=uid)
    session.add(record)
    session.commit()
    session.refresh(record)
    return record


async def _store_image(data: bytes, filename: str, content_type: str) -> str:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY")
    bucket = os.getenv("SUPABASE_STORAGE_BUCKET", "uploads")
    if supabase_url and service_role_key:
        endpoint = f"{supabase_url}/storage/v1/object/{bucket}/{filename}"
        headers = {
            "Authorization": f"Bearer {service_role_key}",
            "Content-Type": content_type,
            "x-upsert": "false",
        }
        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.post(endpoint, content=data, headers=headers)
        if response.is_error:
            raise HTTPException(502, "Image storage upload failed")
        return f"{supabase_url}/storage/v1/object/public/{bucket}/{filename}"

    upload_dir = Path(os.getenv("UPLOAD_DIR", "static/uploads"))
    upload_dir.mkdir(parents=True, exist_ok=True)
    (upload_dir / filename).write_bytes(data)
    base_url = os.getenv("PUBLIC_BASE_URL", "http://127.0.0.1:8000").rstrip("/")
    return f"{base_url}/static/uploads/{filename}"


@router.post("/upload", status_code=status.HTTP_201_CREATED)
async def upload_image(
    image: UploadFile = File(...),
    _: str = Depends(get_current_user),
) -> dict[str, str]:
    if image.content_type not in ALLOWED_IMAGE_TYPES:
        raise HTTPException(415, "Only JPEG, PNG, and WebP images are supported")
    data = await image.read(MAX_IMAGE_BYTES + 1)
    if len(data) > MAX_IMAGE_BYTES:
        raise HTTPException(413, "Image exceeds the 10 MB limit")
    try:
        with Image.open(io.BytesIO(data)) as checked:
            checked.verify()
            extension = "jpg" if checked.format == "JPEG" else checked.format.lower()
    except (UnidentifiedImageError, OSError) as exc:
        raise HTTPException(400, "Invalid image") from exc

    filename = f"{uuid4().hex}.{extension}"
    image_url = await _store_image(data, filename, image.content_type)
    return {"url": image_url, "filename": filename}


@router.post("/items/lost", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
def create_lost_item(
    payload: ItemCreate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem:
    return _save_item(payload, session, uid, LostItem)


@router.post("/items/found", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
def create_found_item(
    payload: ItemCreate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> FoundItem:
    return _save_item(payload, session, uid, FoundItem)


@router.post("/items/match", response_model=list[MatchResponse])
def match_items(
    request: MatchRequest,
    session: Session = Depends(get_db),
    _: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    found_item = session.get(FoundItem, request.found_item_id)
    if found_item is None:
        raise HTTPException(404, "Found item not found")

    candidates = session.scalars(
        select(LostItem).where(
            func.abs(LostItem.lat - found_item.lat) <= request.radius_degrees,
            func.abs(LostItem.lng - found_item.lng) <= request.radius_degrees,
        )
    ).all()
    found_words = set(WORD_PATTERN.findall(f"{found_item.title} {found_item.description}".lower()))
    results = []
    for item in candidates:
        lost_words = set(WORD_PATTERN.findall(f"{item.title} {item.description}".lower()))
        text_score = len(found_words & lost_words) / max(len(found_words | lost_words), 1)
        distance = abs(item.lat - found_item.lat) + abs(item.lng - found_item.lng)
        location_score = max(0.0, 1 - distance / (2 * request.radius_degrees))
        results.append({"item": item, "score": round(text_score * 0.7 + location_score * 0.3, 4)})
    return sorted(results, key=lambda result: float(result["score"]), reverse=True)[:25]