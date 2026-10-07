BEGIN;

ALTER TABLE public.lost_items
    ADD COLUMN IF NOT EXISTS social_share_consent boolean NOT NULL DEFAULT false;

ALTER TABLE public.found_items
    ADD COLUMN IF NOT EXISTS social_share_consent boolean NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS public.social_accounts (
    provider varchar(16) PRIMARY KEY,
    account_id varchar(128) NOT NULL,
    account_name varchar(160) NOT NULL,
    access_token_encrypted text NOT NULL,
    refresh_token_encrypted text,
    expires_at bigint,
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.social_oauth_states (
    state_digest varchar(64) PRIMARY KEY,
    provider varchar(16) NOT NULL,
    created_by varchar(128) NOT NULL,
    expires_at bigint NOT NULL,
    code_verifier varchar(128)
);

CREATE INDEX IF NOT EXISTS ix_social_oauth_states_provider
    ON public.social_oauth_states (provider);
CREATE INDEX IF NOT EXISTS ix_social_oauth_states_expires_at
    ON public.social_oauth_states (expires_at);

CREATE TABLE IF NOT EXISTS public.social_publications (
    id varchar(36) PRIMARY KEY,
    report_id varchar(36) NOT NULL,
    report_type varchar(8) NOT NULL,
    provider varchar(16) NOT NULL,
    status varchar(16) NOT NULL DEFAULT 'pending',
    attempt_count integer NOT NULL DEFAULT 0,
    next_attempt_at bigint NOT NULL DEFAULT 0,
    external_post_id varchar(160),
    last_error varchar(500),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz,
    CONSTRAINT social_publications_report_provider_uq UNIQUE (report_id, provider)
);

CREATE INDEX IF NOT EXISTS ix_social_publications_report_id
    ON public.social_publications (report_id);
CREATE INDEX IF NOT EXISTS ix_social_publications_status
    ON public.social_publications (status);
CREATE INDEX IF NOT EXISTS social_publications_status_due_idx
    ON public.social_publications (status, next_attempt_at);

ALTER TABLE public.social_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.social_oauth_states ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.social_publications ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES ON TABLE
    public.social_accounts,
    public.social_oauth_states,
    public.social_publications
FROM PUBLIC, anon, authenticated;

GRANT ALL PRIVILEGES ON TABLE
    public.social_accounts,
    public.social_oauth_states,
    public.social_publications
TO service_role;

COMMIT;
