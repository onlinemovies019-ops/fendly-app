import os
import json
import urllib.request
import urllib.parse
from contextlib import asynccontextmanager
import logging
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, EmailStr
from sqlalchemy import select, text
from sqlalchemy.orm import Session
from auth import router as auth_router, get_current_user
from database import engine, get_db
from models import Base, User
from routers.items import create_item_compat, match_items, router as items_router
from routers.notifications import router as notifications_router
from routers.users import router as users_router
from routers.admin import router as admin_router
from routers.payments import router as payments_router
from schemas import ItemResponse, MatchResponse


def _validate_production_config() -> None:
    if os.getenv("ENVIRONMENT", "development").lower() != "production":
        return

    required = (
        "DATABASE_URL",
        "FIREBASE_SERVICE_ACCOUNT_JSON",
        "APP_SECRET_KEY",
    )
    missing = [name for name in required if not os.getenv(name)]
    if missing:
        raise RuntimeError(f"Missing production configuration: {', '.join(missing)}")

    email_provider = bool(os.getenv("BREVO_API_KEY") and os.getenv("SENDER_EMAIL")) or bool(
        os.getenv("SMTP_HOST") and os.getenv("SMTP_USERNAME") and os.getenv("SMTP_PASSWORD") and os.getenv("SMTP_FROM_EMAIL")
    )
    if not email_provider:
        raise RuntimeError(
            "Missing production email configuration: set BREVO_API_KEY + SENDER_EMAIL or SMTP_HOST + SMTP_USERNAME + SMTP_PASSWORD + SMTP_FROM_EMAIL"
        )


@asynccontextmanager
async def lifespan(_: FastAPI):
    environment = os.getenv("ENVIRONMENT", "development").lower()
    if environment == "production":
        _validate_production_config()
    if engine is not None:
        with engine.begin() as connection:
            dialect_name = connection.dialect.name
            Base.metadata.create_all(bind=connection)
            if dialect_name == "postgresql":
                connection.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))

            def table_has_column(table_name: str, column_name: str) -> bool:
                try:
                    if dialect_name == "postgresql":
                        table = connection.execute(
                            text(f"SELECT column_name FROM information_schema.columns WHERE table_name = '{table_name}'")
                        ).fetchall()
                        return any(row[0] == column_name for row in table)
                    columns = connection.execute(text(f"PRAGMA table_info({table_name})")).fetchall()
                    return any(row[1] == column_name for row in columns)
                except Exception:
                    return False

            def table_exists(conn, table_name: str) -> bool:
                if dialect_name == "postgresql":
                    result = conn.execute(text(f"SELECT to_regclass('{table_name}') IS NOT NULL AS exists")).fetchone()
                    return bool(result and result[0])
                result = conn.execute(
                    text(f"SELECT name FROM sqlite_master WHERE type='table' AND name='{table_name}'")
                ).fetchone()
                return bool(result)

            def add_column_if_missing(
                table_name: str,
                column_name: str,
                column_type: str,
                *,
                not_null: bool = False,
                default: str | None = None,
            ):
                if table_has_column(table_name, column_name):
                    return
                if not table_exists(connection, table_name):
                    return
                definition = column_type
                if not_null:
                    definition += " NOT NULL"
                if default is not None:
                    definition += f" DEFAULT {default}"
                if dialect_name == "postgresql":
                    connection.execute(text(f"ALTER TABLE {table_name} ADD COLUMN IF NOT EXISTS {column_name} {definition}"))
                else:
                    if column_type.startswith("vector"):
                        return
                    connection.execute(text(f"ALTER TABLE {table_name} ADD COLUMN {column_name} {definition}"))

            # --- USER TABLE COLUMNS (AUTOMATIC SUPABASE MIGRATION) ---
            add_column_if_missing("users", "firebase_uid", "varchar(255)")
            add_column_if_missing("users", "first_name", "varchar(120)")
            add_column_if_missing("users", "surname", "varchar(120)")
            add_column_if_missing("users", "username", "varchar(32)")
            add_column_if_missing("users", "full_name", "varchar(160)")
            add_column_if_missing("users", "email", "varchar(320)")
            add_column_if_missing("users", "mobile", "varchar(32)")
            add_column_if_missing("users", "state", "varchar(120)")
            add_column_if_missing("users", "city", "varchar(120)")
            add_column_if_missing("users", "profile_photo_url", "varchar(1000)")
            add_column_if_missing("users", "email_verified", "boolean", not_null=True, default="false")
            add_column_if_missing("users", "mobile_verified", "boolean", not_null=True, default="false")

            # --- ITEM EMBEDDINGS & REPORT COLUMNS ---
            add_column_if_missing("lost_items", "image_embedding", "vector(512)")
            add_column_if_missing("found_items", "image_embedding", "vector(512)")
            add_column_if_missing("lost_items", "embedding", "vector(1536)")
            add_column_if_missing("found_items", "embedding", "vector(1536)")
            add_column_if_missing("lost_items", "imei", "varchar(32)")
            add_column_if_missing("found_items", "imei", "varchar(32)")
            add_column_if_missing("lost_items", "report_date", "varchar(32)")
            add_column_if_missing("lost_items", "report_location", "varchar(500)")
            add_column_if_missing("lost_items", "title_en", "text")
            add_column_if_missing("lost_items", "description_en", "text")
            add_column_if_missing("lost_items", "report_location_en", "text")
            add_column_if_missing("lost_items", "category_en", "text")
            add_column_if_missing("lost_items", "source_language", "varchar(16)", not_null=True, default="'auto'")
            add_column_if_missing("lost_items", "edit_count", "integer", not_null=True, default="0")
            add_column_if_missing("found_items", "report_date", "varchar(32)")
            add_column_if_missing("found_items", "report_location", "varchar(500)")
            add_column_if_missing("found_items", "title_en", "text")
            add_column_if_missing("found_items", "description_en", "text")
            add_column_if_missing("found_items", "report_location_en", "text")
            add_column_if_missing("found_items", "category_en", "text")
            add_column_if_missing("found_items", "source_language", "varchar(16)", not_null=True, default="'auto'")
            add_column_if_missing("found_items", "edit_count", "integer", not_null=True, default="0")
    yield


app = FastAPI(title="Fendly API", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ORIGINS", "*").split(","),
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "Accept", "Origin"],
)

app.mount("/static", StaticFiles(directory="static"), name="static")

# --- ROUTER INCLUSIONS ---
app.include_router(auth_router)
app.include_router(items_router)
app.add_api_route(
    "/api/items",
    create_item_compat,
    methods=["POST"],
    response_model=ItemResponse,
    status_code=status.HTTP_201_CREATED,
)
app.add_api_route(
    "/api/items/match",
    match_items,
    methods=["POST"],
    response_model=list[MatchResponse],
)
app.include_router(notifications_router)
app.include_router(users_router)
app.include_router(admin_router)
app.include_router(payments_router)


@app.get("/health")
def health_check():
    return {"status": "ok"}

# Print all registered routes when Uvicorn starts
@app.on_event("startup")
async def show_routes():
    print("=== REGISTERED ROUTES ===")
    for route in app.routes:
        methods = getattr(route, "methods", None)
        method_str = f"[{','.join(methods)}]" if methods else ""
        print(f"ROUTE -> {route.path} {method_str}")
    print("=========================")


@app.get("/user/me")
def get_user_me_root(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
):
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
        session.add(user)
        try:
            session.commit()
            session.refresh(user)
        except Exception:
            session.rollback()
    is_verified = bool(user.email_verified)
    return {
        "is_verified": is_verified,
        "email_verified": user.email_verified,
        "mobile_verified": user.mobile_verified,
        "email": user.email,
        "mobile": user.mobile,
    }
