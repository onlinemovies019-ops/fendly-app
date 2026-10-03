CREATE TABLE IF NOT EXISTS public.email_otp_challenges (
    firebase_uid varchar(128) PRIMARY KEY,
    email varchar(320) NOT NULL,
    code_digest varchar(64) NOT NULL,
    sent_at bigint NOT NULL,
    expires_at bigint NOT NULL,
    send_window_started bigint NOT NULL DEFAULT 0,
    send_count integer NOT NULL DEFAULT 0,
    attempts integer NOT NULL DEFAULT 0,
    sent boolean NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS email_otp_challenges_email_idx
    ON public.email_otp_challenges (email);

CREATE INDEX IF NOT EXISTS email_otp_challenges_expires_at_idx
    ON public.email_otp_challenges (expires_at);

ALTER TABLE public.email_otp_challenges ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE public.email_otp_challenges FROM anon, authenticated;
