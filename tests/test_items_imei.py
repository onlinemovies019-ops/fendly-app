from types import SimpleNamespace
from unittest.mock import Mock

import pytest
from sqlalchemy.orm import Session

from routers.items import match_items
from schemas import MatchRequest


@pytest.mark.asyncio
async def test_exact_imei_match_uses_sqlalchemy_session_and_masks_imei():
    record = SimpleNamespace(
        id="lost-item-1",
        created_by="user-1",
        title="Test phone",
        description="Lost phone",
        category="electronics",
        lat=19.076,
        lng=72.8777,
        imei="490154203237518",
    )
    session = Mock(spec=Session)
    session.scalars.return_value.all.return_value = [record]

    results = await match_items(
        MatchRequest(imei="490154203237518", targetType="lost"),
        session=session,
        _="test-user",
    )

    session.scalars.assert_called_once()
    assert results[0]["matchType"] == "EXACT_IMEI"
    assert results[0]["score"] == 1.0
    assert results[0]["item"]["imei"] == "490154******518"