import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlalchemy import text
from auth import router as auth_router
from database import engine
from models import Base
from routers.items import router as items_router
from routers.notifications import router as notifications_router
from routers.users import router as users_router
from routers.admin import router as admin_router
from routers.payments import router as payments_router


def _validate_production_config() -> None:
    if os.getenv("ENVIRONMENT", "development").lower() != "production":
        return
    required = (
        "DATABASE_URL",
        "FIREBASE_SERVICE_ACCOUNT_JSON",
        "SUPABASE_URL",
        "SUPABASE_SERVICE_ROLE_KEY",
        "RAZORPAY_KEY_ID",
        "RAZORPAY_KEY_SECRET",
        "TWOFACTOR_API_KEY",
    )
    missing = [name for name in required if not os.getenv(name)]
    if missing:
        raise RuntimeError(f"Missing production configuration: {', '.join(missing)}")


@asynccontextmanager
async def lifespan(_: FastAPI):
    if os.getenv("ENVIRONMENT", "production").lower() != "production":
        raise RuntimeError("Local backend runtime is disabled. Use the hosted Render backend only.")
    _validate_production_config()
    if engine is not None:
        with engine.begin() as connection:
            connection.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))
            connection.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS username varchar(32)"))
            connection.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name varchar(160)"))
            connection.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS email varchar(320)"))
            connection.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS mobile varchar(32)"))
            connection.execute(text("ALTER TABLE lost_items ADD COLUMN IF NOT EXISTS image_embedding vector(512)"))
            connection.execute(text("ALTER TABLE found_items ADD COLUMN IF NOT EXISTS image_embedding vector(512)"))
            connection.execute(text("ALTER TABLE lost_items ADD COLUMN IF NOT EXISTS report_date varchar(32)"))
            connection.execute(text("ALTER TABLE lost_items ADD COLUMN IF NOT EXISTS report_location varchar(500)"))
            connection.execute(text("ALTER TABLE lost_items ADD COLUMN IF NOT EXISTS edit_count integer NOT NULL DEFAULT 0"))
            connection.execute(text("ALTER TABLE found_items ADD COLUMN IF NOT EXISTS report_date varchar(32)"))
            connection.execute(text("ALTER TABLE found_items ADD COLUMN IF NOT EXISTS report_location varchar(500)"))
            connection.execute(text("ALTER TABLE found_items ADD COLUMN IF NOT EXISTS edit_count integer NOT NULL DEFAULT 0"))
            Base.metadata.create_all(bind=connection)
    yield


app = FastAPI(title="Fendly API", version="1.0.0", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ORIGINS", "*").split(","),
    allow_methods=["*"],
    allow_headers=["*"],
)
app.mount("/static", StaticFiles(directory="static"), name="static")
app.include_router(auth_router)
app.include_router(items_router)
app.include_router(notifications_router)
app.include_router(users_router)
app.include_router(admin_router)
app.include_router(payments_router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
