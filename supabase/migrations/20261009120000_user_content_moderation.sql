BEGIN;

ALTER TABLE public.lost_items
    ADD COLUMN IF NOT EXISTS hidden_from_public BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE public.found_items
    ADD COLUMN IF NOT EXISTS hidden_from_public BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS public.content_reports (
    id VARCHAR(36) PRIMARY KEY,
    reporter_uid VARCHAR(128) NOT NULL,
    report_type VARCHAR(8) NOT NULL CHECK (report_type IN ('lost', 'found')),
    report_id VARCHAR(36) NOT NULL,
    reason VARCHAR(32) NOT NULL CHECK (
        reason IN ('inappropriate', 'spam', 'personal_information', 'fraud', 'other')
    ),
    details VARCHAR(1000),
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT content_reports_reporter_target_uq
        UNIQUE (reporter_uid, report_type, report_id)
);

CREATE INDEX IF NOT EXISTS content_reports_status_created_idx
    ON public.content_reports (status, created_at DESC);
CREATE INDEX IF NOT EXISTS content_reports_reporter_uid_idx
    ON public.content_reports (reporter_uid);
CREATE INDEX IF NOT EXISTS content_reports_report_id_idx
    ON public.content_reports (report_id);

ALTER TABLE public.content_reports ENABLE ROW LEVEL SECURITY;
REVOKE ALL PRIVILEGES ON TABLE public.content_reports
    FROM PUBLIC, anon, authenticated;

COMMIT;
