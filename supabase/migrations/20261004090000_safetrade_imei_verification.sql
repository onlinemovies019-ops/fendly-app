BEGIN;

ALTER TABLE public.lost_items
    ADD COLUMN IF NOT EXISTS status varchar(16) NOT NULL DEFAULT 'LOST',
    ADD COLUMN IF NOT EXISTS imei_hash varchar(64);

CREATE INDEX IF NOT EXISTS lost_items_status_imei_hash_idx
    ON public.lost_items (status, imei_hash);

CREATE TABLE IF NOT EXISTS public.public_imei_lookup_rate_limits (
    quota_key varchar(64) PRIMARY KEY,
    window_started bigint NOT NULL,
    request_count integer NOT NULL DEFAULT 0
);

ALTER TABLE public.public_imei_lookup_rate_limits ENABLE ROW LEVEL SECURITY;
REVOKE ALL PRIVILEGES ON TABLE public.public_imei_lookup_rate_limits
    FROM PUBLIC, anon, authenticated;

COMMIT;
