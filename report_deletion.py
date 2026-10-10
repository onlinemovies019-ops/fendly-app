from fastapi import HTTPException
from sqlalchemy import delete, or_
from sqlalchemy.orm import Session

from models import (
    AdminMatchAlert,
    ContentReport,
    FoundItem,
    LostItem,
    UserNotification,
)


def delete_report_from_app(
    session: Session,
    report: LostItem | FoundItem,
    report_type: str,
) -> None:
    from routers.admin import _supabase_admin_alert_request

    for column in ("lost_item_id", "found_item_id"):
        try:
            _supabase_admin_alert_request(
                "DELETE",
                "admin_match_alerts",
                params={column: f"eq.{report.id}"},
            )
        except HTTPException as error:
            raise HTTPException(
                503,
                "The report was not deleted because its admin match alerts "
                "could not be cleared. Retry after the notification store is available.",
            ) from error

    session.execute(
        delete(ContentReport).where(
            ContentReport.report_type == report_type,
            ContentReport.report_id == report.id,
        )
    )
    session.execute(
        delete(AdminMatchAlert).where(
            or_(
                AdminMatchAlert.lost_item_id == report.id,
                AdminMatchAlert.found_item_id == report.id,
            )
        )
    )
    if report_type == "found":
        session.execute(
            delete(UserNotification).where(
                UserNotification.found_item_id == report.id
            )
        )
    session.delete(report)
    session.commit()
