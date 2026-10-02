import time
from types import SimpleNamespace

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from models import Base
from routers import payments
from routers.payments import PaymentVerificationRequest


@pytest.fixture
def payment_sessions():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    session_factory = sessionmaker(bind=engine, autoflush=False, autocommit=False)
    yield session_factory
    engine.dispose()


def test_verified_subscription_is_available_to_another_session(payment_sessions, monkeypatch):
    uid = "firebase-user-shared"
    payment_id = "pay_captured_123"
    gateway = SimpleNamespace(
        utility=SimpleNamespace(verify_payment_signature=lambda payload: None),
        payment=SimpleNamespace(fetch=lambda payment_id: {
            "order_id": "order_123",
            "amount": payments.PAYMENT_AMOUNT_PAISE,
            "currency": payments.PAYMENT_CURRENCY,
            "status": "captured",
        }),
        order=SimpleNamespace(fetch=lambda order_id: {
            "notes": {"firebase_uid": uid, "purpose": "lost_report"},
        }),
    )
    monkeypatch.setattr(payments, "_client", lambda: gateway)

    with payment_sessions() as first_session:
        verified = payments.verify_payment(
            PaymentVerificationRequest(
                razorpay_order_id="order_123",
                razorpay_payment_id=payment_id,
                razorpay_signature="signature",
            ),
            session=first_session,
            uid=uid,
        )

    with payment_sessions() as second_session:
        subscription = payments.get_subscription(session=second_session, uid=uid)

    assert verified["verified"] is True
    assert subscription["active"] is True
    assert subscription["payment_id"] == payment_id
    assert subscription["expires_at"] > int(time.time() * 1000)


def test_legacy_captured_payment_restores_subscription(payment_sessions, monkeypatch):
    uid = "firebase-user-legacy"
    payment_id = "pay_legacy_123"
    gateway = SimpleNamespace(
        order=SimpleNamespace(
            all=lambda query: {"items": [{
                "id": "order_legacy",
                "notes": {"firebase_uid": uid, "purpose": "lost_report"},
            }]},
            payments=lambda order_id: {"items": [{
                "id": payment_id,
                "created_at": int(time.time()),
                "amount": payments.PAYMENT_AMOUNT_PAISE,
                "currency": payments.PAYMENT_CURRENCY,
                "status": "captured",
            }]},
        ),
    )
    monkeypatch.setattr(payments, "_client", lambda: gateway)

    with payment_sessions() as session:
        subscription = payments.get_subscription(session=session, uid=uid)

    assert subscription["active"] is True
    assert subscription["payment_id"] == payment_id
    assert subscription["expires_at"] > int(time.time() * 1000)