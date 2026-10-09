import logging
import hashlib
import os
import re
import time
from typing import Any
from pathlib import Path
from urllib.parse import quote, unquote, urlsplit
from fastapi import APIRouter, Depends, HTTPException, Response, status
from sqlalchemy import delete, desc, func, or_, select, update
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from sqlalchemy.orm import Session
import firebase_admin
from firebase_admin import auth as firebase_auth
from firebase_admin import firestore
import httpx

from auth import _firebase_app, get_current_user
from database import get_db
from models import (
    DeviceToken,
    AdminMatchAlert,
    ContentReport,
    EmailOTPChallenge,
    FoundItem,
    LostItem,
    SocialPublication,
    User,
    UserBlock,
    UserNotification,
    UsernameReservation,
)
from profile_service import update_profile_record
from schemas import ProfileUpdate, UsernameRequest

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/users", tags=["users"])


@router.get("/blocked-users")
def list_blocked_users(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[str]:
    return session.scalars(
        select(UserBlock.blocked_uid)
        .where(UserBlock.blocker_uid == uid)
        .order_by(UserBlock.created_at.desc())
    ).all()


@router.post("/blocked-users/{item_type}/{item_id}", status_code=status.HTTP_201_CREATED)
def block_report_author(
    item_type: str,
    item_id: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    normalized_type = item_type.strip().lower()
    model = LostItem if normalized_type == "lost" else FoundItem if normalized_type == "found" else None
    if model is None:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Report type must be lost or found")
    item = session.get(model, item_id)
    if item is None or item.hidden_from_public:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Report not found")
    if item.created_by == uid:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "You cannot block yourself")

    existing = session.get(UserBlock, (uid, item.created_by))
    if existing is None:
        session.add(UserBlock(blocker_uid=uid, blocked_uid=item.created_by))
        try:
            session.commit()
        except IntegrityError:
            session.rollback()
            if session.get(UserBlock, (uid, item.created_by)) is None:
                raise HTTPException(
                    status.HTTP_503_SERVICE_UNAVAILABLE,
                    "Could not block this account; please try again",
                )
    return {"status": "blocked"}


@router.delete("/blocked-users/{blocked_uid}")
def unblock_user(
    blocked_uid: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    if blocked_uid == uid:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "You cannot unblock yourself")
    session.execute(
        delete(UserBlock).where(
            UserBlock.blocker_uid == uid,
            UserBlock.blocked_uid == blocked_uid,
        )
    )
    session.commit()
    return {"status": "unblocked"}


def _photo_is_still_referenced(session: Session, photo_url: str, uid: str) -> bool:
    other_profile = session.scalar(
        select(User.firebase_uid)
        .where(User.firebase_uid != uid, User.profile_photo_url == photo_url)
        .limit(1)
    )
    if other_profile:
        return True

    for report in session.execute(
        select(
            LostItem.created_by,
            LostItem.image_url,
            LostItem.image_urls,
            LostItem.social_poster_url,
        )
    ).all():
        if report[0] == uid:
            continue
        if photo_url == report[1] or (
            isinstance(report[2], list) and photo_url in report[2]
        ) or photo_url == report[3]:
            return True
    for report in session.execute(
        select(
            FoundItem.created_by,
            FoundItem.image_url,
            FoundItem.image_urls,
            FoundItem.social_poster_url,
        )
    ).all():
        if report[0] == uid:
            continue
        if photo_url == report[1] or (
            isinstance(report[2], list) and photo_url in report[2]
        ) or photo_url == report[3]:
            return True
    return False


def _delete_profile_photo_asset(photo_url: str) -> None:
    parsed = urlsplit(photo_url)
    filename_pattern = re.compile(r"[0-9a-f]{32}\.(?:jpg|png|webp)")

    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    bucket = os.getenv("SUPABASE_STORAGE_BUCKET", "uploads")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    if supabase_url:
        storage_base = urlsplit(supabase_url)
        segments = [unquote(part) for part in parsed.path.split("/") if part]
        if (
            parsed.scheme == storage_base.scheme
            and parsed.netloc == storage_base.netloc
            and len(segments) == 6
            and segments[:4] == ["storage", "v1", "object", "public"]
            and segments[4] == bucket
            and filename_pattern.fullmatch(segments[5])
        ):
            if not service_role_key:
                raise RuntimeError("Supabase storage credentials are required to delete the profile image")
            endpoint = f"{supabase_url}/storage/v1/object/{quote(bucket, safe='')}/{quote(segments[5], safe='')}"
            response = httpx.delete(
                endpoint,
                headers={
                    "Authorization": f"Bearer {service_role_key}",
                    "apikey": service_role_key,
                },
                timeout=15.0,
            )
            response.raise_for_status()
            return

    public_base_url = os.getenv(
        "PUBLIC_BASE_URL",
        "https://fendly-api.onrender.com",
    ).rstrip("/")
    if public_base_url:
        public_base = urlsplit(public_base_url)
        segments = [unquote(part) for part in parsed.path.split("/") if part]
        if (
            parsed.scheme == public_base.scheme
            and parsed.netloc == public_base.netloc
            and len(segments) == 3
            and segments[:2] == ["static", "uploads"]
            and filename_pattern.fullmatch(segments[2])
        ):
            upload_root = Path(os.getenv("UPLOAD_DIR", "static/uploads")).resolve()
            asset_path = (upload_root / segments[2]).resolve()
            if asset_path.parent != upload_root:
                raise ValueError("Profile image path is outside the upload directory")
            asset_path.unlink(missing_ok=True)

    cloudinary_cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME", "")
    if parsed.hostname == "res.cloudinary.com":
        segments = [unquote(part) for part in parsed.path.split("/") if part]
        if len(segments) < 5 or segments[1:3] != ["image", "upload"]:
            raise RuntimeError("Cloudinary profile image URL could not be safely parsed")
        url_cloud_name = segments[0]
        if not cloudinary_cloud_name:
            raise RuntimeError("CLOUDINARY_CLOUD_NAME is required to delete this profile image")
        if url_cloud_name != cloudinary_cloud_name:
            return

        api_key = os.getenv("CLOUDINARY_API_KEY", "")
        api_secret = os.getenv("CLOUDINARY_API_SECRET", "")
        if not api_key or not api_secret:
            raise RuntimeError("Cloudinary API credentials are required to delete this profile image")

        after_upload = segments[3:]
        version_index = next(
            (index for index, segment in enumerate(after_upload) if re.fullmatch(r"v\d+", segment)),
            None,
        )
        if version_index is None or version_index == len(after_upload) - 1:
            raise RuntimeError("Cloudinary profile image URL has no recognized versioned public ID")
        public_id = "/".join(after_upload[version_index + 1 :])
        public_id = re.sub(r"\.[A-Za-z0-9]+$", "", public_id)
        if not re.fullmatch(r"[A-Za-z0-9_.\-/]+", public_id) or ".." in public_id.split("/"):
            raise RuntimeError("Cloudinary profile image public ID is invalid")

        timestamp = int(time.time())
        signature_input = f"public_id={public_id}&timestamp={timestamp}{api_secret}"
        signature = hashlib.sha1(signature_input.encode("utf-8")).hexdigest()
        response = httpx.post(
            f"https://api.cloudinary.com/v1_1/{quote(cloudinary_cloud_name, safe='')}/image/destroy",
            data={
                "public_id": public_id,
                "timestamp": timestamp,
                "api_key": api_key,
                "signature": signature,
            },
            timeout=15.0,
        )
        response.raise_for_status()
        result = response.json().get("result")
        if result not in {"ok", "not found"}:
            raise RuntimeError("Cloudinary did not confirm deletion of the profile image")


def _delete_firestore_account_copies(app, uid: str, email: str | None) -> None:
    db = firestore.client(app=app)
    user_docs = {uid}
    for document in db.collection("users").where("uid", "==", uid).stream():
        user_docs.add(document.id)
    for document_id in user_docs:
        db.collection("users").document(document_id).delete()

    if email:
        email_ref = db.collection("verified_emails").document(email.lower())
        email_doc = email_ref.get()
        if email_doc.exists and email_doc.get("uid") == uid:
            email_ref.delete()
    for email_doc in db.collection("verified_emails").where("uid", "==", uid).stream():
        email_doc.reference.delete()

    for collection_name in ("found_items", "lost_items"):
        collection = db.collection(collection_name)
        report_ids = set()
        for field in ("user_id", "created_by", "uid", "userId"):
            for document in collection.where(field, "==", uid).stream():
                report_ids.add(document.id)
        for report_id in report_ids:
            collection.document(report_id).delete()


def _require_supabase_cleanup_config() -> tuple[str, str]:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
    if not supabase_url or not service_role_key:
        raise RuntimeError(
            "Supabase cleanup credentials are required to delete report data"
        )
    parsed_url = urlsplit(supabase_url)
    if parsed_url.scheme != "https" or not parsed_url.netloc:
        raise RuntimeError("SUPABASE_URL must be a valid HTTPS URL")
    return supabase_url, service_role_key


def _delete_supabase_account_matching_data(report_ids: set[str]) -> None:
    if not report_ids:
        return

    supabase_url, service_role_key = _require_supabase_cleanup_config()
    encoded_ids = ",".join(f'"{report_id}"' for report_id in sorted(report_ids))
    headers = {
        "apikey": service_role_key,
        "Authorization": f"******",
    }
    for table, params in (
        ("items", {"source_id": f"in.({encoded_ids})"}),
        (
            "admin_match_alerts",
            {
                "or": (
                    f"(found_item_id.in.({encoded_ids}),"
                    f"lost_item_id.in.({encoded_ids}))"
                )
            },
        ),
    ):
        response = httpx.delete(
            f"{supabase_url}/rest/v1/{table}",
            params=params,
            headers=headers,
            timeout=15.0,
        )
        response.raise_for_status()


@router.delete("/account", status_code=status.HTTP_204_NO_CONTENT)
def delete_account(
    response: Response,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> Response:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    email = user.email if user else None
    profile_photo_url = user.profile_photo_url if user else None
    try:
        lost_reports = session.scalars(
            select(LostItem).where(LostItem.created_by == uid)
        ).all()
        found_reports = session.scalars(
            select(FoundItem).where(FoundItem.created_by == uid)
        ).all()
        report_ids = {item.id for item in (*lost_reports, *found_reports)}
        image_urls = {
            url
            for item in (*lost_reports, *found_reports)
            for url in [
                item.image_url,
                *(item.image_urls if isinstance(item.image_urls, list) else []),
                item.social_poster_url,
            ]
            if url
        }
        if profile_photo_url:
            image_urls.add(profile_photo_url)
        if report_ids:
            _require_supabase_cleanup_config()
        for image_url in image_urls:
            if not _photo_is_still_referenced(session, image_url, uid):
                _delete_profile_photo_asset(image_url)

        app = _firebase_app()
        _delete_firestore_account_copies(app, uid, email)
        _delete_supabase_account_matching_data(report_ids)
        if report_ids:
            session.execute(
                delete(SocialPublication).where(SocialPublication.report_id.in_(report_ids))
            )
            session.execute(
                delete(ContentReport).where(
                    ContentReport.report_type.in_(("lost", "found")),
                    ContentReport.report_id.in_(report_ids),
                )
            )
            session.execute(
                delete(AdminMatchAlert).where(
                    or_(
                        AdminMatchAlert.found_item_id.in_(report_ids),
                        AdminMatchAlert.lost_item_id.in_(report_ids),
                    )
                )
            )
            session.execute(
                delete(UserNotification).where(
                    UserNotification.found_item_id.in_(report_ids)
                )
            )
        session.execute(delete(LostItem).where(LostItem.created_by == uid))
        session.execute(delete(FoundItem).where(FoundItem.created_by == uid))
        session.execute(delete(UserNotification).where(UserNotification.firebase_uid == uid))
        session.execute(
            delete(UserBlock).where(
                or_(UserBlock.blocker_uid == uid, UserBlock.blocked_uid == uid)
            )
        )
        session.execute(delete(ContentReport).where(ContentReport.reporter_uid == uid))
        session.execute(delete(DeviceToken).where(DeviceToken.firebase_uid == uid))
        session.execute(delete(EmailOTPChallenge).where(EmailOTPChallenge.firebase_uid == uid))
        session.execute(delete(UsernameReservation).where(UsernameReservation.firebase_uid == uid))
        session.execute(delete(User).where(User.firebase_uid == uid))
        session.flush()

        session.commit()
        try:
            firebase_auth.delete_user(uid, app=app)
        except firebase_auth.UserNotFoundError:
            logger.info("Firebase Auth user was already deleted")
    except firebase_admin.exceptions.FirebaseError as exc:
        session.rollback()
        logger.exception("Firebase account data deletion failed")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Account deletion could not finish; please retry",
        ) from exc
    except Exception as exc:
        session.rollback()
        logger.exception("Account deletion failed")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Account deletion could not finish; please retry",
        ) from exc

    response.status_code = status.HTTP_204_NO_CONTENT
    return response


@router.get("/notifications")
def get_user_notifications(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    unread_count = session.scalar(
        select(func.count(UserNotification.id)).where(
            UserNotification.firebase_uid == uid,
            UserNotification.is_read.is_(False),
        )
    ) or 0
    notifications = session.scalars(
        select(UserNotification)
        .where(UserNotification.firebase_uid == uid)
        .order_by(desc(UserNotification.created_at))
        .limit(50)
    ).all()
    return {
        "unread_count": unread_count,
        "notifications": [
            {
                "id": notification.id,
                "found_item_id": notification.found_item_id,
                "title": notification.title,
                "body": notification.body,
                "score": notification.score,
                "is_read": notification.is_read,
                "created_at": notification.created_at.isoformat() if notification.created_at else None,
            }
            for notification in notifications
        ],
    }


@router.post("/notifications/read")
def mark_user_notifications_read(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, int]:
    result = session.execute(
        update(UserNotification)
        .where(
            UserNotification.firebase_uid == uid,
            UserNotification.is_read.is_(False),
        )
        .values(is_read=True)
    )
    session.commit()
    return {"updated": result.rowcount or 0}


def normalize_username(value: str) -> str:
    base = value.strip().lower()
    if base.startswith("@"):
        base = base[1:]
    import re
    base = re.sub(r"[^a-z0-9_]", "_", base)
    base = re.sub(r"_+", "_", base)
    return base.strip("_")


def _username_is_taken(session: Session, normalized: str) -> bool:
    try:
        reserved = session.get(UsernameReservation, normalized)
        existing_profile = session.scalar(
            select(User.id)
            .where(func.lower(User.username) == normalized)
            .limit(1)
        )
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username availability is temporarily unavailable",
        ) from exc
    return reserved is not None or existing_profile is not None


def _firebase_uid_exists(uid: str) -> bool:
    try:
        firebase_auth.get_user(uid, app=_firebase_app())
        return True
    except firebase_auth.UserNotFoundError:
        return False
    except (firebase_admin.exceptions.FirebaseError, ValueError) as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username availability is temporarily unavailable",
        ) from exc


def _release_stale_username_claims(session: Session, normalized: str) -> None:
    try:
        reservation = session.get(UsernameReservation, normalized)
        profiles = session.scalars(
            select(User).where(func.lower(User.username) == normalized)
        ).all()
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username availability is temporarily unavailable",
        ) from exc

    owner_uids = {
        claim.firebase_uid
        for claim in ([reservation] if reservation is not None else []) + profiles
    }
    stale_uids = {uid for uid in owner_uids if not _firebase_uid_exists(uid)}
    if not stale_uids:
        return

    if reservation is not None and reservation.firebase_uid in stale_uids:
        session.delete(reservation)
    for profile in profiles:
        if profile.firebase_uid in stale_uids:
            profile.username = None
    try:
        session.commit()
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username availability is temporarily unavailable",
        ) from exc


def _username_exists_in_firebase_auth(normalized: str) -> bool:
    email = f"{normalized}@login.fendly.app"
    try:
        firebase_auth.get_user_by_email(email, app=_firebase_app())
        return True
    except firebase_auth.UserNotFoundError:
        return False
    except firebase_admin.exceptions.FirebaseError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username availability is temporarily unavailable",
        ) from exc
    except ValueError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username availability is temporarily unavailable",
        ) from exc


@router.get("/username-availability/{username}")
def check_registration_username(
    username: str,
    session: Session = Depends(get_db),
) -> dict[str, object]:
    normalized = normalize_username(username)
    if len(normalized) < 3 or len(normalized) > 32 or not normalized.replace("_", "").isalnum():
        return {"username": normalized, "available": False}
    if _username_exists_in_firebase_auth(normalized):
        return {"username": normalized, "available": False}
    _release_stale_username_claims(session, normalized)
    return {
        "username": normalized,
        "available": not _username_is_taken(session, normalized),
    }


@router.get("/username/{username}")
def check_username(
    username: str,
    session: Session = Depends(get_db),
    _: str = Depends(get_current_user),
) -> dict[str, object]:
    normalized = normalize_username(username)
    if len(normalized) < 3 or len(normalized) > 32 or not normalized.replace("_", "").isalnum():
        return {"username": normalized, "available": False}
    _release_stale_username_claims(session, normalized)
    taken = _username_is_taken(session, normalized)
    return {"username": normalized, "available": not taken}


@router.post("/username", status_code=201)
def reserve_username(
    payload: UsernameRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    normalized = normalize_username(payload.username)
    _release_stale_username_claims(session, normalized)
    try:
        existing = session.get(UsernameReservation, normalized)
        if existing is not None and existing.firebase_uid != uid:
            raise HTTPException(409, "Username is already taken")
        existing_profile_uid = session.scalar(
            select(User.firebase_uid)
            .where(
                func.lower(User.username) == normalized,
                User.firebase_uid != uid,
            )
            .limit(1)
        )
        if existing_profile_uid is not None:
            raise HTTPException(409, "Username is already taken")
        owned = session.scalar(select(UsernameReservation).where(UsernameReservation.firebase_uid == uid))
        if owned is not None and owned.username != normalized:
            session.delete(owned)
            session.flush()
        if existing is None:
            session.add(UsernameReservation(username=normalized, firebase_uid=uid))
        session.commit()
    except HTTPException:
        session.rollback()
        raise
    except IntegrityError as exc:
        session.rollback()
        raise HTTPException(409, "Username is already taken") from exc
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Username reservation is temporarily unavailable",
        ) from exc
    return {"username": normalized, "status": "reserved"}


@router.get("/profile")
def get_profile(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, Any]:
    try:
        user = session.scalar(select(User).where(User.firebase_uid == uid))
        if user is None:
            user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
            session.add(user)
            try:
                session.commit()
                session.refresh(user)
            except Exception:
                session.rollback()
        return {
            "username": getattr(user, "username", None) or "",
            "full_name": getattr(user, "full_name", None) or "",
            "email": getattr(user, "email", None) or "",
            "mobile": getattr(user, "mobile", None) or "",
            "state": getattr(user, "state", None) or "",
            "city": getattr(user, "city", None) or "",
            "profile_photo_url": getattr(user, "profile_photo_url", None) or "",
            "instagram_url": getattr(user, "instagram_url", None) or "",
            "facebook_url": getattr(user, "facebook_url", None) or "",
            "email_verified": bool(getattr(user, "email_verified", False)),
            "mobile_verified": bool(getattr(user, "mobile_verified", False)),
            "is_verified": bool(getattr(user, "email_verified", False)),
        }
    except Exception as exc:
        logger.error("Profile fetch error: %s", exc)
        return {
            "username": "",
            "full_name": "",
            "email": "",
            "mobile": "",
            "state": "",
            "city": "",
            "profile_photo_url": "",
            "instagram_url": "",
            "facebook_url": "",
            "email_verified": False,
            "mobile_verified": False,
            "is_verified": False,
        }


@router.put("/profile")
def update_profile(
    payload: ProfileUpdate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    try:
        update_profile_record(session, payload, uid)
        return {"status": "saved"}
    except HTTPException:
        session.rollback()
        raise
    except Exception as exc:
        session.rollback()
        logger.error("Profile update error: %s", exc)
        raise HTTPException(500, f"Failed to update profile: {exc}")
