ALTER TABLE public.adoption_listings
    ADD COLUMN IF NOT EXISTS social_post_image_url varchar(1000);
