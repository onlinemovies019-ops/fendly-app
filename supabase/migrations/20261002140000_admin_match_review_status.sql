ALTER TABLE public.admin_match_alerts
    ADD COLUMN IF NOT EXISTS review_status text NOT NULL DEFAULT 'pending';