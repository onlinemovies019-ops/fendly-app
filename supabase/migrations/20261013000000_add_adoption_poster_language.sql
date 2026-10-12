ALTER TABLE public.adoption_listings
    ADD COLUMN IF NOT EXISTS poster_language varchar(16) NOT NULL DEFAULT 'en';
