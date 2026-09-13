import os
from collections.abc import Generator

from fastapi import HTTPException
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker


def _database_url() -> str | None:
    value = os.getenv("DATABASE_URL")
    if not value:
        return None
    if value.startswith("postgres://"):
        return value.replace("postgres://", "postgresql+psycopg2://", 1)
    if value.startswith("postgresql://"):
        return value.replace("postgresql://", "postgresql+psycopg2://", 1)
    if value.startswith("postgresql+psycopg://"):
        return value.replace("postgresql+psycopg://", "postgresql+psycopg2://", 1)
    return value


database_url = _database_url()
engine = create_engine(database_url, pool_pre_ping=True) if database_url else None
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False) if engine else None


def get_db() -> Generator[Session, None, None]:
    if SessionLocal is None:
        raise HTTPException(503, "Database is not configured")
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()