import asyncio
import json
import math
import os
import re

import httpx
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy import desc, or_, select
from sqlalchemy.orm import Session

from ai_matching import create_embedding, item_text
from auth import get_current_user
from database import get_db
from image_matching import cosine_similarity, create_image_embedding
from models import AdminMatchAlert, FoundItem, LostItem, User
from notifications import persist_admin_match_alert, send_match_notifications
from schemas import ItemResponse
from translation import translate_report_fields


router = APIRouter(prefix="/api/admin", tags=["admin"])
WORD_PATTERN = re.compile(r"[a-z0-9]+")
ENGLISH_UNAVAILABLE = "English translation unavailable"


def _supabase_admin_alert_request(method: str, path: str, **kwargs):
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    if not supabase_url or not service_role_key:
        return None
    headers = {
        "apikey": service_role_key,
        "Authorization": f"Bearer {service_role_key}",
        "Content-Type": "application/json",
    }
    headers.update(kwargs.pop("headers", {}))
    try:
        response = httpx.request(
            method,
            f"{supabase_url}/rest/v1/{path}",
            headers=headers,
            timeout=10,
            **kwargs,
        )
        response.raise_for_status()
        return response
    except httpx.HTTPError as error:
        raise HTTPException(503, "Admin notifications are temporarily unavailable") from error


class NotifyRequest(BaseModel):
    recipient_uid: str = Field(min_length=1, max_length=128)
    score: float = Field(ge=0, le=1)


def _parse_admin_uids(raw_value: str | None) -> set[str]:
    if raw_value is None:
        return set()

    value = raw_value.strip()
    if not value:
        return set()
    if value in {"*", "all", "ALL"}:
        return {"*"}

    candidates = set()
    for token in re.findall(r"[A-Za-z0-9._:-]+", value):
        if token and token.lower() not in {"all", "admin", "admins"}:
            candidates.add(token)
    return candidates


def require_admin(uid: str = Depends(get_current_user)) -> str:
    allowed = _parse_admin_uids(os.getenv("ADMIN_FIREBASE_UIDS"))
    if not allowed:
        raise HTTPException(503, "Admin access is not configured")
    if "*" in allowed or uid in allowed:
        return uid
    print(f"[ADMIN AUTH] rejected uid={uid!r}; configured_admin_uids={sorted(allowed)[:10]}", flush=True)
    raise HTTPException(403, "Admin access required")


def _is_dummy_text(text: str | None) -> bool:
    if not text:
        return False
    val = text.lower()
    return "dummy" in val or "test" in val or "sample" in val or val.startswith("test_")


async def _ensure_english_translations(items: list[LostItem | FoundItem], session: Session) -> None:
    pending = [
        item for item in items
        if item.title_en is None
        or item.description_en is None
        or item.category_en is None
        or item.report_location_en is None
    ]
    if not pending:
        return

    semaphore = asyncio.Semaphore(4)

    async def translate_item(item: LostItem | FoundItem) -> dict[str, str] | None:
        async with semaphore:
            return await translate_report_fields(
                item.title,
                item.description,
                item.report_location,
                category=item.category,
                source_language=item.source_language,
            )

    translations = await asyncio.gather(*(translate_item(item) for item in pending))
    updated = False
    for item, translated in zip(pending, translations):
        if translated is None:
            continue
        item.title_en = translated["title"]
        item.description_en = translated["description"]
        item.report_location_en = translated["report_location"]
        item.category_en = translated["category"]
        updated = True
    if updated:
        session.commit()


def _english_report_fields(item: LostItem | FoundItem) -> dict[str, object]:
    return {
        "title": item.title_en if item.title_en is not None else ENGLISH_UNAVAILABLE,
        "description": item.description_en if item.description_en is not None else ENGLISH_UNAVAILABLE,
        "category": item.category_en if item.category_en is not None else ENGLISH_UNAVAILABLE,
        "report_location": (
            item.report_location_en
            if item.report_location_en is not None
            else ENGLISH_UNAVAILABLE if item.report_location else ""
        ),
        "translation_available": item.title_en is not None and item.description_en is not None,
    }


async def _translate_alert_titles(alerts: list[dict[str, object]], session: Session) -> list[dict[str, object]]:
    linked_items: dict[str, LostItem | FoundItem] = {}
    for alert in alerts:
        found_id = alert.get("found_item_id")
        lost_id = alert.get("lost_item_id")
        if found_id:
            item = session.get(FoundItem, str(found_id))
            if item is not None:
                linked_items[f"found:{found_id}"] = item
        if lost_id:
            item = session.get(LostItem, str(lost_id))
            if item is not None:
                linked_items[f"lost:{lost_id}"] = item

    await _ensure_english_translations(list(linked_items.values()), session)
    for alert in alerts:
        found_item = linked_items.get(f"found:{alert.get('found_item_id')}")
        lost_item = linked_items.get(f"lost:{alert.get('lost_item_id')}")
        alert["found_title"] = found_item.title_en if found_item and found_item.title_en is not None else ENGLISH_UNAVAILABLE
        alert["lost_title"] = lost_item.title_en if lost_item and lost_item.title_en is not None else ENGLISH_UNAVAILABLE
        alert["reason"] = f"Found '{alert['found_title']}' may match lost report '{alert['lost_title']}'."
    return alerts


@router.get("/items")
async def list_all_items(
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, object]]:
    lost = session.scalars(select(LostItem)).all()
    found = session.scalars(select(FoundItem)).all()
    items = [("LOST", item) for item in lost] + [("FOUND", item) for item in found]
    items = [
        (item_type, item) for item_type, item in items
        if not (_is_dummy_text(item.title) or _is_dummy_text(item.description) or _is_dummy_text(item.created_by))
    ]
    await _ensure_english_translations([item for _, item in items], session)
    items.sort(key=lambda pair: pair[1].created_at.timestamp() if pair[1].created_at else 0, reverse=True)
    return [
        {
            "id": item.id,
            "type": item_type,
            **_english_report_fields(item),
            "lat": item.lat,
            "lng": item.lng,
            "report_date": item.report_date,
            "image_url": item.image_url,
            "created_by": item.created_by,
            "created_at": item.created_at,
        }
        for item_type, item in items
    ]


@router.get("/alerts")
async def list_match_alerts(
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> list[dict[str, object]]:
    response = _supabase_admin_alert_request(
        "GET",
        "admin_match_alerts",
        params={"select": "*", "order": "created_at.desc", "limit": "50"},
    )
    if response is not None:
        raw_alerts = response.json()
        if isinstance(raw_alerts, list):
            alerts = [
                alert for alert in raw_alerts
                if not (_is_dummy_text(alert.get("found_title")) or _is_dummy_text(alert.get("lost_title")))
            ]
            return await _translate_alert_titles(alerts, session)
        return raw_alerts

    alerts = session.scalars(
        select(AdminMatchAlert).order_by(desc(AdminMatchAlert.created_at)).limit(50)
    ).all()
    alerts = [
        {
            "id": alert.id,
            "found_item_id": alert.found_item_id,
            "lost_item_id": alert.lost_item_id,
            "found_title": alert.found_title,
            "lost_title": alert.lost_title,
            "confidence": alert.confidence,
            "reason": alert.reason,
            "is_read": alert.is_read,
            "email_sent": alert.email_sent,
            "created_at": alert.created_at.isoformat() if alert.created_at else None,
        }
        for alert in alerts
        if not (_is_dummy_text(alert.found_title) or _is_dummy_text(alert.lost_title))
    ]
    return await _translate_alert_titles(alerts, session)


@router.post("/alerts/{alert_id}/read", status_code=204)
def mark_match_alert_read(
    alert_id: str,
    session: Session = Depends(get_db),
    _: str = Depends(require_admin),
) -> None:
    response = _supabase_admin_alert_request(
        "PATCH",
        "admin_match_alerts",
        params={"id": f"eq.{alert_id}"},
        json={"is_read": True},
        headers={"Prefer": "return=representation"},
    )
    if response is not None:
        if not response.json():
            raise HTTPException(404, "Alert not found")
        return

    alert = session.get(AdminMatchAlert, alert_id)
    if alert is None:
        raise HTTPException(404, "Alert not found")
    alert.is_read = True
    session.commit()


@router.get("/search")
async def search_users_and_reports(
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
        report_items: list[tuple[str, LostItem | FoundItem]] = []
        for item_type, model in (("LOST", LostItem), ("FOUND", FoundItem)):
            items = session.scalars(select(model).where(model.created_by == user.firebase_uid)).all()
            report_items.extend((item_type, item) for item in items)
        await _ensure_english_translations([item for _, item in report_items], session)
        reports.extend({
            "id": item.id,
            "type": item_type,
            **_english_report_fields(item),
            "lat": item.lat,
            "lng": item.lng,
            "image_url": item.image_url,
            "created_at": item.created_at,
        } for item_type, item in report_items)
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
    lost_items = session.scalars(select(LostItem)).all()
    await _ensure_english_translations([found_item, *lost_items], session)
    found_words = set(WORD_PATTERN.findall(f"{found_item.title} {found_item.description}".lower()))
    found_embedding = found_item.embedding or await create_embedding(
        item_text(found_item.title, found_item.description, found_item.category)
    )
    found_image_embedding = found_item.image_embedding or await create_image_embedding(found_item.image_url)
    matches = []
    for item in lost_items:
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
    ranked = sorted(matches, key=lambda result: float(result["score"]), reverse=True)[:25]
    for result in ranked:
        score = float(result["score"])
        if score >= 0.85:
            persist_admin_match_alert(session, found_item, result["item"], score)
    for result in ranked:
        item = result["item"]
        item_response = ItemResponse.model_validate(item).model_dump()
        item_response.update(_english_report_fields(item))
        result["item"] = item_response
    return ranked


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
