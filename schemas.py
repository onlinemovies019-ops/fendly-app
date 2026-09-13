from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class ItemCreate(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    description: str = Field(min_length=1, max_length=5000)
    lat: float = Field(ge=-90, le=90)
    lng: float = Field(ge=-180, le=180)
    image_url: str | None = Field(default=None, max_length=1000)
    category: str = Field(default="other", min_length=1, max_length=80)


class ItemResponse(ItemCreate):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_by: str
    created_at: datetime | None = None


class MatchRequest(BaseModel):
    found_item_id: str
    radius_degrees: float = Field(default=0.25, gt=0, le=10)


class MatchResponse(BaseModel):
    item: ItemResponse
    score: float = Field(ge=0, le=1)