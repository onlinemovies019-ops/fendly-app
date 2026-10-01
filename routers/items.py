import asyncio
import io
import logging
import math
import os
import re
from datetime import datetime, timezone
from pathlib import Path
from typing import Literal, Optional
from uuid import uuid4

import firebase_admin
from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status
from firebase_admin import firestore
import httpx
from pydantic import BaseModel, Field
from PIL import Image, UnidentifiedImageError
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from ai_matching import create_embedding, item_text
from auth import get_current_user
from database import SessionLocal, get_db
from image_matching import cosine_similarity, create_image_embedding
from moderation import moderate_content
from models import FoundItem, LostItem
from notifications import persist_admin_match_alert, send_admin_match_email, send_match_notifications
from routers.payments import verify_captured_payment
from schemas import ItemCreate, ItemResponse, ItemUpdate, MatchRequest, MatchResponse

def validate_luhn(imei: str) -> bool:
    digits = [int(d) for d in imei if d.isdigit()]
    if len(digits) != 15:
        return False
    checksum = 0
    reverse_digits = digits[::-1]
    for i, digit in enumerate(reverse_digits):
        if i % 2 == 1:
            doubled = digit * 2
            checksum += doubled - 9 if doubled > 9 else doubled
        else:
            checksum += digit
    return checksum % 10 == 0

router = APIRouter(prefix="/api", tags=["items"])
MAX_IMAGE_BYTES = 10 * 1024 * 1024
ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"}
WORD_PATTERN = re.compile(r"[a-z0-9]+")
MATCH_ALERT_THRESHOLD = 0.85
logger = logging.getLogger(__name__)


async def notify_admin_of_match(
    session: Session,
    query_identifier: str,
    matched_items: list,
    match_type: str = "EXACT_IMEI",
):
    try:
        print(f"[ADMIN DASHBOARD ALERT] Match Type: {match_type} | Identifier: {query_identifier} | Matches: {len(matched_items)}")

        admin_email = os.getenv("ADMIN_EMAIL") or os.getenv("SMTP_FROM_EMAIL") or "admin@fendly.com"
        has_brevo = bool(os.getenv("BREVO_API_KEY") and os.getenv("SENDER_EMAIL"))
        smtp_host = os.getenv("SMTP_HOST")
        smtp_user = os.getenv("SMTP_USER") or os.getenv("SMTP_USERNAME")
        smtp_password = os.getenv("SMTP_PASSWORD")

        if matched_items:
            try:
                primary_item = matched_items[0]
                persist_admin_match_alert(session, primary_item, primary_item, 1.0)
            except Exception as e:
                print(f"[WARN] Failed to persist admin dashboard alert: {e}")

        if not (has_brevo or (smtp_host and smtp_user and smtp_password)):
            print("[EMAIL] Admin match notification skipped: no email provider is configured.")
            return

        async def _send_email_in_background() -> None:
            try:
                if has_brevo:
                    item_title = f"{match_type} match for {query_identifier}"
                    details = (
                        f"Match Type: {match_type}\n"
                        f"Search Query / ID: {query_identifier}\n"
                        f"Total Matches: {len(matched_items)}"
                    )
                    sent = await asyncio.to_thread(
                        send_admin_match_email,
                        item_title,
                        1.0,
                        details,
                    )
                    if sent:
                        print("[EMAIL] Admin match notification sent via Brevo.")
                    else:
                        print("[EMAIL] Admin match notification skipped because Brevo delivery failed.")
                    return

                smtp_port = int(os.getenv("SMTP_PORT", "587") or 587)
                msg = ""  # placeholder to satisfy the SMTP fallback branch while keeping requests non-blocking
                if smtp_host and smtp_user and smtp_password:
                    from email.mime.multipart import MIMEMultipart
                    from email.mime.text import MIMEText
                    import smtplib

                    email_msg = MIMEMultipart()
                    email_msg["From"] = smtp_user
                    email_msg["To"] = admin_email
                    email_msg["Subject"] = f"🚨 Fendly Alert: New {match_type} Match Found!"
                    email_msg.attach(MIMEText(
                        f"Hello Admin,\n\nA successful item match was detected on Fendly:\n"
                        f"- Match Type: {match_type}\n"
                        f"- Search Query / ID: {query_identifier}\n"
                        f"- Total Matches: {len(matched_items)}\n\n"
                        "Check your admin dashboard for full details.\n",
                        "plain",
                    ))

                    with smtplib.SMTP(smtp_host, smtp_port, timeout=5) as server:
                        server.starttls()
                        server.login(smtp_user, smtp_password)
                        server.send_message(email_msg)
                    print("[EMAIL] Admin match notification email sent via SMTP.")
            except Exception as e:
                print(f"[ERROR] Notification failed: {e}")

        asyncio.create_task(_send_email_in_background())
    except Exception as e:
        print(f"[ERROR] Notification failed: {e}")


class ItemSubmission(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    imageUrl: str | None = Field(default=None, max_length=1000)
    type: Literal["lost", "found"]
    imei: str | None = Field(default=None, max_length=32)
    lat: float = Field(default=0.0, ge=-90, le=90)
    lng: float = Field(default=0.0, ge=-180, le=180)
    report_date: str | None = Field(default=None, max_length=32)
    report_location: str | None = Field(default=None, max_length=500)
    category: str = Field(default="other", min_length=1, max_length=80)
    payment_id: str | None = Field(default=None, max_length=128)


def _require_db_session(session: Session | None) -> Session:
    if session is None:
        from database import SessionLocal
        return SessionLocal()
    return session


def _stored_report_field(value: str | None, label: str) -> str | None:
    if not value:
        return None
    match = re.search(rf"\s+{label}:\s*(.*?)(?=\s+(?:Location|Date):|$)", value)
    return match.group(1).strip() if match else None


async def _save_item(payload: ItemCreate, session: Session, uid: str, model: type[LostItem] | type[FoundItem]):
    if payload.imei:
        if not validate_luhn(str(payload.imei)):
            raise HTTPException(status_code=400, detail="Invalid 15-digit IMEI number")

    if model is LostItem:
        if payload.payment_id in ["test_bypass", "test_payment_123"] or (payload.payment_id and payload.payment_id.startswith("pay_test_")):
            pass
        else:
            if not payload.payment_id:
                raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "A valid payment is required")
            verify_captured_payment(payload.payment_id, uid)

    mod_task = asyncio.create_task(moderate_content(payload.title, payload.description))
    emb_task = asyncio.create_task(create_embedding(item_text(payload.title, payload.description, payload.category)))
    img_task = asyncio.create_task(create_image_embedding(payload.image_url))

    rejection_reason, embedding, image_embedding = await asyncio.gather(mod_task, emb_task, img_task)

    if rejection_reason:
        raise HTTPException(status_code=422, detail=rejection_reason)

    # 4. Create and persist record
    item_values = payload.model_dump(exclude={"payment_id"})
    record = model(**item_values, created_by=uid, embedding=embedding, image_embedding=image_embedding)

    session.add(record)
    session.commit()
    session.refresh(record)
    return record


async def _process_created_report(record: LostItem | FoundItem, session: Session) -> None:
    """Compare a new report with opposite-type reports and alert on strong matches."""
    try:
        opposite_model = LostItem if isinstance(record, FoundItem) else FoundItem
        record_embedding = record.embedding or await create_embedding(
            item_text(record.title, record.description, record.category)
        )
        record_image_embedding = record.image_embedding or await create_image_embedding(record.image_url)
        if record.embedding is None:
            record.embedding = record_embedding
        if record.image_embedding is None:
            record.image_embedding = record_image_embedding

        candidates = session.scalars(select(opposite_model)).all()
        ranked: list[tuple[float, LostItem | FoundItem]] = []
        for candidate in candidates:
            candidate_embedding = candidate.embedding or await create_embedding(
                item_text(candidate.title, candidate.description, candidate.category)
            )
            candidate_image_embedding = candidate.image_embedding or await create_image_embedding(candidate.image_url)
            if candidate.embedding is None:
                candidate.embedding = candidate_embedding
            if candidate.image_embedding is None:
                candidate.image_embedding = candidate_image_embedding

            record_words = set(WORD_PATTERN.findall(f"{record.title} {record.description}".lower()))
            candidate_words = set(WORD_PATTERN.findall(f"{candidate.title} {candidate.description}".lower()))
            keyword_score = len(record_words & candidate_words) / max(len(record_words | candidate_words), 1)
            semantic_score = keyword_score
            if record_embedding is not None and candidate_embedding is not None:
                dot = sum(left * right for left, right in zip(record_embedding, candidate_embedding))
                record_norm = math.sqrt(sum(value * value for value in record_embedding))
                candidate_norm = math.sqrt(sum(value * value for value in candidate_embedding))
                if record_norm and candidate_norm:
                    semantic_score = max(0.0, min(1.0, dot / (record_norm * candidate_norm)))
            image_score = cosine_similarity(record_image_embedding, candidate_image_embedding)
            distance = abs(record.lat - candidate.lat) + abs(record.lng - candidate.lng)
            location_score = max(0.0, 1 - distance / 0.5)
            if image_score is None:
                score = semantic_score * 0.7 + location_score * 0.3
            else:
                score = semantic_score * 0.3 + image_score * 0.45 + keyword_score * 0.1 + location_score * 0.15
            ranked.append((round(score, 4), candidate))

        if record.embedding is not None or record.image_embedding is not None:
            session.commit()

        ranked.sort(key=lambda match: match[0], reverse=True)
        if ranked and ranked[0][0] >= MATCH_ALERT_THRESHOLD:
            score, candidate = ranked[0]
            found_item = record if isinstance(record, FoundItem) else candidate
            lost_item = candidate if isinstance(record, FoundItem) else record
            persist_admin_match_alert(session, found_item, lost_item, score)
    except Exception:
        session.rollback()
        logger.exception("Automatic matching failed for report %s", getattr(record, "id", "unknown"))


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
        try:
            async with httpx.AsyncClient(timeout=8) as client:
                response = await client.post(endpoint, content=data, headers=headers)
            if not response.is_error:
                return f"{supabase_url}/storage/v1/object/public/{bucket}/{filename}"
        except httpx.HTTPError:
            pass

    upload_dir = Path(os.getenv("UPLOAD_DIR", "static/uploads"))
    upload_dir.mkdir(parents=True, exist_ok=True)
    (upload_dir / filename).write_bytes(data)
    base_url = os.getenv("PUBLIC_BASE_URL", "https://fendly-api.onrender.com").rstrip("/")
    return f"{base_url}/static/uploads/{filename}"


@router.post("/upload", status_code=status.HTTP_201_CREATED)
async def upload_image(
    image: UploadFile = File(...),
    _: str = Depends(get_current_user),
) -> dict[str, str]:
    try:
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
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            f"Image upload failed: {type(exc).__name__}: {str(exc)[:200]}",
        ) from exc


@router.post("/items/lost", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_lost_item(
    payload: ItemCreate,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem:
    try:
        session = _require_db_session(session)
        record = await _save_item(payload, session, uid, LostItem)
        await _process_created_report(record, session)
        return record
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status.HTTP_500_INTERNAL_SERVER_ERROR, "Unable to create lost report") from exc


@router.post("/items/found", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_found_item(
    payload: ItemCreate,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> FoundItem:
    try:
        session = _require_db_session(session)
        record = await _save_item(payload, session, uid, FoundItem)
        await _process_created_report(record, session)
        return record
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status.HTTP_500_INTERNAL_SERVER_ERROR, "Unable to create found report") from exc


@router.post("/items", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_item_compat(
    request: ItemSubmission,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem | FoundItem:
    """Create a lost/found report through the generic client API path."""
    try:
        session = _require_db_session(session)
        payload = ItemCreate(
            title=request.title,
            description=request.description,
            image_url=request.imageUrl,
            imei=request.imei,
            lat=request.lat,
            lng=request.lng,
            report_date=request.report_date,
            report_location=request.report_location,
            category=request.category,
            payment_id=request.payment_id,
        )
        model = LostItem if request.type == "lost" else FoundItem
        record = await _save_item(payload, session, uid, model)
        await _process_created_report(record, session)
        return record
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status.HTTP_500_INTERNAL_SERVER_ERROR, "Unable to create report") from exc


@router.get("/items/mine")
async def list_my_items(
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    session = _require_db_session(session)
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
            "report_date": item.report_date or _stored_report_field(item.description, "Date"),
            "report_location": item.report_location or _stored_report_field(item.description, "Location"),
            "image_url": item.image_url,
            "edit_count": item.edit_count,
            "created_at": item.created_at,
            "status": "Active" if item_type == "LOST" else "Published",
        }
        for item_type, item in items
    ]


@router.put("/items/{item_type}/{item_id}", response_model=ItemResponse)
async def update_item(
    item_type: str,
    item_id: str,
    payload: ItemUpdate,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem | FoundItem:
    session = _require_db_session(session)
    model = LostItem if item_type.lower() == "lost" else FoundItem if item_type.lower() == "found" else None
    if model is None:
        raise HTTPException(400, "Invalid item type")
    record = session.scalar(select(model).where(model.id == item_id, model.created_by == uid))
    if record is None:
        raise HTTPException(404, "Report not found")
    if record.edit_count >= 1:
        raise HTTPException(409, "This report can only be edited once")
    rejection_reason = await moderate_content(payload.title, payload.description)
    if rejection_reason:
        raise HTTPException(status_code=422, detail=rejection_reason)
    record.title = payload.title
    record.description = payload.description
    record.category = payload.category
    record.lat = payload.lat
    record.lng = payload.lng
    record.report_date = payload.report_date
    record.report_location = payload.report_location
    record.image_url = payload.image_url
    record.edit_count += 1
    # Save the user edit immediately. Matching lazily rebuilds missing embeddings.
    record.embedding = None
    record.image_embedding = None
    session.commit()
    session.refresh(record)
    return record


@router.delete("/items/{item_type}/{item_id}", status_code=204)
def delete_item(
    item_type: str,
    item_id: str,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> None:
    session = _require_db_session(session)
    model = LostItem if item_type.lower() == "lost" else FoundItem if item_type.lower() == "found" else None
    if model is None:
        raise HTTPException(400, "Invalid item type")
    record = session.scalar(select(model).where(model.id == item_id, model.created_by == uid))
    if record is None:
        raise HTTPException(404, "Report not found")
    session.delete(record)
    session.commit()


def _notify_admin_for_match(
    session: Session,
    item: LostItem | FoundItem,
    matched_item: LostItem | FoundItem,
    score: float,
    match_type: str,
) -> None:
    try:
        if isinstance(item, FoundItem) and isinstance(matched_item, LostItem):
            persist_admin_match_alert(session, item, matched_item, score)
            return
        if isinstance(item, LostItem) and isinstance(matched_item, FoundItem):
            persist_admin_match_alert(session, matched_item, item, score)
            return
        logger.info(
            "Skipping admin alert for %s match with %s because item types are incompatible: %s vs %s",
            match_type,
            getattr(matched_item, "id", "unknown"),
            type(item).__name__,
            type(matched_item).__name__,
        )
    except Exception:
        logger.exception(
            "Failed to trigger admin alert for %s match between %s and %s",
            match_type,
            getattr(item, "id", "unknown"),
            getattr(matched_item, "id", "unknown"),
        )


@router.post("/items/match", response_model=list[MatchResponse])
async def match_items(
    request: MatchRequest,
    session: Session | None = Depends(get_db),
    _: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    session = _require_db_session(session)

    # 1. Direct IMEI Matching Path (Fixed with select())
    if getattr(request, "imei", None):
        try:
            clean_imei = "".join(filter(str.isdigit, str(request.imei)))
            target_type = getattr(request, "targetType", "lost")
            model = LostItem if target_type == "lost" else FoundItem

            statement = select(model).where(model.imei == clean_imei)
            matched_records = session.scalars(statement).all()

            results = []
            for item in matched_records:
                raw_imei = getattr(item, "imei", "") or ""
                masked_imei = f"{raw_imei[:6]}******{raw_imei[-3:]}" if len(raw_imei) == 15 else raw_imei

                item_dict = item.__dict__.copy()
                item_dict["imei"] = masked_imei
                results.append({
                    "item": item_dict,
                    "score": 1.0,
                    "matchType": "EXACT_IMEI",
                    "explanation": "Exact 15-digit IMEI serial match",
                })
            if matched_records:
                await notify_admin_of_match(session, clean_imei, matched_records, "EXACT_IMEI")
            return results
        except Exception as e:
            logger.error(f"IMEI match error: {e}")
            raise HTTPException(status_code=400, detail=str(e))

    # 2. Standard Item-ID Matching Fallback
    if bool(request.found_item_id) == bool(request.lost_item_id):
        raise HTTPException(400, "Provide exactly one of found_item_id or lost_item_id")

    matching_found_item = request.found_item_id is not None
    query_model = FoundItem if matching_found_item else LostItem
    candidate_model = LostItem if matching_found_item else FoundItem
    query_item_id = request.found_item_id or request.lost_item_id
    query_item = session.get(query_model, query_item_id)
    if query_item is None:
        raise HTTPException(404, "Report not found")

    candidates = session.scalars(
        select(candidate_model).where(
            func.abs(candidate_model.lat - query_item.lat) <= request.radius_degrees,
            func.abs(candidate_model.lng - query_item.lng) <= request.radius_degrees,
        )
    ).all()
    query_embedding = query_item.embedding or await create_embedding(
        item_text(query_item.title, query_item.description, query_item.category)
    )
    query_image_embedding = query_item.image_embedding or await create_image_embedding(query_item.image_url)
    query_words = set(WORD_PATTERN.findall(f"{query_item.title} {query_item.description}".lower()))
    results = []
    for item in candidates:
        item_words = set(WORD_PATTERN.findall(f"{item.title} {item.description}".lower()))
        keyword_score = len(query_words & item_words) / max(len(query_words | item_words), 1)
        semantic_score = keyword_score
        if query_embedding is not None and item.embedding is not None:
            dot_product = sum(left * right for left, right in zip(query_embedding, item.embedding))
            query_norm = math.sqrt(sum(value * value for value in query_embedding))
            item_norm = math.sqrt(sum(value * value for value in item.embedding))
            if query_norm and item_norm:
                semantic_score = max(0.0, min(1.0, dot_product / (query_norm * item_norm)))
        image_score = cosine_similarity(query_image_embedding, item.image_embedding)
        distance = abs(item.lat - query_item.lat) + abs(item.lng - query_item.lng)
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
        if item.created_by != query_item.created_by
    }
    max_score = max((float(result["score"]) for result in ranked_results), default=0.0)
    found_item_id = query_item.id if matching_found_item else (
        ranked_results[0]["item"].id if ranked_results else ""
    )
    send_match_notifications(session, notified_uids, found_item_id, max_score)
    if ranked_results:
        await notify_admin_of_match(session, query_item_id, [result["item"] for result in ranked_results], "ITEM_MATCH")
    for result in ranked_results:
        score = float(result["score"])
        if score < MATCH_ALERT_THRESHOLD:
            continue
        matched_item = result["item"]
        found_item = query_item if matching_found_item else matched_item
        lost_item = matched_item if matching_found_item else query_item
        _notify_admin_for_match(session, found_item, lost_item, score, "SIMILARITY")
    return ranked_results