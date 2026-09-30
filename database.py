import logging
import os
from collections.abc import Generator

import sqlalchemy
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

logger = logging.getLogger(__name__)


def _database_url() -> str:
    value = os.getenv("DATABASE_URL")
    if not value or not value.strip():
        return "sqlite:///./fendly.db"
    value = value.strip()
    if value.startswith("postgres://"):
        return value.replace("postgres://", "postgresql+psycopg2://", 1)
    if value.startswith("postgresql://"):
        return value.replace("postgresql://", "postgresql+psycopg2://", 1)
    if value.startswith("postgresql+psycopg://"):
        return value.replace("postgresql+psycopg://", "postgresql+psycopg2://", 1)
    return value


def _create_db_engine():
    url = _database_url()
    try:
        if url.startswith("sqlite"):
            return create_engine(url, connect_args={"check_same_thread": False})
        return create_engine(url, pool_pre_ping=True, pool_recycle=300)
    except Exception as exc:
        logger.warning("Failed to initialize engine for %s: %s. Falling back to SQLite.", url, exc)
        return create_engine("sqlite:///./fendly.db", connect_args={"check_same_thread": False})


engine = _create_db_engine()
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


def get_db() -> Generator[Session, None, None]:
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()
