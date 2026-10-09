BEGIN;

CREATE TABLE IF NOT EXISTS public.user_blocks (
    blocker_uid VARCHAR(128) NOT NULL,
    blocked_uid VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT user_blocks_pkey PRIMARY KEY (blocker_uid, blocked_uid),
    CONSTRAINT user_blocks_no_self_block_check CHECK (blocker_uid <> blocked_uid)
);

CREATE INDEX IF NOT EXISTS user_blocks_blocker_created_idx
    ON public.user_blocks (blocker_uid, created_at DESC);

ALTER TABLE public.user_blocks ENABLE ROW LEVEL SECURITY;
REVOKE ALL PRIVILEGES ON TABLE public.user_blocks
    FROM PUBLIC, anon, authenticated;

COMMIT;
