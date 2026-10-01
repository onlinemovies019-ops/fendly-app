import os
import re
from typing import Any, Optional

from fastapi import APIRouter, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field


def validate_luhn(imei: str) -> bool:
    """Validate a 15-digit IMEI using the Luhn checksum algorithm."""
    clean = re.sub(r"\D", "", str(imei or ""))
    if len(clean) != 15 or not clean.isdigit():
        return False

    total = 0
    parity = len(clean) % 2
    for index, digit_char in enumerate(clean):
        digit = int(digit_char)
        if index % 2 == parity:
            digit *= 2
            if digit > 9:
                digit -= 9
        total += digit
    return total % 10 == 0


def clean_imei(raw: Optional[str]) -> Optional[str]:
    if raw is None:
        return None
    cleaned = re.sub(r"\D", "", str(raw))
    return cleaned if cleaned else None


def mask_imei(imei: Optional[str]) -> Optional[str]:
    if imei and "*" in imei:
        return imei
    value = clean_imei(imei)
    if not value:
        return None
    if len(value) <= 9:
        return value
    return f"{value[:6]}{'*' * (len(value) - 9)}{value[-3:]}"


class ItemCreateRequest(BaseModel):
    title: str = Field(..., min_length=1, max_length=160)
    description: str = Field(default="", max_length=5000)
    imageUrl: Optional[str] = Field(default=None, max_length=1000)
    type: str = Field(..., min_length=1, max_length=10)
    imei: Optional[str] = Field(default=None, max_length=32)
    lat: float = 0.0
    lng: float = 0.0
    report_date: Optional[str] = Field(default=None, max_length=32)
    report_location: Optional[str] = Field(default=None, max_length=500)
    category: str = Field(default="other", min_length=1, max_length=80)
    payment_id: Optional[str] = Field(default=None, max_length=128)


class ItemMatchRequest(BaseModel):
    imageUrl: Optional[str] = Field(default=None, max_length=1000)
    imei: Optional[str] = Field(default=None, max_length=32)
    targetType: Optional[str] = Field(default=None, min_length=1, max_length=10)
    found_item_id: Optional[str] = None
    lost_item_id: Optional[str] = None
    radius_degrees: float = Field(default=0.25, gt=0, le=10)


app = FastAPI(title="Fendly API")
router = APIRouter(prefix="/api")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["Authorization", "Content-Type", "Accept", "Origin"],
)


def _supabase_client() -> Any | None:
    try:
        from supabase import create_client
    except Exception:
        return None

    url = os.getenv("SUPABASE_URL")
    key = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_ANON_KEY")
    if not url or not key:
        return None
    return create_client(url, key)


def _exact_imei_matches(clean_imei: str, target_type: str) -> list[dict[str, Any]]:
    client = _supabase_client()
    if client is None:
        return []

    try:
        response = client.table("items").select("*").eq("imei", clean_imei).eq("type", target_type).execute()
        rows = response.data or []
    except Exception:
        return []

    matches: list[dict[str, Any]] = []
    for row in rows:
        matches.append(
            {
                "id": row.get("id"),
                "title": row.get("title"),
                "description": row.get("description"),
                "image_url": row.get("image_url") or row.get("imageUrl"),
                "report_location": row.get("report_location") or row.get("location"),
                "lat": row.get("lat", 0.0),
                "lng": row.get("lng", 0.0),
                "type": row.get("type"),
                "imei": mask_imei(row.get("imei")),
                "score": 1.0,
                "matchType": "EXACT_IMEI",
            }
        )
    return matches


def _item_by_id(item_id: str, item_type: str) -> dict[str, Any]:
    client = _supabase_client()
    if client is None:
        raise HTTPException(status_code=503, detail="Item lookup is unavailable")

    try:
        response = client.table("items").select("*").eq("id", item_id).eq("type", item_type).execute()
        rows = response.data or []
    except Exception as error:
        raise HTTPException(status_code=503, detail="Item lookup is unavailable") from error

    if not rows:
        raise HTTPException(status_code=404, detail="Report not found")
    return rows[0]


def _visual_matches(image_url: str, target_type: str) -> list[dict[str, Any]]:
    try:
        from ai_matching import create_embedding, item_text
        from image_matching import cosine_similarity
    except Exception:
        return []

    client = _supabase_client()
    if client is None:
        return []

    try:
        query_embedding = create_embedding(item_text("visual match", image_url, "other"))
        response = client.table("items").select("*").eq("type", target_type).execute()
        rows = response.data or []
    except Exception:
        return []

    matches: list[dict[str, Any]] = []
    for row in rows:
        candidate_embedding = row.get("embedding")
        if not candidate_embedding:
            continue
        try:
            score = cosine_similarity(query_embedding, candidate_embedding)
        except Exception:
            continue
        if not isinstance(score, (int, float)):
            continue
        matches.append(
            {
                "id": row.get("id"),
                "title": row.get("title"),
                "description": row.get("description"),
                "image_url": row.get("image_url") or row.get("imageUrl"),
                "report_location": row.get("report_location") or row.get("location"),
                "lat": row.get("lat", 0.0),
                "lng": row.get("lng", 0.0),
                "type": row.get("type"),
                "imei": mask_imei(row.get("imei")),
                "score": float(score),
                "matchType": "VISUAL",
            }
        )

    matches.sort(key=lambda item: float(item["score"]), reverse=True)
    return matches


@router.post("/items", status_code=201)
async def create_item(request: ItemCreateRequest):
    if request.imei is not None and not validate_luhn(request.imei):
        raise HTTPException(status_code=400, detail="Invalid 15-digit IMEI number")

    payment_id = request.payment_id or ""
    if payment_id == "test_payment_123" or payment_id.startswith("test_"):
        pass

    cleaned_imei = clean_imei(request.imei)
    item_type = (request.type or "").strip().lower()
    if item_type not in {"lost", "found"}:
        raise HTTPException(status_code=400, detail="type must be 'lost' or 'found'")

    payload: dict[str, Any] = {
        "title": request.title,
        "description": request.description,
        "image_url": request.imageUrl,
        "imageUrl": request.imageUrl,
        "type": item_type,
        "lat": request.lat,
        "lng": request.lng,
        "report_date": request.report_date,
        "report_location": request.report_location,
        "category": request.category,
        "payment_id": request.payment_id,
        "imei": cleaned_imei,
    }

    if request.imageUrl is None:
        payload.pop("image_url", None)
        payload.pop("imageUrl", None)

    client = _supabase_client()
    if client is not None:
        try:
            response = client.table("items").insert(payload).execute()
            row = (response.data or [{}])[0]
            return {"ok": True, "item": row, "imei": mask_imei(cleaned_imei)}
        except Exception:
            pass

    return {"ok": True, "item": payload, "imei": mask_imei(cleaned_imei)}


@router.post("/items/match")
async def match_items(request: ItemMatchRequest):
    if request.found_item_id and request.lost_item_id:
        raise HTTPException(status_code=400, detail="Provide exactly one of found_item_id or lost_item_id")

    source_item = None
    if request.found_item_id:
        source_item = _item_by_id(request.found_item_id, "found")
        target_type = "lost"
    elif request.lost_item_id:
        source_item = _item_by_id(request.lost_item_id, "lost")
        target_type = "found"
    else:
        target_type = (request.targetType or "").strip().lower()
        if target_type not in {"lost", "found"}:
            if request.imei is None:
                raise HTTPException(status_code=400, detail="targetType must be 'lost' or 'found'")

    cleaned_imei = clean_imei(request.imei)
    if request.imei is not None and (cleaned_imei is None or len(cleaned_imei) != 15 or not cleaned_imei.isdigit()):
        raise HTTPException(status_code=400, detail="Invalid 15-digit IMEI number")
    if cleaned_imei is None and source_item is not None:
        cleaned_imei = clean_imei(source_item.get("imei"))

    image_url = request.imageUrl
    if not image_url and source_item is not None:
        image_url = source_item.get("image_url") or source_item.get("imageUrl")

    results: list[dict[str, Any]] = []

    if cleaned_imei and len(cleaned_imei) == 15 and cleaned_imei.isdigit() and target_type in {"lost", "found"}:
        exact_results = _exact_imei_matches(cleaned_imei, target_type)
        results.extend(exact_results)

    if image_url:
        visual_results = _visual_matches(image_url, target_type)
        results.extend(visual_results)

    deduped: dict[str, dict[str, Any]] = {}
    for item in results:
        item_id = str(item.get("id") or "")
        if not item_id:
            continue
        if item_id not in deduped:
            deduped[item_id] = item

    ranked = sorted(
        deduped.values(),
        key=lambda item: (
            0 if item.get("matchType") == "EXACT_IMEI" else 1,
            -(float(item.get("score") or 0.0)),
        ),
    )

    for item in ranked:
        item["imei"] = mask_imei(item.get("imei"))

    return ranked


app.include_router(router)


@app.get("/health")
async def health_check():
    return {"status": "ok"}
