import time
from types import SimpleNamespace

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from models import User
from models import Base
from routers import admin as admin_module
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


def test_admin_subscription_override_can_activate_and_cancel_user(payment_sessions):
    uid = "firebase-admin-override-user"

    with payment_sessions() as session:
        user = User(firebase_uid=uid, username="override-user", full_name="Override User", mobile_verified=True)
        session.add(user)
        session.commit()

        add_result = admin_module.override_user_subscription(
            uid=uid,
            payload=admin_module.SubscriptionOverrideRequest(action="add"),
            session=session,
            _="admin-1",
        )

        assert add_result["active"] is True
        assert add_result["expires_at"] is not None
        assert session.get(User, user.id).annual_subscription_expires_at is not None

        cancel_result = admin_module.override_user_subscription(
            uid=uid,
            payload=admin_module.SubscriptionOverrideRequest(action="cancel"),
            session=session,
            _="admin-1",
        )

        assert cancel_result["active"] is False
        assert cancel_result["expires_at"] is None
        assert session.get(User, user.id).annual_subscription_expires_at is None


@pytest.mark.asyncio
async def test_admin_user_search_for_phone_matches_only_that_mobile(payment_sessions):
    with payment_sessions() as session:
        session.add_all([
            User(firebase_uid="firebase-phone-search-user", username="phone-search", mobile="+91 98765-43210"),
            User(firebase_uid="firebase-phone-search-duplicate", mobile="9876543210"),
            User(firebase_uid="firebase-number-in-name", full_name="9876543210", mobile="1111111111"),
            User(firebase_uid="firebase-number-in-username", username="9876543210", mobile="2222222222"),
        ])
        session.commit()

        results = await admin_module.search_users_and_reports(
            q="9876543210",
            session=session,
            _="admin-1",
        )

    assert len(results) == 1
    assert results[0]["user"]["uid"] == "firebase-phone-search-user"


@pytest.mark.asyncio
async def test_admin_user_search_for_username_matches_only_username(payment_sessions):
    with payment_sessions() as session:
        session.add_all([
            User(firebase_uid="firebase-username-match", username="rohan", full_name="Rohan Bagmare"),
            User(firebase_uid="firebase-name-only-match", username="another-user", full_name="Rohan Bagmare"),
        ])
        session.commit()

        results = await admin_module.search_users_and_reports(
            q="rohan",
            session=session,
            _="admin-1",
        )

    assert [result["user"]["uid"] for result in results] == ["firebase-username-match"]


@pytest.mark.asyncio
async def test_admin_user_search_prefers_exact_username_match_over_partial_matches(payment_sessions):
    with payment_sessions() as session:
        session.add_all([
            User(firebase_uid="firebase-username-partial", username="rohan123", full_name="Rohan Other"),
            User(firebase_uid="firebase-username-exact", username="rohan", full_name="Rohan Bagmare"),
        ])
        session.commit()

        results = await admin_module.search_users_and_reports(
            q="rohan",
            session=session,
            _="admin-1",
        )

    assert [result["user"]["uid"] for result in results] == ["firebase-username-exact"]