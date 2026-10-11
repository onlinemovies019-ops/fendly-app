BEGIN;

ALTER TABLE public.admin_match_alerts
    ADD COLUMN IF NOT EXISTS verified_by varchar(128),
    ADD COLUMN IF NOT EXISTS verification_notes text,
    ADD COLUMN IF NOT EXISTS reviewed_at timestamptz;

ALTER TABLE public.lost_items
    ADD COLUMN IF NOT EXISTS allow_success_story_sharing boolean NOT NULL DEFAULT false;
ALTER TABLE public.found_items
    ADD COLUMN IF NOT EXISTS status varchar(16) NOT NULL DEFAULT 'FOUND',
    ADD COLUMN IF NOT EXISTS allow_success_story_sharing boolean NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS public.user_notifications (
    id varchar(36) PRIMARY KEY,
    firebase_uid varchar(128) NOT NULL,
    found_item_id varchar(36) NOT NULL,
    lost_item_id varchar(36),
    match_alert_id varchar(36),
    notification_type varchar(32) NOT NULL DEFAULT 'match_verified',
    title varchar(160) NOT NULL,
    body text NOT NULL,
    score double precision NOT NULL DEFAULT 0,
    is_read boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.user_notifications
    ADD COLUMN IF NOT EXISTS lost_item_id varchar(36),
    ADD COLUMN IF NOT EXISTS match_alert_id varchar(36),
    ADD COLUMN IF NOT EXISTS notification_type varchar(32) NOT NULL DEFAULT 'match_verified';

CREATE INDEX IF NOT EXISTS user_notifications_firebase_uid_idx
    ON public.user_notifications (firebase_uid);
CREATE INDEX IF NOT EXISTS user_notifications_found_item_id_idx
    ON public.user_notifications (found_item_id);
CREATE INDEX IF NOT EXISTS user_notifications_lost_item_id_idx
    ON public.user_notifications (lost_item_id);
CREATE INDEX IF NOT EXISTS user_notifications_match_alert_id_idx
    ON public.user_notifications (match_alert_id);
CREATE UNIQUE INDEX IF NOT EXISTS user_notifications_match_recipient_uq
    ON public.user_notifications (match_alert_id, firebase_uid)
    WHERE match_alert_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS public.private_image_migration_cleanup (
    source_key varchar(64) PRIMARY KEY,
    source_kind varchar(16) NOT NULL,
    source_locator varchar(1000) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    cleaned_at timestamptz,
    last_error varchar(500)
);

ALTER TABLE public.private_image_migration_cleanup ENABLE ROW LEVEL SECURITY;
REVOKE ALL PRIVILEGES ON TABLE public.private_image_migration_cleanup
FROM PUBLIC, anon, authenticated;
DROP POLICY IF EXISTS fendly_private_image_migration_deny_clients
    ON public.private_image_migration_cleanup;
CREATE POLICY fendly_private_image_migration_deny_clients
    ON public.private_image_migration_cleanup
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);
GRANT SELECT, INSERT, UPDATE, DELETE
    ON TABLE public.private_image_migration_cleanup TO service_role;
CREATE INDEX IF NOT EXISTS private_image_migration_cleanup_pending_idx
    ON public.private_image_migration_cleanup (created_at)
    WHERE cleaned_at IS NULL;

ALTER TABLE public.lost_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.found_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_match_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.items ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES ON TABLE
    public.lost_items,
    public.found_items,
    public.admin_match_alerts,
    public.user_notifications,
    public.items
FROM PUBLIC, anon, authenticated;

DROP POLICY IF EXISTS fendly_private_lost_items_deny_clients ON public.lost_items;
CREATE POLICY fendly_private_lost_items_deny_clients ON public.lost_items
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

DROP POLICY IF EXISTS fendly_private_found_items_deny_clients ON public.found_items;
CREATE POLICY fendly_private_found_items_deny_clients ON public.found_items
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

DROP POLICY IF EXISTS fendly_private_admin_match_alerts_deny_clients ON public.admin_match_alerts;
CREATE POLICY fendly_private_admin_match_alerts_deny_clients ON public.admin_match_alerts
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

DROP POLICY IF EXISTS fendly_private_user_notifications_deny_clients ON public.user_notifications;
CREATE POLICY fendly_private_user_notifications_deny_clients ON public.user_notifications
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

DROP POLICY IF EXISTS fendly_private_items_index_deny_clients ON public.items;
CREATE POLICY fendly_private_items_index_deny_clients ON public.items
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE
    public.lost_items,
    public.found_items,
    public.admin_match_alerts,
    public.user_notifications,
    public.items
TO service_role;

UPDATE storage.buckets
SET public = false
WHERE id = 'uploads' OR name = 'uploads';

INSERT INTO storage.buckets (
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
)
VALUES (
    'report-images',
    'report-images',
    false,
    10485760,
    ARRAY['image/jpeg', 'image/png', 'image/webp']::text[]
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    public = false,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

INSERT INTO storage.buckets (
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
)
VALUES (
    'private-vault',
    'private-vault',
    false,
    10485788,
    ARRAY['application/octet-stream']::text[]
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    public = false,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

DROP POLICY IF EXISTS fendly_private_uploads_deny_clients ON storage.objects;
CREATE POLICY fendly_private_uploads_deny_clients ON storage.objects
    AS RESTRICTIVE
    FOR ALL TO anon, authenticated
    USING (bucket_id <> 'uploads')
    WITH CHECK (bucket_id <> 'uploads');

DROP POLICY IF EXISTS fendly_private_report_images_deny_clients ON storage.objects;
CREATE POLICY fendly_private_report_images_deny_clients ON storage.objects
    AS RESTRICTIVE
    FOR ALL TO anon, authenticated
    USING (bucket_id <> 'report-images')
    WITH CHECK (bucket_id <> 'report-images');

DROP POLICY IF EXISTS fendly_private_vault_deny_clients ON storage.objects;
CREATE POLICY fendly_private_vault_deny_clients ON storage.objects
    AS RESTRICTIVE
    FOR ALL TO anon, authenticated
    USING (bucket_id <> 'private-vault')
    WITH CHECK (bucket_id <> 'private-vault');

UPDATE public.social_publications
SET status = 'cancelled',
    next_attempt_at = 0,
    last_error = 'Automatic social publishing has been disabled.'
WHERE status IN ('pending', 'processing');

COMMIT;
