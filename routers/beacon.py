from datetime import datetime, timezone
from typing import Any

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, ConfigDict, Field, field_validator
from sqlalchemy.dialects.postgresql import insert as pg_insert
from sqlalchemy.dialects.sqlite import insert as sqlite_insert
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import BeaconEvent

router = APIRouter(prefix="/api/v1/beacon", tags=["beacon"])


class BeaconEventInput(BaseModel):
    model_config = ConfigDict(extra="forbid")

    event_id: str = Field(min_length=1, max_length=128)
    latitude: float = Field(ge=-90, le=90)
    longitude: float = Field(ge=-180, le=180)
    timestamp: datetime
    source: str = Field(default="device", min_length=1, max_length=32)
    relay_metadata: dict[str, Any] | None = None

    @field_validator("timestamp")
    @classmethod
    def require_timezone(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("timestamp must include a timezone")
        return value.astimezone(timezone.utc)

    @field_validator("event_id", "source")
    @classmethod
    def trim_required_text(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("value must not be blank")
        return value

    @field_validator("relay_metadata")
    @classmethod
    def validate_relay_metadata(cls, value: dict[str, Any] | None):
        if value is None:
            return value
        if len(value) > 20 or any(
            not isinstance(key, str)
            or not key
            or len(key) > 64
            or not isinstance(item, (str, int, float, bool, type(None)))
            or isinstance(item, (str, int, float)) and len(str(item)) > 256
            for key, item in value.items()
        ):
            raise ValueError("relay_metadata must contain at most 20 short scalar values")
        return value


class BeaconSyncRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    events: list[BeaconEventInput] = Field(min_length=1, max_length=100)


@router.post("/sync")
def sync_beacon_events(
    payload: BeaconSyncRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, int]:
    deduplicated: dict[str, BeaconEventInput] = {}
    for event in payload.events:
        deduplicated.setdefault(event.event_id, event)
    rows = [
        {
            "firebase_uid": uid,
            "event_id": event.event_id,
            "latitude": event.latitude,
            "longitude": event.longitude,
            "occurred_at": event.timestamp,
            "source": event.source,
            "relay_metadata": event.relay_metadata,
        }
        for event in deduplicated.values()
    ]
    dialect = session.get_bind().dialect.name
    if dialect == "sqlite":
        statement = sqlite_insert(BeaconEvent).values(rows)
    elif dialect == "postgresql":
        statement = pg_insert(BeaconEvent).values(rows)
    else:
        raise HTTPException(status_code=503, detail="Beacon storage is unavailable")
    try:
        result = session.execute(
            statement.on_conflict_do_nothing(
                index_elements=["firebase_uid", "event_id"]
            )
        )
        session.commit()
    except Exception as exc:
        session.rollback()
        raise HTTPException(status_code=503, detail="Beacon sync is temporarily unavailable") from exc
    inserted = max(result.rowcount or 0, 0)
    return {
        "received": len(payload.events),
        "inserted": inserted,
        "duplicates": len(payload.events) - inserted,
    }
