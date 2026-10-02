from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class ItemCreate(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    source_language: str = Field(default="auto", max_length=16)
    lat: float = Field(default=0.0, ge=-90, le=90)
    lng: float = Field(default=0.0, ge=-180, le=180)
    report_date: str | None = Field(default=None, max_length=32)
    report_location: str | None = Field(default=None, max_length=500)
    image_url: str | None = Field(default=None, max_length=1000)
    imei: str | None = Field(default=None, max_length=32)
    category: str = Field(default="other", min_length=1, max_length=80)
    payment_id: str | None = Field(default=None, max_length=128)


class ItemUpdate(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    source_language: str = Field(default="auto", max_length=16)
    lat: float = Field(default=0.0, ge=-90, le=90)
    lng: float = Field(default=0.0, ge=-180, le=180)
    report_date: str | None = Field(default=None, max_length=32)
    report_location: str | None = Field(default=None, max_length=500)
    image_url: str | None = Field(default=None, max_length=1000)
    category: str = Field(default="other", min_length=1, max_length=80)


class ItemResponse(ItemCreate):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_by: str
    created_at: datetime | None = None
    edit_count: int = 0


class MatchRequest(BaseModel):
    imei: str | None = Field(default=None, max_length=32)
    targetType: str | None = Field(default=None, max_length=10)
    imageUrl: str | None = Field(default=None, max_length=2000)
    found_item_id: str | None = Field(default=None, min_length=1)
    lost_item_id: str | None = Field(default=None, min_length=1)
    radius_degrees: float = Field(default=0.25, gt=0, le=10)


class MatchResponse(BaseModel):
    item: ItemResponse | None = None
    score: float = Field(ge=0, le=1)
    matchType: str | None = None
    imei: str | None = None


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
    email_verified: bool | None = Field(default=None)
    mobile_verified: bool | None = Field(default=None)