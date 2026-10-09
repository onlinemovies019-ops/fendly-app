import asyncio
import gzip
import json
import io
import logging
import math
import os
import re
from datetime import datetime, timedelta, timezone
from functools import lru_cache
from pathlib import Path
from typing import Literal, Optional
from urllib.parse import urlsplit
from uuid import uuid4

import firebase_admin
from fastapi import APIRouter, BackgroundTasks, Depends, File, HTTPException, Query, UploadFile, status
from firebase_admin import firestore
import httpx
from pydantic import BaseModel, Field
from PIL import Image, UnidentifiedImageError
from sqlalchemy import delete, func, or_, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from ai_matching import create_embedding, item_text
from auth import get_current_user
from database import SessionLocal, get_db
from image_matching import cosine_similarity, create_image_embedding
from imei_security import imei_digest, validate_imei
from moderation import moderate_content
from models import AdminMatchAlert, ContentReport, FoundItem, LostItem, SocialPublication, UserNotification
from notifications import persist_admin_match_alert, send_admin_match_email, send_match_notifications
from routers.payments import require_lost_report_entitlement
from schemas import ItemCreate, ItemResponse, ItemUpdate, MatchPreview, MatchRequest, MatchResponse
from social_publishing import schedule_report_publications
from social_poster import render_report_poster
from translation import translate_report_fields

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
    match_type: str = "ITEM_MATCH",
):
    try:
        print(f"[ADMIN DASHBOARD ALERT] Match Type: {match_type} | Identifier: {query_identifier} | Matches: {len(matched_items)}")

        admin_email = os.getenv("ADMIN_EMAIL") or os.getenv("SMTP_FROM_EMAIL") or "admin@fendly.com"
        has_brevo = bool(os.getenv("BREVO_API_KEY") and os.getenv("SENDER_EMAIL"))
        smtp_host = os.getenv("SMTP_HOST")
        smtp_user = os.getenv("SMTP_USER") or os.getenv("SMTP_USERNAME")
        smtp_password = os.getenv("SMTP_PASSWORD")

        alert_created = False
        if matched_items:
            try:
                primary_item = matched_items[0]
                if hasattr(primary_item, "id"):
                    alert_created = persist_admin_match_alert(session, primary_item, primary_item, 1.0)
                    if not alert_created:
                        print("[EMAIL] Duplicate admin alert suppressed; dashboard alert already exists.")
                        return
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


async def _find_cloudinary_image_matches(
    image_url: str,
    target_type: str,
    session: Session,
) -> list[dict[str, object]]:
    function_url = os.getenv(
        "IMAGE_MATCHING_FUNCTION_URL",
        "https://us-central1-fendly-6d746.cloudfunctions.net/matchReportImages",
    )
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY")
    if not service_role_key:
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Image matching is not configured")

    try:
        async with httpx.AsyncClient(timeout=240) as client:
            response = await client.post(
                function_url,
                json={"imageUrl": image_url, "targetType": target_type},
                headers={"Authorization": f"Bearer {service_role_key}"},
            )
        response.raise_for_status()
        matches = response.json().get("matches", [])
    except (httpx.HTTPError, ValueError, TypeError) as error:
        logger.exception("Cloudinary image matching request failed")
        raise HTTPException(status.HTTP_502_BAD_GATEWAY, "Image matching is temporarily unavailable") from error

    if not isinstance(matches, list):
        raise HTTPException(status.HTTP_502_BAD_GATEWAY, "Image matcher returned an invalid response")

    model = LostItem if target_type == "lost" else FoundItem
    source_ids = [str(match.get("source_id")) for match in matches if isinstance(match, dict) and match.get("source_id")]
    if not source_ids:
        return []

    records = session.scalars(select(model).where(model.id.in_(source_ids))).all()
    records_by_id = {str(record.id): record for record in records}
    results = []
    for match in matches:
        if not isinstance(match, dict):
            continue
        record = records_by_id.get(str(match.get("source_id", "")))
        if record is None:
            continue
        results.append({
            "item": record,
            "score": max(0.0, min(1.0, float(match.get("similarity", 0.0)))),
            "matchType": "IMAGE",
        })
    return results


class ItemSubmission(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    source_language: str = Field(default="auto", max_length=16)
    imageUrl: str | None = Field(default=None, max_length=1000)
    imageUrls: list[str] = Field(default_factory=list, max_length=3)
    type: Literal["lost", "found"]
    lat: float = Field(default=0.0, ge=-90, le=90)
    lng: float = Field(default=0.0, ge=-180, le=180)
    report_date: str | None = Field(default=None, max_length=32)
    report_location: str | None = Field(default=None, max_length=500)
    category: str = Field(default="other", min_length=1, max_length=80)
    social_share_consent: bool = False
    community_guidelines_accepted: bool = False
    payment_id: str | None = Field(default=None, max_length=128)
    imei_number: str | None = Field(default=None, exclude=True)


class ContentReportRequest(BaseModel):
    reason: Literal["inappropriate", "spam", "personal_information", "fraud", "other"]
    details: str | None = Field(default=None, max_length=1000)


def _require_db_session(session: Session | None) -> Session:
    if session is None:
        from database import SessionLocal
        return SessionLocal()
    return session


def _validate_report_image_urls(image_urls: list[str]) -> None:
    public_base = os.getenv("PUBLIC_BASE_URL", "https://fendly-api.onrender.com").rstrip("/")
    supabase_base = os.getenv("SUPABASE_URL", "").rstrip("/")
    cloudinary_name = os.getenv("CLOUDINARY_CLOUD_NAME", "")
    cloudinary_url = os.getenv("CLOUDINARY_URL", "")
    is_production = os.getenv("ENVIRONMENT", "development").lower() == "production"
    if not cloudinary_name and cloudinary_url:
        cloudinary_name = urlsplit(cloudinary_url).hostname or ""
        cloudinary_name = cloudinary_name.removeprefix("api.")

    allowed_origins = {
        (parsed.scheme, parsed.netloc)
        for value in (public_base, supabase_base)
        if value
        for parsed in [urlsplit(value)]
    }
    for image_url in image_urls:
        parsed = urlsplit(image_url)
        is_local_http_upload = (
            not is_production
            and parsed.scheme == "http"
            and parsed.hostname in {"localhost", "127.0.0.1", "::1"}
        )
        if (
            (parsed.scheme != "https" and not is_local_http_upload)
            or not parsed.path
            or parsed.username
            or parsed.password
            or parsed.query
            or parsed.fragment
        ):
            raise HTTPException(status.HTTP_400_BAD_REQUEST, "Report images must use a Fendly upload URL")

        is_fendly_upload = (parsed.scheme, parsed.netloc) in allowed_origins
        is_supabase_upload = (
            bool(supabase_base)
            and (parsed.scheme, parsed.netloc) == (
                urlsplit(supabase_base).scheme,
                urlsplit(supabase_base).netloc,
            )
        )
        is_cloudinary_upload = (
            bool(cloudinary_name)
            and parsed.scheme == "https"
            and parsed.netloc == "res.cloudinary.com"
            and parsed.path.startswith(f"/{cloudinary_name}/image/upload/")
        )
        if not (is_fendly_upload or is_supabase_upload or is_cloudinary_upload):
            raise HTTPException(status.HTTP_400_BAD_REQUEST, "Report images must use a Fendly upload URL")


def _stored_report_field(value: str | None, label: str) -> str | None:
    if not value:
        return None
    match = re.search(rf"\s+{label}:\s*(.*?)(?=\s+(?:Location|Date):|$)", value)
    return match.group(1).strip() if match else None


ITEM_CATEGORY_KEYWORDS = (
    ("Electronics", (
        "smartphone", "mobile phone", "cell phone", "phone", "mobile", "television",
        "computer mouse", "wireless mouse", "laptop", "computer", "tablet", "charger",
        "headphone", "earphone", "camera", "smartwatch", "smart watch", "refrigerator",
        "fridge", "washing machine", "microwave", "speaker", "remote", "router", "monitor",
        "printer", "keyboard", "tube light", "led light", "light bulb", "flashlight", "torch",
        "lamp", "tv", "fan", "bulb",
    )),
    ("Animals", (
        "animal", "dog", "puppy", "cat", "kitten", "mouse", "mice", "cow", "goat", "sheep",
        "horse", "bird", "parrot", "rabbit", "pet", "fish", "snake",
    )),
    ("People", (
        "missing person", "person", "people", "man", "men", "woman", "women", "male", "female",
        "boy", "boys", "girl", "girls", "kid", "kids", "child", "children", "toddler",
    )),
    ("Apparels and accessories", (
        "wallet", "purse", "handbag", "backpack", "bag", "belt", "spectacles", "sunglasses",
        "goggles", "glasses", "eyeglasses", "spects", "specs", "clothing", "clothes", "apparel",
        "shirt", "trousers", "pants", "dress", "jacket", "coat", "shoes", "sandals", "footwear",
        "cap", "hat", "scarf", "gloves", "umbrella",
    )),
    ("Automobile", (
        "auto-rickshaw", "motorcycle", "motorbike", "bicycle", "scooter", "scooty", "moped",
        "vehicle", "tractor", "truck", "bus", "car", "bike", "cycle", "van", "auto",
    )),
    ("Documents", (
        "identity card", "id card", "passport", "driver license", "driving license", "certificate",
        "document", "aadhaar", "pan card", "license", "paper",
    )),
    ("Jewelry", (
        "necklace", "bracelet", "earring", "jewelry", "jewellery", "bangle", "ring", "gold chain",
    )),
    ("Keys", ("keychain", "keys", "key")),
    ("Household items", (
        "furniture", "utensils", "cookware", "sofa", "chair", "table", "bed", "pillow", "blanket",
        "curtain", "mattress", "kitchen appliance",
    )),
    ("Sports equipment", (
        "cricket bat", "tennis racket", "football", "basketball", "volleyball", "sports equipment",
        "racket", "bat", "ball",
    )),
    ("Toys", ("stuffed toy", "teddy bear", "toy", "doll", "puzzle")),
    ("Tools", ("screwdriver", "wrench", "hammer", "drill", "toolbox", "tool")),
    ("Medical items", ("medicine", "medication", "medical device", "inhaler", "blood pressure monitor")),
)


def infer_item_category(title: str, description: str) -> str:
    for source_text in (title, description):
        searchable_text = re.sub(r"\s+", " ", source_text or "").casefold()
        for category, keywords in ITEM_CATEGORY_KEYWORDS:
            for keyword in keywords:
                if re.search(rf"(?<![a-z0-9]){re.escape(keyword)}(?![a-z0-9])", searchable_text):
                    return category
    return "other"


def resolve_item_category(category: str | None, title: str, description: str) -> str:
    provided_category = (category or "").strip()
    if provided_category and provided_category.casefold() != "other":
        return provided_category
    return infer_item_category(title, description)


async def _save_item(
    payload: ItemCreate,
    session: Session,
    uid: str,
    model: type[LostItem] | type[FoundItem],
    background_tasks: BackgroundTasks | None = None,
):
    if not payload.community_guidelines_accepted:
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            "You must accept the Community Guidelines before submitting a report",
        )
    if payload.imei_number is not None:
        if model is not LostItem:
            raise HTTPException(status.HTTP_400_BAD_REQUEST, "IMEI can only be attached to a lost-item report")
        validate_imei(payload.imei_number)

    category = resolve_item_category(payload.category, payload.title, payload.description)
    if model is LostItem:
        require_lost_report_entitlement(session, uid, payload.payment_id)

    report_image_urls = list(dict.fromkeys(
        [*payload.image_urls, *([payload.image_url] if payload.image_url else [])]
    ))
    _validate_report_image_urls(report_image_urls)
    mod_task = asyncio.create_task(moderate_content(
        payload.title,
        payload.description,
        image_urls=report_image_urls,
    ))
    emb_task = asyncio.create_task(create_embedding(item_text(payload.title, payload.description, category)))
    img_task = asyncio.create_task(create_image_embedding(payload.image_url))
    translation_task = asyncio.create_task(translate_report_fields(
        payload.title,
        payload.description,
        payload.report_location,
        category=category,
        source_language=payload.source_language,
    ))

    rejection_reason, embedding, image_embedding, translated_fields = await asyncio.gather(
        mod_task, emb_task, img_task, translation_task
    )

    if rejection_reason:
        raise HTTPException(status_code=422, detail=rejection_reason)

    if category == "other" and translated_fields:
        category = infer_item_category(translated_fields.get("title", ""), translated_fields.get("description", ""))
        if category != "other":
            embedding = await create_embedding(item_text(payload.title, payload.description, category))
            translated_fields["category"] = category

    # 4. Create and persist record
    item_values = payload.model_dump(
        exclude={"payment_id", "imei_number", "community_guidelines_accepted"}
    )
    item_values["category"] = category
    item_values["image_url"] = payload.image_url or (payload.image_urls[0] if payload.image_urls else None)
    if model is LostItem:
        item_values["status"] = "LOST"
        item_values["imei_hash"] = imei_digest(payload.imei_number) if payload.imei_number else None
    item_values.update(
        title_en=translated_fields["title"] if translated_fields else None,
        description_en=translated_fields["description"] if translated_fields else None,
        report_location_en=translated_fields["report_location"] if translated_fields else None,
        category_en=translated_fields["category"] if translated_fields else None,
    )
    record = model(**item_values, created_by=uid, embedding=embedding, image_embedding=image_embedding)

    session.add(record)
    session.flush()
    if record.social_share_consent:
        try:
            poster_bytes = await render_report_poster(
                record,
                "lost" if model is LostItem else "found",
            )
            record.social_poster_url = await _store_image(
                poster_bytes,
                f"{uuid4().hex}.jpg",
                "image/jpeg",
            )
        except Exception:
            logger.exception("Could not generate social poster for report %s", record.id)
    schedule_report_publications(
        session,
        record,
        "lost" if model is LostItem else "found",
        background_tasks,
    )
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
                image_format = checked.format
        except (UnidentifiedImageError, OSError) as exc:
            raise HTTPException(400, "Invalid image") from exc

        image_content_type = {
            "JPEG": "image/jpeg",
            "PNG": "image/png",
            "WEBP": "image/webp",
        }.get(image_format)
        if image_content_type is None:
            raise HTTPException(415, "Only JPEG, PNG, and WebP images are supported")
        extension = "jpg" if image_format == "JPEG" else image_format.lower()
        rejection_reason = await moderate_content(
            "",
            "",
            image_data=data,
            image_content_type=image_content_type,
        )
        if rejection_reason:
            raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, rejection_reason)

        filename = f"{uuid4().hex}.{extension}"
        image_url = await _store_image(data, filename, image_content_type)
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
    background_tasks: BackgroundTasks,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem:
    try:
        session = _require_db_session(session)
        record = await _save_item(payload, session, uid, LostItem, background_tasks)
        await _process_created_report(record, session)
        return record
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status.HTTP_500_INTERNAL_SERVER_ERROR, "Unable to create lost report") from exc


@router.post("/items/found", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_found_item(
    payload: ItemCreate,
    background_tasks: BackgroundTasks,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> FoundItem:
    try:
        session = _require_db_session(session)
        record = await _save_item(payload, session, uid, FoundItem, background_tasks)
        await _process_created_report(record, session)
        return record
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status.HTTP_500_INTERNAL_SERVER_ERROR, "Unable to create found report") from exc


@router.post("/items", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_item_compat(
    request: ItemSubmission,
    background_tasks: BackgroundTasks,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem | FoundItem:
    """Create a lost/found report through the generic client API path."""
    try:
        session = _require_db_session(session)
        payload = ItemCreate(
            title=request.title,
            description=request.description,
            source_language=request.source_language,
            image_url=request.imageUrl,
            image_urls=request.imageUrls,
            social_share_consent=request.social_share_consent,
            community_guidelines_accepted=request.community_guidelines_accepted,
            lat=request.lat,
            lng=request.lng,
            report_date=request.report_date,
            report_location=request.report_location,
            category=request.category,
            payment_id=request.payment_id,
            imei_number=request.imei_number,
        )
        model = LostItem if request.type == "lost" else FoundItem
        record = await _save_item(payload, session, uid, model, background_tasks)
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
    lost_ids = {item.id for item in lost_items}
    found_ids = {item.id for item in found_items}
    recovered_statuses = {"RECOVERED", "REUNITED", "CLOSED"}
    recovered_lost_ids = {
        item.id for item in lost_items
        if (item.status or "").strip().upper() in recovered_statuses
    }
    matched_report_ids: set[str] = set()
    recovered_report_ids = set(recovered_lost_ids)
    if lost_ids or found_ids:
        match_alerts = session.scalars(
            select(AdminMatchAlert).where(
                or_(
                    AdminMatchAlert.lost_item_id.in_(lost_ids) if lost_ids else False,
                    AdminMatchAlert.found_item_id.in_(found_ids) if found_ids else False,
                )
            )
        ).all()
        linked_lost_ids = {alert.lost_item_id for alert in match_alerts}
        if linked_lost_ids:
            linked_lost_items = session.scalars(
                select(LostItem).where(LostItem.id.in_(linked_lost_ids))
            ).all()
            recovered_lost_ids.update(
                item.id for item in linked_lost_items
                if (item.status or "").strip().upper() in recovered_statuses
            )
        for alert in match_alerts:
            if alert.lost_item_id in recovered_lost_ids:
                recovered_report_ids.add(alert.found_item_id)
            if (alert.review_status or "").strip().lower() in {"pending", "confirmed"}:
                matched_report_ids.update((alert.lost_item_id, alert.found_item_id))

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
            "image_urls": item.image_urls or ([item.image_url] if item.image_url else []),
            "edit_count": item.edit_count,
            "created_at": item.created_at,
            "status": "Active" if item_type == "LOST" else "Published",
            "workflow_stage": (
                4 if item.id in recovered_report_ids
                else 3 if item.id in matched_report_ids
                else 2
            ),
        }
        for item_type, item in items
    ]


@router.get("/reports")
def list_public_reports(
    city: str | None = Query(default=None, max_length=120),
    session: Session | None = Depends(get_db),
) -> list[dict[str, object]]:
    session = _require_db_session(session)
    normalized_city = city.strip().casefold() if city else ""
    escaped_city = normalized_city.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    reports: list[tuple[str, LostItem | FoundItem]] = []
    for item_type, model in (("LOST", LostItem), ("FOUND", FoundItem)):
        query = select(model).where(model.hidden_from_public.is_(False))
        if model is LostItem:
            query = query.where(LostItem.status == "LOST")
        if escaped_city:
            query = query.where(
                func.lower(model.report_location).like(f"%{escaped_city}%", escape="\\")
            )
        matching_items = session.scalars(query).all()
        if escaped_city:
            matching_items = [
                item for item in matching_items
                if _is_indian_coordinate(item.lat, item.lng)
            ]
        reports.extend((item_type, item) for item in matching_items)

    reports.sort(
        key=lambda pair: pair[1].created_at.timestamp() if pair[1].created_at else 0,
        reverse=True,
    )
    return [
        {
            "id": item.id,
            "type": item_type,
            "title": item.title,
            "category": item.category,
            "report_date": item.report_date or _stored_report_field(item.description, "Date"),
            "report_location": item.report_location or _stored_report_field(item.description, "Location"),
            "image_url": item.image_url,
            "image_urls": item.image_urls or ([item.image_url] if item.image_url else []),
            "created_at": item.created_at,
            "status": "Active" if item_type == "LOST" else "Published",
        }
        for item_type, item in reports
    ]


@lru_cache(maxsize=1)
def _india_boundary() -> list[list[list[list[float]]]]:
    # Natural Earth 1:10m India boundary (public domain), stored locally for offline checks.
    boundary_path = Path(__file__).with_name("india_boundary.geojson.gz")
    with gzip.open(boundary_path, "rt", encoding="utf-8") as boundary_file:
        geometry = json.load(boundary_file)
    if geometry.get("type") != "MultiPolygon":
        raise ValueError(f"Unexpected India boundary geometry: {geometry.get('type')!r}")
    return geometry["coordinates"]


def _point_in_ring(longitude: float, latitude: float, ring: list[list[float]]) -> bool:
    inside = False
    previous_longitude, previous_latitude = ring[-1]
    for current_longitude, current_latitude in ring:
        cross_product = (
            (longitude - previous_longitude) * (current_latitude - previous_latitude)
            - (latitude - previous_latitude) * (current_longitude - previous_longitude)
        )
        if (
            abs(cross_product) <= 1e-9
            and min(previous_longitude, current_longitude) - 1e-9 <= longitude
            <= max(previous_longitude, current_longitude) + 1e-9
            and min(previous_latitude, current_latitude) - 1e-9 <= latitude
            <= max(previous_latitude, current_latitude) + 1e-9
        ):
            return True

        if (current_latitude > latitude) != (previous_latitude > latitude):
            crossing_longitude = (
                (previous_longitude - current_longitude)
                * (latitude - current_latitude)
                / (previous_latitude - current_latitude)
                + current_longitude
            )
            if longitude < crossing_longitude:
                inside = not inside
        previous_longitude, previous_latitude = current_longitude, current_latitude
    return inside


def _is_indian_coordinate(latitude: float, longitude: float) -> bool:
    for polygon in _india_boundary():
        if not polygon or not _point_in_ring(longitude, latitude, polygon[0]):
            continue
        if not any(_point_in_ring(longitude, latitude, hole) for hole in polygon[1:]):
            return True
    return False


@router.put("/items/{item_type}/{item_id}", response_model=ItemResponse)
async def update_item(
    item_type: str,
    item_id: str,
    payload: ItemUpdate,
    session: Session | None = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> LostItem | FoundItem:
    session = _require_db_session(session)
    if not payload.community_guidelines_accepted:
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            "You must accept the Community Guidelines before updating a report",
        )
    model = LostItem if item_type.lower() == "lost" else FoundItem if item_type.lower() == "found" else None
    if model is None:
        raise HTTPException(400, "Invalid item type")
    record = session.scalar(select(model).where(model.id == item_id, model.created_by == uid))
    if record is None:
        raise HTTPException(404, "Report not found")
    if record.edit_count >= 1:
        raise HTTPException(409, "This report can only be edited once")
    if record.created_at:
        now = datetime.now(timezone.utc)
        created_at = record.created_at if record.created_at.tzinfo else record.created_at.replace(tzinfo=timezone.utc)
        if (now - created_at) > timedelta(hours=5):
            raise HTTPException(409, "Reports can only be edited within 5 hours of creation")
    report_image_urls = payload.image_urls or (
        [payload.image_url] if payload.image_url else
        record.image_urls or ([record.image_url] if record.image_url else [])
    )
    report_image_urls = list(dict.fromkeys(report_image_urls))
    _validate_report_image_urls(report_image_urls)
    rejection_reason = await moderate_content(
        payload.title,
        payload.description,
        image_urls=report_image_urls,
    )
    if rejection_reason:
        raise HTTPException(status_code=422, detail=rejection_reason)
    category = resolve_item_category(payload.category, payload.title, payload.description)
    translated_fields = await translate_report_fields(
        payload.title,
        payload.description,
        payload.report_location,
        category=category,
        source_language=payload.source_language,
    )
    if category == "other" and translated_fields:
        category = infer_item_category(translated_fields.get("title", ""), translated_fields.get("description", ""))
    record.title = payload.title
    record.description = payload.description
    record.title_en = translated_fields["title"] if translated_fields else None
    record.description_en = translated_fields["description"] if translated_fields else None
    record.report_location_en = translated_fields["report_location"] if translated_fields else None
    record.category_en = category if category != "other" and payload.category.strip().casefold() == "other" else translated_fields["category"] if translated_fields else None
    record.source_language = payload.source_language
    record.category = category
    record.lat = payload.lat
    record.lng = payload.lng
    record.report_date = payload.report_date
    record.report_location = payload.report_location
    record.image_urls = payload.image_urls or ([payload.image_url] if payload.image_url else record.image_urls)
    record.image_url = payload.image_url or (record.image_urls[0] if record.image_urls else None)
    record.edit_count += 1
    # Save the user edit immediately. Matching lazily rebuilds missing embeddings.
    record.embedding = None
    record.image_embedding = None
    session.commit()
    session.refresh(record)
    return record


@router.post("/items/{item_type}/{item_id}/reports", status_code=status.HTTP_201_CREATED)
def submit_content_report(
    item_type: str,
    item_id: str,
    payload: ContentReportRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    normalized_type = item_type.strip().lower()
    if normalized_type not in {"lost", "found"}:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Report type must be lost or found")

    model = LostItem if normalized_type == "lost" else FoundItem
    target = session.get(model, item_id)
    if target is None or target.hidden_from_public:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Report not found")
    if target.created_by == uid:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "You cannot report your own content")
    existing = session.scalar(
        select(ContentReport).where(
            ContentReport.reporter_uid == uid,
            ContentReport.report_type == normalized_type,
            ContentReport.report_id == item_id,
        )
    )
    if existing is not None:
        raise HTTPException(status.HTTP_409_CONFLICT, "You have already reported this content")

    report = ContentReport(
        reporter_uid=uid,
        report_type=normalized_type,
        report_id=item_id,
        reason=payload.reason,
        details=payload.details.strip() if payload.details else None,
    )
    session.add(report)
    try:
        session.commit()
    except IntegrityError as exc:
        session.rollback()
        raise HTTPException(status.HTTP_409_CONFLICT, "You have already reported this content") from exc
    return {"status": "received"}


@router.delete("/items/{item_type}/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_item(
    item_type: str,
    item_id: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> None:
    normalized_type = item_type.strip().lower()
    if normalized_type not in {"lost", "found"}:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Report type must be lost or found")

    model = LostItem if normalized_type == "lost" else FoundItem
    report = session.scalar(select(model).where(model.id == item_id))
    if report is None or report.created_by != uid:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Report not found")

    try:
        session.execute(
            delete(ContentReport).where(
                ContentReport.report_type == normalized_type,
                ContentReport.report_id == item_id,
            )
        )
        session.execute(
            delete(AdminMatchAlert).where(
                or_(
                    AdminMatchAlert.lost_item_id == item_id,
                    AdminMatchAlert.found_item_id == item_id,
                )
            )
        )
        session.execute(delete(SocialPublication).where(SocialPublication.report_id == item_id))
        if normalized_type == "found":
            session.execute(delete(UserNotification).where(UserNotification.found_item_id == item_id))
        session.delete(report)
        session.commit()
    except Exception as exc:
        session.rollback()
        logger.exception("Could not delete %s report %s", normalized_type, item_id)
        raise HTTPException(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            "Report could not be deleted; please try again",
        ) from exc


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
    uid: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    session = _require_db_session(session)
    from routers.admin import _parse_admin_uids

    is_admin = uid in _parse_admin_uids(os.getenv("ADMIN_FIREBASE_UIDS"))

    if request.imageUrl:
        target_type = (request.targetType or "").strip().lower()
        if target_type not in {"lost", "found"}:
            raise HTTPException(400, "targetType must be lost or found for image matching")
        if not is_admin:
            owned_image = False
            for model in (LostItem, FoundItem):
                owned_reports = session.scalars(
                    select(model).where(model.created_by == uid)
                ).all()
                if any(
                    item.image_url == request.imageUrl
                    or (
                        isinstance(item.image_urls, list)
                        and request.imageUrl in item.image_urls
                    )
                    for item in owned_reports
                ):
                    owned_image = True
                    break
            if not owned_image:
                raise HTTPException(404, "Report not found")
        results = await _find_cloudinary_image_matches(request.imageUrl, target_type, session)
        return [
            {
                **result,
                "item": result["item"] if is_admin else MatchPreview(
                    title=result["item"].title,
                    category=result["item"].category,
                    report_date=result["item"].report_date,
                    image_url=result["item"].image_url,
                ),
            }
            for result in results
        ]

    # 2. Standard Item-ID Matching Fallback
    if bool(request.found_item_id) == bool(request.lost_item_id):
        raise HTTPException(400, "Provide exactly one of found_item_id or lost_item_id")

    matching_found_item = request.found_item_id is not None
    query_model = FoundItem if matching_found_item else LostItem
    candidate_model = LostItem if matching_found_item else FoundItem
    query_item_id = request.found_item_id or request.lost_item_id
    query_item = session.scalar(
        select(query_model).where(query_model.id == query_item_id)
    )
    if query_item is None:
        raise HTTPException(404, "Report not found")
    if not is_admin and query_item.created_by != uid:
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
    return [
        {
            **result,
            "item": result["item"] if is_admin else MatchPreview(
                title=result["item"].title,
                category=result["item"].category,
                report_date=result["item"].report_date,
                image_url=result["item"].image_url,
            ),
        }
        for result in ranked_results
    ]