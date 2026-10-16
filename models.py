from datetime import date, datetime
from uuid import uuid4

from pgvector.sqlalchemy import Vector
from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    Date,
    DateTime,
    Float,
    ForeignKey,
    Index,
    Integer,
    JSON,
    String,
    Text,
    UniqueConstraint,
    func,
)
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
    instagram_url: Mapped[str | None] = mapped_column(String(2048))
    facebook_url: Mapped[str | None] = mapped_column(String(2048))
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

    otp_digest: Mapped[str] = mapped_column("session_digest", String(64), primary_key=True)
    phone_digest: Mapped[str] = mapped_column(String(64), index=True)
    sent_at: Mapped[int] = mapped_column(BigInteger)
    expires_at: Mapped[int] = mapped_column(BigInteger, index=True)
    attempts: Mapped[int] = mapped_column(default=0)


class UserNotification(Base):
    __tablename__ = "user_notifications"
    __table_args__ = (
        UniqueConstraint(
            "match_alert_id",
            "firebase_uid",
            name="user_notifications_match_recipient_uq",
        ),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    firebase_uid: Mapped[str] = mapped_column(String(128), index=True)
    found_item_id: Mapped[str | None] = mapped_column(String(36), index=True)
    lost_item_id: Mapped[str | None] = mapped_column(String(36), index=True)
    adoption_listing_id: Mapped[str | None] = mapped_column(String(36), index=True)
    match_alert_id: Mapped[str | None] = mapped_column(String(36), index=True)
    notification_type: Mapped[str] = mapped_column(String(32), default="match_verified")
    title: Mapped[str] = mapped_column(String(160))
    body: Mapped[str] = mapped_column(Text)
    score: Mapped[float] = mapped_column(Float)
    is_read: Mapped[bool] = mapped_column(Boolean, default=False, index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class VaultItem(Base):
    __tablename__ = "vault_items"
    __table_args__ = (Index("vault_items_owner_created_idx", "firebase_uid", "created_at"),)

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    firebase_uid: Mapped[str] = mapped_column(String(128), index=True)
    object_key: Mapped[str] = mapped_column(String(80), unique=True)
    mime_type: Mapped[str] = mapped_column(String(32))
    store: Mapped[str | None] = mapped_column(String(160))
    purchase_date: Mapped[date | None] = mapped_column(Date)
    item: Mapped[str | None] = mapped_column(String(160))
    serial_number: Mapped[str | None] = mapped_column(String(128))
    warranty_months: Mapped[int | None] = mapped_column(Integer)
    warranty_expiry: Mapped[date | None] = mapped_column(Date)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), index=True
    )


class AdoptionListing(Base):
    __tablename__ = "adoption_listings"
    __table_args__ = (
        CheckConstraint("age_months BETWEEN 0 AND 600", name="adoption_listings_age_months_check"),
        CheckConstraint(
            "status IN ('available', 'pending', 'adopted')",
            name="adoption_listings_status_check",
        ),
        Index("adoption_listings_public_feed_idx", "status", "species", "location_city"),
        Index("adoption_listings_owner_created_idx", "lister_user_id", "created_at"),
    )
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    lister_user_id: Mapped[str] = mapped_column(String(128))
    pet_name: Mapped[str] = mapped_column(String(120))
    species: Mapped[str] = mapped_column(String(60))
    breed: Mapped[str | None] = mapped_column(String(120))
    age_months: Mapped[int] = mapped_column(Integer)
    gender: Mapped[str] = mapped_column(String(32))
    health_status: Mapped[str] = mapped_column(String(500))
    description: Mapped[str] = mapped_column(Text)
    location_city: Mapped[str] = mapped_column(String(120))
    images: Mapped[list[str]] = mapped_column(JSON, default=list)
    poster_language: Mapped[str] = mapped_column(String(16), default="en", server_default="en")
    status: Mapped[str] = mapped_column(String(16), default="available")
    auto_post_social: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    social_post_status: Mapped[str | None] = mapped_column(String(16))
    social_post_error: Mapped[str | None] = mapped_column(String(500))
    social_post_image_url: Mapped[str | None] = mapped_column(String(1000))
    facebook_post_id: Mapped[str | None] = mapped_column(String(160))
    instagram_post_id: Mapped[str | None] = mapped_column(String(160))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )


class AdoptionApplication(Base):
    __tablename__ = "adoption_applications"
    __table_args__ = (
        CheckConstraint(
            "status IN ('submitted', 'approved', 'rejected')",
            name="adoption_applications_status_check",
        ),
        UniqueConstraint("listing_id", "applicant_user_id", name="adoption_applications_applicant_uq"),
        Index("adoption_applications_listing_status_idx", "listing_id", "status"),
        Index("adoption_applications_applicant_user_id_idx", "applicant_user_id"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    listing_id: Mapped[str] = mapped_column(
        String(36),
        ForeignKey("adoption_listings.id", ondelete="CASCADE"),
    )
    applicant_user_id: Mapped[str] = mapped_column(String(128))
    home_type: Mapped[str] = mapped_column(String(120))
    pet_experience: Mapped[str] = mapped_column(Text)
    contact_phone: Mapped[str] = mapped_column(String(32))
    status: Mapped[str] = mapped_column(String(16), default="submitted")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )


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
    verified_by: Mapped[str | None] = mapped_column(String(128))
    verification_notes: Mapped[str | None] = mapped_column(Text)
    reviewed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    email_sent: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class LostItem(Base):
    __tablename__ = "lost_items"

    __table_args__ = (
        UniqueConstraint("created_by", "client_draft_id", name="uq_lost_items_client_draft"),
    )
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    created_by: Mapped[str] = mapped_column(String(128), index=True)
    status: Mapped[str] = mapped_column(
    client_draft_id: Mapped[str | None] = mapped_column(String(36))
    hidden_verification_detail: Mapped[str | None] = mapped_column(Text)
        String(16), default="LOST", server_default="LOST", index=True
    )
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
    social_share_consent: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    allow_success_story_sharing: Mapped[bool] = mapped_column(
        Boolean, default=False, server_default="false", nullable=False
    )
    hidden_from_public: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    social_poster_url: Mapped[str | None] = mapped_column(String(1000))
    edit_count: Mapped[int] = mapped_column(default=0)
    image_embedding: Mapped[list[float] | None] = mapped_column(Vector(512))
    image_embedding_model: Mapped[str | None] = mapped_column(String(64))
    embedding: Mapped[list[float] | None] = mapped_column(Vector(1536))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class FoundItem(Base):
    __tablename__ = "found_items"

    __table_args__ = (
        UniqueConstraint("created_by", "client_draft_id", name="uq_found_items_client_draft"),
    )
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    created_by: Mapped[str] = mapped_column(String(128), index=True)
    status: Mapped[str] = mapped_column(
    client_draft_id: Mapped[str | None] = mapped_column(String(36))
    hidden_verification_detail: Mapped[str | None] = mapped_column(Text)
        String(16), default="FOUND", server_default="FOUND", index=True
    )
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
    social_share_consent: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    allow_success_story_sharing: Mapped[bool] = mapped_column(
        Boolean, default=False, server_default="false", nullable=False
    )
    hidden_from_public: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    social_poster_url: Mapped[str | None] = mapped_column(String(1000))
    edit_count: Mapped[int] = mapped_column(default=0)
    image_embedding: Mapped[list[float] | None] = mapped_column(Vector(512))
    image_embedding_model: Mapped[str | None] = mapped_column(String(64))
    embedding: Mapped[list[float] | None] = mapped_column(Vector(1536))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class SocialAccount(Base):
class ReportClaim(Base):
    __tablename__ = "report_claims"
    __table_args__ = (
        UniqueConstraint(
            "claimant_uid",
            "lost_item_id",
            "found_item_id",
            name="uq_report_claims_claimant_pair",
        ),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    claimant_uid: Mapped[str] = mapped_column(String(128), index=True)
    lost_item_id: Mapped[str] = mapped_column(String(36), index=True)
    found_item_id: Mapped[str] = mapped_column(String(36), index=True)
    verification_detail: Mapped[str] = mapped_column(Text)
    status: Mapped[str] = mapped_column(String(16), default="pending", index=True)
    reviewed_by: Mapped[str | None] = mapped_column(String(128))
    review_notes: Mapped[str | None] = mapped_column(Text)
    reviewed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )


    __tablename__ = "social_accounts"

    provider: Mapped[str] = mapped_column(String(16), primary_key=True)
    account_id: Mapped[str] = mapped_column(String(128))
    account_name: Mapped[str] = mapped_column(String(160))
    access_token_encrypted: Mapped[str] = mapped_column(Text)
    deletion_access_token_encrypted: Mapped[str | None] = mapped_column(Text)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )


class SocialOAuthState(Base):
    __tablename__ = "social_oauth_states"

    state_digest: Mapped[str] = mapped_column(String(64), primary_key=True)
    provider: Mapped[str] = mapped_column(String(16), index=True)
    created_by: Mapped[str] = mapped_column(String(128))
    expires_at: Mapped[int] = mapped_column(BigInteger, index=True)


class SocialPublication(Base):
    __tablename__ = "social_publications"
    __table_args__ = (
        UniqueConstraint("report_id", "provider", name="social_publications_report_provider_uq"),
        Index("social_publications_status_due_idx", "status", "next_attempt_at"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    report_id: Mapped[str] = mapped_column(String(36), index=True)
    report_type: Mapped[str] = mapped_column(String(8))
    provider: Mapped[str] = mapped_column(String(16))
    status: Mapped[str] = mapped_column(String(16), default="pending", index=True)
    attempt_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    next_attempt_at: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False)
    external_post_id: Mapped[str | None] = mapped_column(String(160))
    last_error: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )
    published_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class ContentReport(Base):
    __tablename__ = "content_reports"
    __table_args__ = (
        CheckConstraint("report_type IN ('lost', 'found')", name="content_reports_report_type_check"),
        CheckConstraint(
            "reason IN ('inappropriate', 'spam', 'personal_information', 'fraud', 'other')",
            name="content_reports_reason_check",
        ),
        UniqueConstraint(
            "reporter_uid",
            "report_type",
            "report_id",
            name="content_reports_reporter_target_uq",
        ),
        Index("content_reports_status_created_idx", "status", "created_at"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    reporter_uid: Mapped[str] = mapped_column(String(128), index=True)
    report_type: Mapped[str] = mapped_column(String(8))
    report_id: Mapped[str] = mapped_column(String(36), index=True)
    reason: Mapped[str] = mapped_column(String(32))
    details: Mapped[str | None] = mapped_column(String(1000))
    status: Mapped[str] = mapped_column(String(16), default="pending", nullable=False, index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
