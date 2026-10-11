import io
import logging
import os
import re
from uuid import uuid4

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status
from PIL import Image, UnidentifiedImageError
from sqlalchemy import desc, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from adoption_social import publish_adoption_listing
from adoption_storage import delete_adoption_image, store_adoption_image
from auth import get_current_user
from database import get_db
from models import AdoptionApplication, AdoptionListing, User, UserNotification
from moderation import moderate_content
from notifications import send_user_notification_push


router = APIRouter(prefix="/api/adoptions", tags=["adoptions"])
logger = logging.getLogger(__name__)
MAX_IMAGE_BYTES = 10 * 1024 * 1024
MAX_IMAGES = 5
MIME_FORMATS = {
    "image/jpeg": ("JPEG", "jpg"),
    "image/png": ("PNG", "png"),
    "image/webp": ("WEBP", "webp"),
}
PHONE_PATTERN = re.compile(r"^\+?[0-9][0-9 ()-]{6,30}$")


def _check_image(data: bytes, mime_type: str | None) -> str:
    expected = MIME_FORMATS.get((mime_type or "").split(";", 1)[0].strip().lower())
    if expected is None:
        raise HTTPException(415, "Only JPEG, PNG, and WebP pet photos are supported")
    try:
        with Image.open(io.BytesIO(data)) as image:
            image_format = image.format
            width, height = image.size
            if width <= 0 or height <= 0 or width * height > 40_000_000:
                raise HTTPException(413, "Image dimensions are too large")
            image.verify()
        with Image.open(io.BytesIO(data)) as image:
            image.load()
    except HTTPException:
        raise
    except (UnidentifiedImageError, OSError, ValueError, Image.DecompressionBombError) as exc:
        raise HTTPException(422, "Uploaded file is not a valid image") from exc
    if image_format != expected[0]:
        raise HTTPException(415, "Image content does not match its MIME type")
    return expected[1]


def _serialize_listing(
    listing: AdoptionListing,
    *,
    include_owner_fields: bool = False,
) -> dict[str, object]:
    result: dict[str, object] = {
        "id": listing.id,
        "pet_name": listing.pet_name,
        "species": listing.species,
        "breed": listing.breed,
        "age_months": listing.age_months,
        "gender": listing.gender,
        "health_status": listing.health_status,
        "description": listing.description,
        "location_city": listing.location_city,
        "images": listing.images or [],
        "status": listing.status,
        "created_at": listing.created_at.isoformat() if listing.created_at else None,
    }
    if include_owner_fields:
        result.update(
            {
                "auto_post_social": listing.auto_post_social,
                "social_post_status": listing.social_post_status,
                "social_post_error": listing.social_post_error,
                "facebook_post_id": listing.facebook_post_id,
                "instagram_post_id": listing.instagram_post_id,
            }
        )
    return result


def _publish_listing_social(listing: AdoptionListing, session: Session) -> None:
    if not listing.auto_post_social:
        return
    listing.social_post_status = "pending"
    listing.social_post_error = None
    session.commit()
    def record_publication(provider: str, external_id: str) -> None:
        if provider == "facebook":
            listing.facebook_post_id = external_id
        elif provider == "instagram":
            listing.instagram_post_id = external_id
        session.commit()

    publications, errors = publish_adoption_listing(listing, record_publication)
    listing.facebook_post_id = publications.get("facebook", listing.facebook_post_id)
    listing.instagram_post_id = publications.get("instagram", listing.instagram_post_id)
    if errors:
        listing.social_post_status = "failed"
        listing.social_post_error = "; ".join(errors)[:500]
    else:
        listing.social_post_status = "published"
        listing.social_post_error = None
    session.commit()


@router.get("")
def list_adoptions(
    species: str | None = None,
    min_age_months: int | None = None,
    max_age_months: int | None = None,
    city: str | None = None,
    session: Session = Depends(get_db),
) -> list[dict[str, object]]:
    if min_age_months is not None and not 0 <= min_age_months <= 600:
        raise HTTPException(422, "min_age_months must be between 0 and 600")
    if max_age_months is not None and not 0 <= max_age_months <= 600:
        raise HTTPException(422, "max_age_months must be between 0 and 600")
    if (
        min_age_months is not None
        and max_age_months is not None
        and min_age_months > max_age_months
    ):
        raise HTTPException(422, "min_age_months must not exceed max_age_months")
    query = select(AdoptionListing).where(
        AdoptionListing.status.in_(("available", "pending"))
    )
    if species and species.strip():
        query = query.where(AdoptionListing.species.ilike(species.strip()))
    if min_age_months is not None:
        query = query.where(AdoptionListing.age_months >= min_age_months)
    if max_age_months is not None:
        query = query.where(AdoptionListing.age_months <= max_age_months)
    if city and city.strip():
        query = query.where(AdoptionListing.location_city.ilike(f"%{city.strip()}%"))
    listings = session.scalars(
        query.order_by(desc(AdoptionListing.created_at)).limit(100)
    ).all()
    return [_serialize_listing(listing) for listing in listings]


@router.get("/mine")
def list_my_adoptions(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    listings = session.scalars(
        select(AdoptionListing)
        .where(AdoptionListing.lister_user_id == uid)
        .order_by(desc(AdoptionListing.created_at))
    ).all()
    return [_serialize_listing(listing, include_owner_fields=True) for listing in listings]


@router.get("/applications/mine")
def list_my_adoption_applications(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    applications = session.scalars(
        select(AdoptionApplication)
        .where(AdoptionApplication.applicant_user_id == uid)
        .order_by(desc(AdoptionApplication.created_at))
    ).all()
    results = []
    for application in applications:
        listing = session.get(AdoptionListing, application.listing_id)
        if listing is None:
            continue
        result: dict[str, object] = {
            "id": application.id,
            "listing_id": listing.id,
            "pet_name": listing.pet_name,
            "status": application.status,
            "created_at": (
                application.created_at.isoformat() if application.created_at else None
            ),
        }
        if application.status == "approved":
            lister = session.scalar(
                select(User).where(User.firebase_uid == listing.lister_user_id)
            )
            result["lister_contact_phone"] = (
                lister.mobile if lister and lister.mobile_verified else None
            )
        results.append(result)
    return results


@router.get("/{listing_id}")
def get_adoption(
    listing_id: str,
    session: Session = Depends(get_db),
) -> dict[str, object]:
    listing = session.get(AdoptionListing, listing_id)
    if listing is None or listing.status == "adopted":
        raise HTTPException(404, "Adoption listing not found")
    return _serialize_listing(listing)


@router.post("", status_code=status.HTTP_201_CREATED)
async def create_adoption(
    pet_name: str = Form(..., min_length=1, max_length=120),
    species: str = Form(..., min_length=1, max_length=60),
    breed: str | None = Form(default=None, max_length=120),
    age_months: int = Form(..., ge=0, le=600),
    gender: str = Form(..., min_length=1, max_length=32),
    health_status: str = Form(..., min_length=1, max_length=500),
    description: str = Form(..., min_length=1, max_length=5000),
    location_city: str = Form(..., min_length=1, max_length=120),
    auto_post_social: bool = Form(default=False),
    images: list[UploadFile] = File(...),
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    if not images or len(images) > MAX_IMAGES:
        raise HTTPException(422, f"Provide between 1 and {MAX_IMAGES} pet photos")

    prepared: list[tuple[bytes, str, str]] = []
    for uploaded in images:
        data = await uploaded.read(MAX_IMAGE_BYTES + 1)
        if len(data) > MAX_IMAGE_BYTES:
            raise HTTPException(413, "Each pet photo must be 10 MiB or smaller")
        extension = _check_image(data, uploaded.content_type)
        content_type = MIME_FORMATS[(uploaded.content_type or "").split(";", 1)[0].strip().lower()][0]
        normalized_type = f"image/{'jpeg' if content_type == 'JPEG' else content_type.lower()}"
        rejection = await moderate_content(
            pet_name,
            description,
            image_data=data,
            image_content_type=normalized_type,
        )
        if rejection:
            raise HTTPException(422, rejection)
        prepared.append((data, extension, normalized_type))

    image_urls: list[str] = []
    try:
        for data, extension, content_type in prepared:
            image_urls.append(
                await store_adoption_image(
                    data,
                    f"{uid}/{uuid4().hex}.{extension}",
                    content_type,
                )
            )
    except Exception as exc:
        for image_url in image_urls:
            try:
                delete_adoption_image(image_url)
            except Exception:
                logger.exception("Failed to clean up partial adoption image upload")
        logger.exception("Adoption image upload failed")
        raise HTTPException(503, "Pet photos could not be uploaded; please try again") from exc

    listing = AdoptionListing(
        id=str(uuid4()),
        lister_user_id=uid,
        pet_name=pet_name.strip(),
        species=species.strip().lower(),
        breed=breed.strip() or None if breed else None,
        age_months=age_months,
        gender=gender.strip(),
        health_status=health_status.strip(),
        description=description.strip(),
        location_city=location_city.strip(),
        images=image_urls,
        status="available",
        auto_post_social=auto_post_social,
        social_post_status="pending" if auto_post_social else None,
    )
    try:
        session.add(listing)
        session.commit()
        session.refresh(listing)
    except Exception as exc:
        session.rollback()
        for image_url in image_urls:
            try:
                delete_adoption_image(image_url)
            except Exception:
                logger.exception("Failed to clean up adoption photos after database failure")
        logger.exception("Adoption listing could not be saved")
        raise HTTPException(503, "Adoption listing could not be saved") from exc

    if auto_post_social:
        try:
            _publish_listing_social(listing, session)
        except (ValueError, RuntimeError) as exc:
            session.rollback()
            logger.warning("Adoption social publishing did not finish: %s", exc)
            listing = session.get(AdoptionListing, listing.id) or listing
            listing.social_post_status = "failed"
            listing.social_post_error = str(exc)[:500]
            session.commit()
    return _serialize_listing(listing, include_owner_fields=True)


@router.post("/{listing_id}/apply", status_code=status.HTTP_201_CREATED)
def apply_to_adoption(
    listing_id: str,
    home_type: str = Form(..., min_length=1, max_length=120),
    pet_experience: str = Form(..., min_length=1, max_length=3000),
    contact_phone: str = Form(..., min_length=7, max_length=32),
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    listing = session.scalar(
        select(AdoptionListing)
        .where(AdoptionListing.id == listing_id)
        .with_for_update()
    )
    if listing is None or listing.status not in ("available", "pending"):
        raise HTTPException(404, "This pet is not currently accepting applications")
    if listing.lister_user_id == uid:
        raise HTTPException(403, "You cannot apply to adopt your own listing")
    normalized_phone = contact_phone.strip()
    if not PHONE_PATTERN.fullmatch(normalized_phone):
        raise HTTPException(422, "Enter a valid contact phone number")
    application = AdoptionApplication(
        id=str(uuid4()),
        listing_id=listing.id,
        applicant_user_id=uid,
        home_type=home_type.strip(),
        pet_experience=pet_experience.strip(),
        contact_phone=normalized_phone,
        status="submitted",
    )
    try:
        session.add(application)
        listing.status = "pending"
        session.commit()
        session.refresh(application)
    except IntegrityError as exc:
        session.rollback()
        raise HTTPException(409, "You have already applied for this pet") from exc
    return {
        "id": application.id,
        "listing_id": listing.id,
        "status": application.status,
    }


@router.get("/{listing_id}/applications")
def list_listing_applications(
    listing_id: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> list[dict[str, object]]:
    listing = session.get(AdoptionListing, listing_id)
    if listing is None:
        raise HTTPException(404, "Adoption listing not found")
    if listing.lister_user_id != uid:
        raise HTTPException(403, "Only the lister can review applications")
    applications = session.scalars(
        select(AdoptionApplication)
        .where(AdoptionApplication.listing_id == listing.id)
        .order_by(desc(AdoptionApplication.created_at))
    ).all()
    return [
        {
            "id": application.id,
            "home_type": application.home_type,
            "pet_experience": application.pet_experience,
            "contact_phone": application.contact_phone,
            "status": application.status,
            "created_at": (
                application.created_at.isoformat() if application.created_at else None
            ),
        }
        for application in applications
    ]


@router.post("/applications/{application_id}/decision")
def decide_adoption_application(
    application_id: str,
    decision: str = Form(..., pattern="^(approved|rejected)$"),
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    application = session.scalar(
        select(AdoptionApplication)
        .where(AdoptionApplication.id == application_id)
        .with_for_update()
    )
    if application is None:
        raise HTTPException(404, "Adoption application not found")
    listing = session.scalar(
        select(AdoptionListing)
        .where(AdoptionListing.id == application.listing_id)
        .with_for_update()
    )
    if listing is None:
        raise HTTPException(404, "Adoption listing not found")
    if listing.lister_user_id != uid:
        raise HTTPException(403, "Only the lister can decide applications")
    if application.status != "submitted":
        raise HTTPException(409, "This application has already been decided")
    if decision == "approved" and listing.status not in ("available", "pending"):
        raise HTTPException(409, "This pet is no longer available")

    application.status = decision
    if decision == "approved":
        listing.status = "adopted"
        other_applications = session.scalars(
            select(AdoptionApplication).where(
                AdoptionApplication.listing_id == listing.id,
                AdoptionApplication.id != application.id,
                AdoptionApplication.status == "submitted",
            )
        ).all()
        for other in other_applications:
            other.status = "rejected"
            session.add(
                UserNotification(
                    firebase_uid=other.applicant_user_id,
                    found_item_id=None,
                    adoption_listing_id=listing.id,
                    notification_type="adoption_application_update",
                    title="Adoption application update",
                    body=f"The application for {listing.pet_name} was not selected.",
                    score=0.0,
                )
            )
    else:
        remaining = session.scalar(
            select(AdoptionApplication.id).where(
                AdoptionApplication.listing_id == listing.id,
                AdoptionApplication.id != application.id,
                AdoptionApplication.status == "submitted",
            ).limit(1)
        )
        if remaining is None:
            listing.status = "available"

    notification = UserNotification(
        firebase_uid=application.applicant_user_id,
        found_item_id=None,
        adoption_listing_id=listing.id,
        notification_type="adoption_application_update",
        title="Adoption application update",
        body=(
            f"Your application for {listing.pet_name} was approved. "
            "Open Fendly to view the lister's contact details."
            if decision == "approved"
            else f"Your application for {listing.pet_name} was not selected."
        ),
        score=0.0,
    )
    session.add(notification)
    session.commit()
    send_user_notification_push(session, notification)
    return {"status": decision}


@router.post("/{listing_id}/social-post/retry")
def retry_adoption_social_post(
    listing_id: str,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    from routers.admin import require_admin

    require_admin(uid=uid, session=session)
    listing = session.get(AdoptionListing, listing_id)
    if listing is None:
        raise HTTPException(404, "Adoption listing not found")
    if not listing.auto_post_social:
        raise HTTPException(409, "The lister did not opt in to social auto-posting")
    _publish_listing_social(listing, session)
    return _serialize_listing(listing, include_owner_fields=True)
