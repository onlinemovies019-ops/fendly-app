from datetime import datetime
from urllib.parse import urlsplit

from pydantic import BaseModel, ConfigDict, Field, field_validator


class ItemCreate(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    source_language: str = Field(default="auto", max_length=16)
    lat: float = Field(default=0.0, ge=-90, le=90)
    lng: float = Field(default=0.0, ge=-180, le=180)
    report_date: str | None = Field(default=None, max_length=32)
    report_location: str | None = Field(default=None, max_length=500)
    image_url: str | None = Field(default=None, max_length=1000)
    image_urls: list[str] = Field(default_factory=list, max_length=3)
    category: str = Field(default="other", min_length=1, max_length=80)
    social_share_consent: bool = False
    payment_id: str | None = Field(default=None, max_length=128)
    imei_number: str | None = Field(default=None, exclude=True)


class ItemUpdate(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    source_language: str = Field(default="auto", max_length=16)
    lat: float = Field(default=0.0, ge=-90, le=90)
    lng: float = Field(default=0.0, ge=-180, le=180)
    report_date: str | None = Field(default=None, max_length=32)
    report_location: str | None = Field(default=None, max_length=500)
    image_url: str | None = Field(default=None, max_length=1000)
    image_urls: list[str] = Field(default_factory=list, max_length=3)
    category: str = Field(default="other", min_length=1, max_length=80)


class ItemResponse(ItemCreate):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_by: str
    created_at: datetime | None = None
    edit_count: int = 0


class MatchRequest(BaseModel):
    targetType: str | None = Field(default=None, max_length=10)
    imageUrl: str | None = Field(default=None, max_length=2000)
    found_item_id: str | None = Field(default=None, min_length=1)
    lost_item_id: str | None = Field(default=None, min_length=1)
    radius_degrees: float = Field(default=0.25, gt=0, le=10)


class MatchPreview(BaseModel):
    title: str
    category: str
    report_date: str | None = None
    image_url: str | None = None


class MatchResponse(BaseModel):
    item: ItemResponse | MatchPreview | None = None
    score: float = Field(ge=0, le=1)
    matchType: str | None = None


class FcmTokenRequest(BaseModel):
    token: str = Field(min_length=20, max_length=4096)
    platform: str = Field(default="android", min_length=1, max_length=32)


class UsernameRequest(BaseModel):
    username: str = Field(min_length=3, max_length=32, pattern=r"^[A-Za-z0-9_]+$")


class ProfileUpdate(BaseModel):
    username: str | None = Field(default=None, max_length=32, pattern=r"^[A-Za-z0-9_]+$")
    full_name: str | None = Field(default=None, max_length=160)
    email: str | None = Field(default=None, max_length=320)
    mobile: str | None = Field(default=None, max_length=32)
    state: str | None = Field(default=None, max_length=120)
    city: str | None = Field(default=None, max_length=120)
    profile_photo_url: str | None = Field(default=None, max_length=1000)
    instagram_url: str | None = Field(default=None, max_length=2048)
    facebook_url: str | None = Field(default=None, max_length=2048)

    @field_validator("instagram_url", "facebook_url")
    @classmethod
    def validate_social_url(cls, value: str | None, info):
        if value is None or not value.strip():
            return "" if value == "" else None
        value = value.strip()
        if any(character.isspace() or ord(character) < 32 for character in value):
            raise ValueError("Social links must be public HTTPS URLs")
        try:
            parsed = urlsplit(value)
            hostname = (parsed.hostname or "").lower().rstrip(".")
        except ValueError as exc:
            raise ValueError("Social links must be public HTTPS URLs") from exc
        domains = {
            "instagram_url": ("instagram.com",),
            "facebook_url": ("facebook.com",),
        }
        allowed_domains = domains[info.field_name]
        if (
            parsed.scheme.lower() != "https"
            or not hostname
            or not any(
                hostname == domain or hostname.endswith(f".{domain}")
                for domain in allowed_domains
            )
            or parsed.username is not None
            or parsed.password is not None
            or not parsed.netloc
        ):
            raise ValueError(
                f"{info.field_name} must use HTTPS and a supported social host"
            )
        return value