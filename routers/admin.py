import math
import os
import re

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from ai_matching import create_embedding, item_text
from auth import get_current_user
from database import get_db
from image_matching import cosine_similarity, create_image_embedding
from models import FoundItem, LostItem, User
from notifications import send_match_notifications


router = APIRouter(prefix="/api/admin", tags=["admin"])
WORD_PATTERN = re.compile(r"[a-z0-9]+")


class NotifyRequest(BaseModel):
    recipient_uid: str = Field(min_length=1, max_length=128)
    score: float = Field(ge=0, le=1)


def require_admin(uid: str = Depends(get_current_user)) -> str:
    allowed = {value.strip() for value in os.getenv("ADMIN_FIREBASE_UIDS", "").split(",") if value.strip()}
    if not allowed:
        raise HTTPException(503, "Admin access is not configured")
    if uid not in allowed:
        raise HTTPException(403, "Admin access required")
    return uid


@router.get("/items")
def list_all_items(
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, object]]:
    lost = session.scalars(select(LostItem)).all()
    found = session.scalars(select(FoundItem)).all()
    items = [("LOST", item) for item in lost] + [("FOUND", item) for item in found]
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
            "created_by": item.created_by,
            "created_at": item.created_at,
        }
        for item_type, item in items
    ]


@router.get("/search")
def search_users_and_reports(
    q: str,
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, object]]:
    query = q.strip().lower()
    if len(query) < 2:
        return []
    users = session.scalars(select(User).where(
        or_(User.username.ilike(f"%{query}%"), User.full_name.ilike(f"%{query}%"), User.email.ilike(f"%{query}%"), User.mobile.ilike(f"%{query}%"))
    )).all()
    results = []
    for user in users:
        reports = []
        for item_type, model in (("LOST", LostItem), ("FOUND", FoundItem)):
            items = session.scalars(select(model).where(model.created_by == user.firebase_uid)).all()
            reports.extend({
                "id": item.id,
                "type": item_type,
                "title": item.title,
                "description": item.description,
                "category": item.category,
                "lat": item.lat,
                "lng": item.lng,
                "image_url": item.image_url,
                "created_at": item.created_at,
            } for item in items)
        reports.sort(key=lambda item: item["created_at"].timestamp() if item["created_at"] else 0, reverse=True)
        results.append({
            "user": {"uid": user.firebase_uid, "username": user.username, "full_name": user.full_name, "email": user.email, "mobile": user.mobile},
            "reports": reports,
        })
    return results


@router.get("/matches/{found_item_id}")
async def find_matches(
    found_item_id: str,
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, object]]:
    found_item = session.get(FoundItem, found_item_id)
    if found_item is None:
        raise HTTPException(404, "Found item not found")
    found_words = set(WORD_PATTERN.findall(f"{found_item.title} {found_item.description}".lower()))
    found_embedding = found_item.embedding or await create_embedding(
        item_text(found_item.title, found_item.description, found_item.category)
    )
    found_image_embedding = found_item.image_embedding or await create_image_embedding(found_item.image_url)
    matches = []
    for item in session.scalars(select(LostItem)).all():
        lost_words = set(WORD_PATTERN.findall(f"{item.title} {item.description}".lower()))
        keyword_score = len(found_words & lost_words) / max(len(found_words | lost_words), 1)
        semantic_score = keyword_score
        if found_embedding is not None and item.embedding is not None:
            dot = sum(left * right for left, right in zip(found_embedding, item.embedding))
            found_norm = math.sqrt(sum(value * value for value in found_embedding))
            item_norm = math.sqrt(sum(value * value for value in item.embedding))
            if found_norm and item_norm:
                semantic_score = max(0.0, min(1.0, dot / (found_norm * item_norm)))
        image_score = cosine_similarity(found_image_embedding, item.image_embedding)
        distance = abs(item.lat - found_item.lat) + abs(item.lng - found_item.lng)
        location_score = max(0.0, 1 - distance / 0.5)
        if image_score is None:
            score = semantic_score * 0.7 + location_score * 0.3
        else:
            score = semantic_score * 0.3 + image_score * 0.45 + keyword_score * 0.1 + location_score * 0.15
        score = round(score, 4)
        matches.append({"item": item, "score": score})
    return sorted(matches, key=lambda result: float(result["score"]), reverse=True)[:25]


@router.post("/matches/{found_item_id}/notify")
def notify_owner(
    found_item_id: str,
    payload: NotifyRequest,
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> dict[str, int]:
    if session.get(FoundItem, found_item_id) is None:
        raise HTTPException(404, "Found item not found")
    count = send_match_notifications(session, {payload.recipient_uid}, found_item_id, payload.score)
    return {"sent": count}
