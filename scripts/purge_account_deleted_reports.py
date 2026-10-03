import argparse
import os
from collections.abc import Callable
from urllib.parse import urlsplit

from sqlalchemy import delete, or_, select
from sqlalchemy.orm import Session

from models import AdminMatchAlert, FoundItem, LostItem, UserNotification


def _delete_firestore_reports(app, report_ids: set[str]) -> None:
    from firebase_admin import firestore

    db = firestore.client(app=app)
    references = {}
    for collection_name in ("found_items", "lost_items"):
        collection = db.collection(collection_name)
        for report_id in report_ids:
            if "/" in report_id:
                raise ValueError("Report ID contains an invalid path separator")
            reference = collection.document(report_id)
            if reference.get().exists:
                references[reference.path] = reference
        for field in ("user_id", "created_by", "uid", "userId"):
            for document in collection.where(field, "==", "account-deleted").stream():
                references[document.reference.path] = document.reference

    batch = db.batch()
    pending = 0
    for reference in references.values():
        batch.delete(reference)
        pending += 1
        if pending == 400:
            batch.commit()
            batch = db.batch()
            pending = 0
    if pending:
        batch.commit()


def purge_account_deleted_reports(
    session: Session,
    *,
    delete_supabase_data: Callable[[set[str]], None],
    delete_firestore_reports: Callable[[set[str]], None],
    delete_asset: Callable[[str], None],
) -> dict[str, int]:
    lost_reports = session.scalars(
        select(LostItem).where(LostItem.created_by == "account-deleted")
    ).all()
    found_reports = session.scalars(
        select(FoundItem).where(FoundItem.created_by == "account-deleted")
    ).all()
    lost_ids = {report.id for report in lost_reports}
    found_ids = {report.id for report in found_reports}
    report_ids = lost_ids | found_ids
    image_urls = {
        image_url
        for report in (*lost_reports, *found_reports)
        for image_url in [
            report.image_url,
            *(report.image_urls if isinstance(report.image_urls, list) else []),
        ]
        if image_url
    }
    counts = {"lost_reports": len(lost_ids), "found_reports": len(found_ids)}
    session.rollback()
    if not report_ids:
        return counts

    from routers.users import _photo_is_still_referenced

    for image_url in image_urls:
        if not _photo_is_still_referenced(session, image_url, "account-deleted"):
            delete_asset(image_url)
    delete_supabase_data(report_ids)
    delete_firestore_reports(report_ids)

    if found_ids or lost_ids:
        alert_predicates = []
        if found_ids:
            alert_predicates.append(AdminMatchAlert.found_item_id.in_(found_ids))
            session.execute(
                delete(UserNotification).where(
                    UserNotification.found_item_id.in_(found_ids)
                )
            )
        if lost_ids:
            alert_predicates.append(AdminMatchAlert.lost_item_id.in_(lost_ids))
        session.execute(delete(AdminMatchAlert).where(or_(*alert_predicates)))

    session.execute(
        delete(LostItem).where(LostItem.created_by == "account-deleted")
    )
    session.execute(
        delete(FoundItem).where(FoundItem.created_by == "account-deleted")
    )
    session.commit()
    return counts


def _validate_media_delete_config(image_urls: set[str]) -> None:
    if not any(urlsplit(url).hostname == "res.cloudinary.com" for url in image_urls):
        return
    required = (
        "CLOUDINARY_CLOUD_NAME",
        "CLOUDINARY_API_KEY",
        "CLOUDINARY_API_SECRET",
    )
    missing = [name for name in required if not os.getenv(name)]
    if missing:
        raise SystemExit(
            "Refusing to purge Cloudinary report images: configure "
            + ", ".join(missing)
            + " in the production service environment."
        )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Purge historical reports retained after account deletion."
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="perform the irreversible purge; without this flag only counts are shown",
    )
    args = parser.parse_args()
    if os.getenv("ENVIRONMENT", "").lower() != "production":
        raise SystemExit("Refusing to run: ENVIRONMENT must be set to production.")

    from auth import _firebase_app
    from database import SessionLocal
    from routers.users import (
        _delete_profile_photo_asset,
        _delete_supabase_account_matching_data,
    )

    with SessionLocal() as session:
        counts = {
            "lost_reports": len(
                session.scalars(
                    select(LostItem.id).where(
                        LostItem.created_by == "account-deleted"
                    )
                ).all()
            ),
            "found_reports": len(
                session.scalars(
                    select(FoundItem.id).where(
                        FoundItem.created_by == "account-deleted"
                    )
                ).all()
            ),
        }
        print(
            "Historical reports found: "
            f"{counts['lost_reports']} lost, {counts['found_reports']} found."
        )
        if not args.apply:
            print("Dry run only. Re-run with --apply to perform the approved purge.")
            return
        if not counts["lost_reports"] and not counts["found_reports"]:
            print("Nothing to purge.")
            return
        image_urls = {
            image_url
            for model in (LostItem, FoundItem)
            for report in session.scalars(
                select(model).where(model.created_by == "account-deleted")
            ).all()
            for image_url in [
                report.image_url,
                *(report.image_urls if isinstance(report.image_urls, list) else []),
            ]
            if image_url
        }
        _validate_media_delete_config(image_urls)
        if not os.getenv("SUPABASE_URL") or not os.getenv("SUPABASE_SERVICE_ROLE_KEY"):
            raise SystemExit(
                "Refusing to purge: production Supabase cleanup credentials are missing."
            )

        actual_counts = purge_account_deleted_reports(
            session,
            delete_supabase_data=_delete_supabase_account_matching_data,
            delete_firestore_reports=lambda ids: _delete_firestore_reports(
                _firebase_app(), ids
            ),
            delete_asset=_delete_profile_photo_asset,
        )
        print(
            "Purge completed: "
            f"{actual_counts['lost_reports']} lost and "
            f"{actual_counts['found_reports']} found reports removed."
        )


if __name__ == "__main__":
    main()
