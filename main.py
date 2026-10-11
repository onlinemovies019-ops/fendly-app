import os
import json
import urllib.request
import urllib.parse
import asyncio
import logging
import time
from pathlib import Path
from contextlib import asynccontextmanager
import logging
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from starlette.exceptions import HTTPException as StarletteHTTPException
from starlette.types import Scope
from pydantic import BaseModel, EmailStr
from sqlalchemy import delete, select, text
from sqlalchemy.engine import Connection
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session
from auth import router as auth_router, get_current_user
from database import engine, get_db
from models import Base, EmailOTPChallenge, PublicImeiLookupRateLimit, SmsOTPChallenge, SmsOTPRateLimit, User
from routers.items import create_item_compat, match_items, router as items_router
from routers.notifications import inbox_router as inbox_notifications_router
from routers.notifications import router as notifications_router
from routers.users import router as users_router
from routers.admin import router as admin_router
from routers.payments import router as payments_router
from routers.imei import router as imei_router
from routers.vault import router as vault_router
from schemas import ItemResponse, MatchResponse

logger = logging.getLogger(__name__)


def _apply_private_workflow_migration(connection: Connection) -> None:
    migration_path = (
        Path(__file__).resolve().parent
        / "supabase/migrations/20261011000000_private_match_workflow.sql"
    )
    migration_sql = migration_path.read_text(encoding="utf-8")
    for statement in migration_sql.split(";"):
        statement = statement.strip()
        if statement and statement.upper() not in {"BEGIN", "COMMIT"}:
            connection.exec_driver_sql(statement)


def _validate_production_config() -> None:
    if os.getenv("ENVIRONMENT", "development").lower() != "production":
        return

    required = (
        "DATABASE_URL",
        "FIREBASE_SERVICE_ACCOUNT_JSON",
        "APP_SECRET_KEY",
        "RESEND_API_KEY",
        "OPENAI_API_KEY",
        "SUPABASE_URL",
        "SUPABASE_SERVICE_ROLE_KEY",
    )
    missing = [name for name in required if not os.getenv(name)]
    if missing:
        raise RuntimeError(f"Missing production configuration: {', '.join(missing)}")
    if len(os.getenv("APP_SECRET_KEY", "")) < 32:
        raise RuntimeError("APP_SECRET_KEY must contain at least 32 characters")


@asynccontextmanager
async def lifespan(_: FastAPI):
    environment = os.getenv("ENVIRONMENT", "development").lower()
    if environment == "production":
        _validate_production_config()
    verify_image_bucket = False
    if engine is not None:
        with engine.begin() as connection:
            dialect_name = connection.dialect.name
            Base.metadata.create_all(bind=connection)
            if dialect_name == "postgresql":
                connection.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))
                connection.execute(
                    text("ALTER TABLE public.email_otp_challenges ENABLE ROW LEVEL SECURITY")
                )
                connection.execute(
                    text("ALTER TABLE public.sms_otp_challenges ENABLE ROW LEVEL SECURITY")
                )
                connection.execute(
                    text("ALTER TABLE public.sms_otp_rate_limits ENABLE ROW LEVEL SECURITY")
                )
                connection.execute(
                    text("ALTER TABLE public.public_imei_lookup_rate_limits ENABLE ROW LEVEL SECURITY")
                )
                for table_name in (
                    "social_accounts",
                    "social_oauth_states",
                    "social_publications",
                    "content_reports",
                ):
                    connection.execute(
                        text(f"ALTER TABLE public.{table_name} ENABLE ROW LEVEL SECURITY")
                    )
                    connection.execute(
                        text(
                            f"REVOKE ALL PRIVILEGES ON TABLE public.{table_name} "
                            "FROM PUBLIC, anon, authenticated"
                        )
                    )
                _apply_private_workflow_migration(connection)
                verify_image_bucket = bool(
                    os.getenv("SUPABASE_URL") and os.getenv("SUPABASE_SERVICE_ROLE_KEY")
                )
            now = int(time.time())
            connection.execute(
                delete(EmailOTPChallenge).where(
                    EmailOTPChallenge.expires_at <= now,
                    EmailOTPChallenge.send_window_started + 86400 <= now,
                )
            )
            connection.execute(
                delete(SmsOTPChallenge).where(SmsOTPChallenge.expires_at <= now)
            )
            connection.execute(
                delete(SmsOTPRateLimit).where(SmsOTPRateLimit.sent_at <= now - 86400)
            )

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
            add_column_if_missing("users", "instagram_url", "varchar(2048)")
            add_column_if_missing("users", "facebook_url", "varchar(2048)")
            add_column_if_missing("users", "email_verified", "boolean", not_null=True, default="false")
            add_column_if_missing("users", "mobile_verified", "boolean", not_null=True, default="false")
            add_column_if_missing("users", "annual_subscription_expires_at", "bigint")
            add_column_if_missing("users", "annual_subscription_payment_id", "varchar(128)")
            add_column_if_missing(
                "social_accounts",
                "deletion_access_token_encrypted",
                "text",
            )

            # --- ITEM EMBEDDINGS & REPORT COLUMNS ---
            add_column_if_missing("lost_items", "image_embedding", "vector(512)")
            add_column_if_missing("found_items", "image_embedding", "vector(512)")
            add_column_if_missing("lost_items", "embedding", "vector(1536)")
            add_column_if_missing("found_items", "embedding", "vector(1536)")
            add_column_if_missing("lost_items", "report_date", "varchar(32)")
            add_column_if_missing("lost_items", "report_location", "varchar(500)")
            add_column_if_missing("lost_items", "image_urls", "json")
            add_column_if_missing("lost_items", "social_poster_url", "varchar(1000)")
            add_column_if_missing("lost_items", "title_en", "text")
            add_column_if_missing("lost_items", "description_en", "text")
            add_column_if_missing("lost_items", "report_location_en", "text")
            add_column_if_missing("lost_items", "category_en", "text")
            add_column_if_missing("lost_items", "source_language", "varchar(16)", not_null=True, default="'auto'")
            add_column_if_missing("lost_items", "edit_count", "integer", not_null=True, default="0")
            add_column_if_missing("lost_items", "status", "varchar(16)", not_null=True, default="'LOST'")
            add_column_if_missing("lost_items", "imei_hash", "varchar(64)")
            add_column_if_missing("lost_items", "hidden_from_public", "boolean", not_null=True, default="false")
            add_column_if_missing("found_items", "report_date", "varchar(32)")
            add_column_if_missing("found_items", "report_location", "varchar(500)")
            add_column_if_missing("found_items", "image_urls", "json")
            add_column_if_missing("found_items", "social_poster_url", "varchar(1000)")
            add_column_if_missing("found_items", "title_en", "text")
            add_column_if_missing("found_items", "description_en", "text")
            add_column_if_missing("found_items", "report_location_en", "text")
            add_column_if_missing("found_items", "category_en", "text")
            add_column_if_missing("found_items", "source_language", "varchar(16)", not_null=True, default="'auto'")
            add_column_if_missing("found_items", "edit_count", "integer", not_null=True, default="0")
            add_column_if_missing("found_items", "hidden_from_public", "boolean", not_null=True, default="false")
            add_column_if_missing("admin_match_alerts", "review_status", "varchar(20)", not_null=True, default="'pending'")
            connection.execute(
                text(
                    "CREATE INDEX IF NOT EXISTS lost_items_status_imei_hash_idx "
                    "ON lost_items (status, imei_hash)"
                )
            )
    if verify_image_bucket:
        from image_storage import verify_private_bucket

        verify_private_bucket()
        verify_private_bucket(os.getenv("VAULT_STORAGE_BUCKET", "private-vault"))
    cleanup_task = asyncio.create_task(_cleanup_expired_otp_challenges())
    try:
        yield
    finally:
        cleanup_task.cancel()
        try:
            await asyncio.gather(cleanup_task)
        except asyncio.CancelledError:
            pass


async def _cleanup_expired_otp_challenges() -> None:
    while True:
        now = int(time.time())
        try:
            with Session(engine) as session:
                session.execute(
                    delete(EmailOTPChallenge).where(
                        EmailOTPChallenge.expires_at <= now,
                        EmailOTPChallenge.send_window_started + 86400 <= now,
                    )
                )
                session.execute(
                    delete(SmsOTPChallenge).where(SmsOTPChallenge.expires_at <= now)
                )
                session.execute(
                    delete(SmsOTPRateLimit).where(
                        SmsOTPRateLimit.sent_at <= now - 86400
                    )
                )
                session.execute(
                    delete(PublicImeiLookupRateLimit).where(
                        PublicImeiLookupRateLimit.window_started <= now - 86400
                    )
                )
                session.commit()
        except SQLAlchemyError:
            logger.exception("Expired verification data cleanup failed")
        await asyncio.sleep(3600)


app = FastAPI(title="Fendly API", lifespan=lifespan)


def _cors_allowed_origins() -> list[str]:
    configured = os.getenv(
        "CORS_ORIGINS",
        "https://fendly-api.onrender.com",
    )
    origins = [
        origin.strip() if origin.strip() == "*" else origin.strip().rstrip("/")
        for origin in configured.split(",")
        if origin.strip()
    ]
    if "*" in origins and os.getenv("ENVIRONMENT", "").lower() == "production":
        logger.warning(
            "Ignoring wildcard CORS origin in production; configure explicit origins"
        )
        origins = [origin for origin in origins if origin != "*"]
    return origins or ["https://fendly-api.onrender.com"]


app.add_middleware(
    CORSMiddleware,
    allow_origins=_cors_allowed_origins(),
    allow_credentials=False,
    allow_methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "Accept", "Origin"],
)

class PublicStaticFiles(StaticFiles):
    async def get_response(self, path: str, scope: Scope):
        if path == "uploads" or path.startswith("uploads/"):
            raise StarletteHTTPException(status_code=404)
        return await super().get_response(path, scope)


app.mount("/static", PublicStaticFiles(directory="static"), name="static")

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
app.include_router(inbox_notifications_router)
app.include_router(users_router)
app.include_router(admin_router)
app.include_router(payments_router)
app.include_router(imei_router)
app.include_router(vault_router)


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
