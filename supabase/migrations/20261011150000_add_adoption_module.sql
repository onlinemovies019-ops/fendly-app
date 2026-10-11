BEGIN;

ALTER TABLE public.user_notifications
    ALTER COLUMN found_item_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS adoption_listing_id varchar(36);

CREATE INDEX IF NOT EXISTS user_notifications_adoption_listing_id_idx
    ON public.user_notifications (adoption_listing_id);

CREATE TABLE IF NOT EXISTS public.adoption_listings (
    id varchar(36) PRIMARY KEY,
    lister_user_id varchar(128) NOT NULL,
    pet_name varchar(120) NOT NULL,
    species varchar(60) NOT NULL,
    breed varchar(120),
    age_months integer NOT NULL
        CONSTRAINT adoption_listings_age_months_check CHECK (age_months BETWEEN 0 AND 600),
    gender varchar(32) NOT NULL,
    health_status varchar(500) NOT NULL,
    description text NOT NULL,
    location_city varchar(120) NOT NULL,
    images json NOT NULL DEFAULT '[]'::json,
    status varchar(16) NOT NULL DEFAULT 'available'
        CONSTRAINT adoption_listings_status_check CHECK (status IN ('available', 'pending', 'adopted')),
    auto_post_social boolean NOT NULL DEFAULT false,
    social_post_status varchar(16),
    social_post_error varchar(500),
    facebook_post_id varchar(160),
    instagram_post_id varchar(160),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS adoption_listings_public_feed_idx
    ON public.adoption_listings (status, species, location_city);
CREATE INDEX IF NOT EXISTS adoption_listings_owner_created_idx
    ON public.adoption_listings (lister_user_id, created_at);

CREATE TABLE IF NOT EXISTS public.adoption_applications (
    id varchar(36) PRIMARY KEY,
    listing_id varchar(36) NOT NULL
        REFERENCES public.adoption_listings(id) ON DELETE CASCADE,
    applicant_user_id varchar(128) NOT NULL,
    home_type varchar(120) NOT NULL,
    pet_experience text NOT NULL,
    contact_phone varchar(32) NOT NULL,
    status varchar(16) NOT NULL DEFAULT 'submitted'
        CONSTRAINT adoption_applications_status_check CHECK (status IN ('submitted', 'approved', 'rejected')),
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT adoption_applications_applicant_uq
        UNIQUE (listing_id, applicant_user_id)
);

CREATE INDEX IF NOT EXISTS adoption_applications_listing_status_idx
    ON public.adoption_applications (listing_id, status);
CREATE INDEX IF NOT EXISTS adoption_applications_applicant_user_id_idx
    ON public.adoption_applications (applicant_user_id);

ALTER TABLE public.adoption_listings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.adoption_applications ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES ON TABLE
    public.adoption_listings,
    public.adoption_applications
FROM PUBLIC, anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE
    public.adoption_listings,
    public.adoption_applications
TO service_role;

DROP POLICY IF EXISTS fendly_private_adoption_listings_deny_clients
    ON public.adoption_listings;
CREATE POLICY fendly_private_adoption_listings_deny_clients
    ON public.adoption_listings
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

DROP POLICY IF EXISTS fendly_private_adoption_applications_deny_clients
    ON public.adoption_applications;
CREATE POLICY fendly_private_adoption_applications_deny_clients
    ON public.adoption_applications
    FOR ALL TO anon, authenticated USING (false) WITH CHECK (false);

INSERT INTO storage.buckets (
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
)
VALUES (
    'adoption-images',
    'adoption-images',
    true,
    10485760,
    ARRAY['image/jpeg', 'image/png', 'image/webp']::text[]
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    public = true,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

COMMIT;
