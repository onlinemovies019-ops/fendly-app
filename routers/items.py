import io
import math
import os
import re
from pathlib import Path
from uuid import uuid4

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status
import httpx
from PIL import Image, UnidentifiedImageError
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from ai_matching import create_embedding, item_text
from auth import get_current_user
from database import get_db
from image_matching import cosine_similarity, create_image_embedding
from moderation import moderate_content
from models import FoundItem, LostItem
from notifications import send_match_notifications
from routers.payments import verify_captured_payment
from schemas import ItemCreate, ItemResponse, MatchRequest, MatchResponse


router = APIRouter(prefix="/api", tags=["items"])
MAX_IMAGE_BYTES = 10 * 1024 * 1024
ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"}
WORD_PATTERN = re.compile(r"[a-z0-9]+")


async def _save_item(payload: ItemCreate, session: Session, uid: str, model: type[LostItem] | type[FoundItem]):
    if model is LostItem:
        if not payload.payment_id:
            raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "A valid payment is required")
        verify_captured_payment(payload.payment_id, uid)
    rejection_reason = await moderate_content(payload.title, payload.description)
    if rejection_reason:
        raise HTTPException(status_code=422, detail=rejection_reason)
    embedding = await create_embedding(item_text(payload.title, payload.description, payload.category))
    image_embedding = await create_image_embedding(payload.image_url)
    item_values = payload.model_dump(exclude={"payment_id"})
    record = model(**item_values, created_by=uid, embedding=embedding, image_embedding=image_embedding)
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

    if os.getenv("ENVIRONMENT", "development").lower() == "production":
        raise HTTPException(503, "Persistent image storage is not configured")

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
async def create_lost_item(
    payload: ItemCreate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem:
    return await _save_item(payload, session, uid, LostItem)


@router.post("/items/found", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_found_item(
    payload: ItemCreate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> FoundItem:
    return await _save_item(payload, session, uid, FoundItem)


@router.get("/items/mine")
async def list_my_items(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    lost_items = session.scalars(select(LostItem).where(LostItem.created_by == uid)).all()
    found_items = session.scalars(select(FoundItem).where(FoundItem.created_by == uid)).all()
    items = [("LOST", item) for item in lost_items] + [("FOUND", item) for item in found_items]
    items.sort(key=lambda pair: pair[1].created_at.timestamp() if pair[1].created_at else 0, reverse=True)
    return [
        {
            "id": item.id,
            "type": item_type,
            "title": item.title,
            "description": item.description,
            "category": item.category,
            "lat": item.lat,
            "lng": item.lng,
            "image_url": item.image_url,
            "created_at": item.created_at,
            "status": "Active" if item_type == "LOST" else "Published",
        }
        for item_type, item in items
    ]


@router.post("/items/match", response_model=list[MatchResponse])
async def match_items(
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
    found_embedding = found_item.embedding or await create_embedding(
        item_text(found_item.title, found_item.description, found_item.category)
    )
    found_image_embedding = found_item.image_embedding or await create_image_embedding(found_item.image_url)
    found_words = set(WORD_PATTERN.findall(f"{found_item.title} {found_item.description}".lower()))
    results = []
    for item in candidates:
        lost_words = set(WORD_PATTERN.findall(f"{item.title} {item.description}".lower()))
        keyword_score = len(found_words & lost_words) / max(len(found_words | lost_words), 1)
        semantic_score = keyword_score
        if found_embedding is not None and item.embedding is not None:
            dot_product = sum(left * right for left, right in zip(found_embedding, item.embedding))
            found_norm = math.sqrt(sum(value * value for value in found_embedding))
            item_norm = math.sqrt(sum(value * value for value in item.embedding))
            if found_norm and item_norm:
                semantic_score = max(0.0, min(1.0, dot_product / (found_norm * item_norm)))
        image_score = cosine_similarity(found_image_embedding, item.image_embedding)
        distance = abs(item.lat - found_item.lat) + abs(item.lng - found_item.lng)
        location_score = max(0.0, 1 - distance / (2 * request.radius_degrees))
        if image_score is None:
            score = semantic_score * 0.7 + location_score * 0.3
        else:
            score = semantic_score * 0.3 + image_score * 0.45 + keyword_score * 0.1 + location_score * 0.15
        results.append({"item": item, "score": round(score, 4)})
    ranked_results = sorted(results, key=lambda result: float(result["score"]), reverse=True)[:25]
    notified_uids = {
        item.created_by
        for result in ranked_results
        if float(result["score"]) >= 0.5
        for item in [result["item"]]
        if item.created_by != found_item.created_by
    }
    send_match_notifications(
        session,
        notified_uids,
        found_item.id,
        max((float(result["score"]) for result in ranked_results), default=0.0),
    )
    return ranked_results