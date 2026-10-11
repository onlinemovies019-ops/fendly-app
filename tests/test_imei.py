import pytest
from fastapi import HTTPException, Response
from pydantic import ValidationError

from models import VaultItem
from routers.imei import verify_imei
from routers.items import ItemSubmission
from routers.vault import _serialize
from schemas import ItemCreate


def test_retired_imei_lookup_returns_gone_without_caching():
    response = Response()

    with pytest.raises(HTTPException) as exc_info:
        verify_imei("490154203237518", response)

    assert exc_info.value.status_code == 410
    assert response.headers["Cache-Control"] == "no-store"


@pytest.mark.parametrize(
    "model,payload",
    [
        (ItemCreate, {"title": "Phone", "description": "Lost phone", "imei_number": "490154203237518"}),
        (
            ItemSubmission,
            {
                "title": "Phone",
                "description": "Lost phone",
                "type": "lost",
                "imei_number": "490154203237518",
            },
        ),
    ],
)
def test_lost_report_schemas_reject_imei(model, payload):
    with pytest.raises(ValidationError, match="IMEI submission has been disabled"):
        model.model_validate(payload)


def test_vault_metadata_exposes_only_serial_number():
    record = VaultItem(
        id="vault-item-1",
        firebase_uid="user-1",
        object_key="encrypted-object.enc",
        mime_type="image/jpeg",
        serial_number="SN-12345",
    )

    serialized = _serialize(record)

    assert serialized["serial_number"] == "SN-12345"
    assert "serial_imei" not in serialized
    assert "imei" not in serialized
    assert "serial_imei" not in VaultItem.__table__.columns
