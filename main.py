import io
import json
import os
import sys
from pathlib import Path
from uuid import uuid4

import firebase_admin
from fastapi import Depends, FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from fastapi.staticfiles import StaticFiles
from firebase_admin import auth as firebase_auth, credentials
from PIL import Image, UnidentifiedImageError
from pydantic import BaseModel, Field
from sqlalchemy import Float, String, Text, create_engine, func, select
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, sessionmaker

database_url = os.getenv("DATABASE_URL")
if not database_url:
    print("FATAL ERROR: DATABASE_URL environment variable is not set!", file=sys.stderr)
    sys.exit(1)

database_url = database_url.replace("postgres://", "postgresql+psycopg://", 1)
database_url = database_url.replace("postgresql://", "postgresql+psycopg://", 1)
database_url = database_url.replace("postgresql+psycopg2://", "postgresql+psycopg://", 1)
upload_dir = Path(os.getenv("UPLOAD_DIR", "static/uploads"))
upload_dir.mkdir(parents=True, exist_ok=True)
engine = create_engine(database_url, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    firebase_uid: Mapped[str] = mapped_column(String(128), unique=True, index=True)


class LostItem(Base):
    __tablename__ = "lost_items"
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    created_by: Mapped[str] = mapped_column(String(128), index=True)
    title: Mapped[str] = mapped_column(String(160))
    description: Mapped[str] = mapped_column(Text)
    category: Mapped[str] = mapped_column(String(80), default="other")
    lat: Mapped[float] = mapped_column(Float)
    lng: Mapped[float] = mapped_column(Float)
    image_url: Mapped[str | None] = mapped_column(String(1000))


class FoundItem(Base):
    __tablename__ = "found_items"
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    created_by: Mapped[str] = mapped_column(String(128), index=True)
    title: Mapped[str] = mapped_column(String(160))
    description: Mapped[str] = mapped_column(Text)
    category: Mapped[str] = mapped_column(String(80), default="other")
    lat: Mapped[float] = mapped_column(Float)
    lng: Mapped[float] = mapped_column(Float)
    image_url: Mapped[str | None] = mapped_column(String(1000))


class Item(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    lat: float = Field(ge=-90, le=90)
    lng: float = Field(ge=-180, le=180)
    image_url: str | None = None
    category: str = "other"


class MatchRequest(BaseModel):
    found_item_id: str
    radius_degrees: float = Field(default=0.25, gt=0, le=10)


app = FastAPI(title="Fendly API")
app.add_middleware(CORSMiddleware, allow_origins=os.getenv("CORS_ORIGINS", "*").split(","), allow_methods=["*"], allow_headers=["*"])
app.mount("/static", StaticFiles(directory="static"), name="static")
bearer = HTTPBearer(auto_error=False)


def db():
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()


def firebase_uid(header: HTTPAuthorizationCredentials | None = Depends(bearer)) -> str:
    if not header or header.scheme.lower() != "bearer":
        raise HTTPException(401, "Bearer token required")
    try:
        if not firebase_admin._apps:
            raw = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
            if raw:
                firebase_admin.initialize_app(credentials.Certificate(json.loads(raw)))
            else:
                firebase_admin.initialize_app(credentials.ApplicationDefault())
        return firebase_auth.verify_id_token(header.credentials)["uid"]
    except Exception as exc:
        raise HTTPException(401, "Invalid Firebase token") from exc


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/upload")
async def upload(image: UploadFile = File(...), uid: str = Depends(firebase_uid)):
    del uid
    if image.content_type not in {"image/jpeg", "image/png", "image/webp"}:
        raise HTTPException(415, "Unsupported image type")
    data = await image.read(10 * 1024 * 1024 + 1)
    try:
        with Image.open(io.BytesIO(data)) as checked:
            checked.verify()
            extension = "jpg" if checked.format == "JPEG" else checked.format.lower()
    except (UnidentifiedImageError, OSError) as exc:
        raise HTTPException(400, "Invalid image") from exc
    filename = f"{uuid4().hex}.{extension}"
    (upload_dir / filename).write_bytes(data)
    base = os.getenv("PUBLIC_BASE_URL", "http://127.0.0.1:8000").rstrip("/")
    return {"url": f"{base}/static/uploads/{filename}", "filename": filename}


def save_item(payload: Item, session: Session, uid: str, model):
    record = model(**payload.model_dump(), created_by=uid)
    session.add(record)
    session.commit()
    session.refresh(record)
    return record


@app.post("/api/items/lost")
def lost(payload: Item, session: Session = Depends(db), uid: str = Depends(firebase_uid)):
    return save_item(payload, session, uid, LostItem)


@app.post("/api/items/found")
def found(payload: Item, session: Session = Depends(db), uid: str = Depends(firebase_uid)):
    return save_item(payload, session, uid, FoundItem)


@app.post("/api/items/match")
def match(request: MatchRequest, session: Session = Depends(db), uid: str = Depends(firebase_uid)):
    del uid
    found_item = session.get(FoundItem, request.found_item_id)
    if not found_item:
        raise HTTPException(404, "Found item not found")
    candidates = session.scalars(select(LostItem).where(func.abs(LostItem.lat - found_item.lat) <= request.radius_degrees, func.abs(LostItem.lng - found_item.lng) <= request.radius_degrees)).all()
    found_words = set((found_item.title + " " + found_item.description).lower().split())
    result = []
    for item in candidates:
        words = set((item.title + " " + item.description).lower().split())
        text_score = len(found_words & words) / max(len(found_words | words), 1)
        distance = abs(item.lat - found_item.lat) + abs(item.lng - found_item.lng)
        location_score = max(0, 1 - distance / (2 * request.radius_degrees))
        result.append({"item": item, "score": round(text_score * .7 + location_score * .3, 4)})
    return sorted(result, key=lambda value: value["score"], reverse=True)[:25]


@app.on_event("startup")
def initialize_database():
    Base.metadata.create_all(bind=engine)
