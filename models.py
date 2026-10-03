from datetime import datetime
from uuid import uuid4

from pgvector.sqlalchemy import Vector
from sqlalchemy import BigInteger, Boolean, DateTime, Float, Index, JSON, String, Text, UniqueConstraint, func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    firebase_uid: Mapped[str] = mapped_column(String(128), unique=True, index=True)
    username: Mapped[str | None] = mapped_column(String(32), index=True)
    full_name: Mapped[str | None] = mapped_column(String(160), index=True)
    email: Mapped[str | None] = mapped_column(String(320), index=True)
    mobile: Mapped[str | None] = mapped_column(String(32), index=True)
    state: Mapped[str | None] = mapped_column(String(120), index=True)
    city: Mapped[str | None] = mapped_column(String(120), index=True)
    profile_photo_url: Mapped[str | None] = mapped_column(String(1000))
    email_verified: Mapped[bool] = mapped_column(Boolean, default=False)
    mobile_verified: Mapped[bool] = mapped_column(Boolean, default=False)
    annual_subscription_expires_at: Mapped[int | None] = mapped_column(BigInteger)
    annual_subscription_payment_id: Mapped[str | None] = mapped_column(String(128))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class UsernameReservation(Base):
    __tablename__ = "username_reservations"

    username: Mapped[str] = mapped_column(String(32), primary_key=True)
    firebase_uid: Mapped[str] = mapped_column(String(128), unique=True, index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class DeviceToken(Base):
    __tablename__ = "device_tokens"
    __table_args__ = (UniqueConstraint("token", name="uq_device_tokens_token"),)

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    firebase_uid: Mapped[str] = mapped_column(String(128), index=True)
    token: Mapped[str] = mapped_column(String(4096), unique=True)
    platform: Mapped[str] = mapped_column(String(32), default="android")
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())


class EmailOTPChallenge(Base):
    __tablename__ = "email_otp_challenges"

    firebase_uid: Mapped[str] = mapped_column(String(128), primary_key=True)
    email: Mapped[str] = mapped_column(String(320), index=True)
    code_digest: Mapped[str] = mapped_column(String(64))
    sent_at: Mapped[int] = mapped_column(BigInteger)
    expires_at: Mapped[int] = mapped_column(BigInteger, index=True)
    send_window_started: Mapped[int] = mapped_column(BigInteger, default=0)
    send_count: Mapped[int] = mapped_column(default=0)
    attempts: Mapped[int] = mapped_column(default=0)
    sent: Mapped[bool] = mapped_column(Boolean, default=False)


class SmsOTPRateLimit(Base):
    __tablename__ = "sms_otp_rate_limits"

    quota_key: Mapped[str] = mapped_column(String(64), primary_key=True)
    sent_at: Mapped[int] = mapped_column(BigInteger)
    window_started: Mapped[int] = mapped_column(BigInteger)
    send_count: Mapped[int] = mapped_column(default=0)


class SmsOTPChallenge(Base):
    __tablename__ = "sms_otp_challenges"

    session_digest: Mapped[str] = mapped_column(String(64), primary_key=True)
    phone_digest: Mapped[str] = mapped_column(String(64), index=True)
    sent_at: Mapped[int] = mapped_column(BigInteger)
    expires_at: Mapped[int] = mapped_column(BigInteger, index=True)
    attempts: Mapped[int] = mapped_column(default=0)


class PublicImeiLookupRateLimit(Base):
    __tablename__ = "public_imei_lookup_rate_limits"

    quota_key: Mapped[str] = mapped_column(String(64), primary_key=True)
    window_started: Mapped[int] = mapped_column(BigInteger)
    request_count: Mapped[int] = mapped_column(default=0)


class UserNotification(Base):
    __tablename__ = "user_notifications"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    firebase_uid: Mapped[str] = mapped_column(String(128), index=True)
    found_item_id: Mapped[str] = mapped_column(String(36), index=True)
    title: Mapped[str] = mapped_column(String(160))
    body: Mapped[str] = mapped_column(Text)
    score: Mapped[float] = mapped_column(Float)
    is_read: Mapped[bool] = mapped_column(Boolean, default=False, index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class AdminMatchAlert(Base):
    __tablename__ = "admin_match_alerts"
    __table_args__ = (UniqueConstraint("found_item_id", "lost_item_id", name="uq_admin_match_alert_pair"),)

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    found_item_id: Mapped[str] = mapped_column(String(36), index=True)
    lost_item_id: Mapped[str] = mapped_column(String(36), index=True)
    found_title: Mapped[str] = mapped_column(String(160))
    lost_title: Mapped[str] = mapped_column(String(160))
    confidence: Mapped[float] = mapped_column(Float)
    reason: Mapped[str] = mapped_column(Text)
    is_read: Mapped[bool] = mapped_column(Boolean, default=False, index=True)
    review_status: Mapped[str] = mapped_column(String(20), default="pending")
    email_sent: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class LostItem(Base):
    __tablename__ = "lost_items"
    __table_args__ = (
        Index("lost_items_status_imei_hash_idx", "status", "imei_hash"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    created_by: Mapped[str] = mapped_column(String(128), index=True)
    status: Mapped[str] = mapped_column(String(16), default="LOST", index=True)
    imei_hash: Mapped[str | None] = mapped_column(String(64))
    title: Mapped[str] = mapped_column(String(160))
    description: Mapped[str] = mapped_column(Text)
    title_en: Mapped[str | None] = mapped_column(Text)
    description_en: Mapped[str | None] = mapped_column(Text)
    report_location_en: Mapped[str | None] = mapped_column(Text)
    category_en: Mapped[str | None] = mapped_column(Text)
    source_language: Mapped[str] = mapped_column(String(16), default="auto")
    category: Mapped[str] = mapped_column(String(80), default="other", index=True)
    lat: Mapped[float] = mapped_column(Float, index=True)
    lng: Mapped[float] = mapped_column(Float, index=True)
    report_date: Mapped[str | None] = mapped_column(String(32))
    report_location: Mapped[str | None] = mapped_column(String(500))
    image_url: Mapped[str | None] = mapped_column(String(1000))
    image_urls: Mapped[list[str] | None] = mapped_column(JSON)
    edit_count: Mapped[int] = mapped_column(default=0)
    image_embedding: Mapped[list[float] | None] = mapped_column(Vector(512))
    embedding: Mapped[list[float] | None] = mapped_column(Vector(1536))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class FoundItem(Base):
    __tablename__ = "found_items"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    created_by: Mapped[str] = mapped_column(String(128), index=True)
    title: Mapped[str] = mapped_column(String(160))
    description: Mapped[str] = mapped_column(Text)
    title_en: Mapped[str | None] = mapped_column(Text)
    description_en: Mapped[str | None] = mapped_column(Text)
    report_location_en: Mapped[str | None] = mapped_column(Text)
    category_en: Mapped[str | None] = mapped_column(Text)
    source_language: Mapped[str] = mapped_column(String(16), default="auto")
    category: Mapped[str] = mapped_column(String(80), default="other", index=True)
    lat: Mapped[float] = mapped_column(Float, index=True)
    lng: Mapped[float] = mapped_column(Float, index=True)
    report_date: Mapped[str | None] = mapped_column(String(32))
    report_location: Mapped[str | None] = mapped_column(String(500))
    image_url: Mapped[str | None] = mapped_column(String(1000))
    image_urls: Mapped[list[str] | None] = mapped_column(JSON)
    edit_count: Mapped[int] = mapped_column(default=0)
    image_embedding: Mapped[list[float] | None] = mapped_column(Vector(512))
    embedding: Mapped[list[float] | None] = mapped_column(Vector(1536))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())