CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.items (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id text UNIQUE,
    title text NOT NULL,
    description text NOT NULL DEFAULT '',
    image_url text NOT NULL,
    type text NOT NULL CHECK (type IN ('lost', 'found')),
    created_at timestamptz NOT NULL DEFAULT now(),
    embedding vector(512) NOT NULL
);

CREATE INDEX IF NOT EXISTS items_embedding_hnsw_idx
    ON public.items USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS items_type_created_at_idx
    ON public.items (type, created_at DESC);

CREATE TABLE IF NOT EXISTS public.admin_match_alerts (
    id varchar(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    found_item_id text NOT NULL,
    lost_item_id text NOT NULL,
    found_title text NOT NULL,
    lost_title text NOT NULL,
    confidence double precision NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    reason text NOT NULL,
    is_read boolean NOT NULL DEFAULT false,
    email_sent boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_admin_match_alert_pair UNIQUE (found_item_id, lost_item_id)
);

CREATE INDEX IF NOT EXISTS admin_match_alerts_created_at_idx
    ON public.admin_match_alerts (created_at DESC);

CREATE OR REPLACE FUNCTION public.match_items(
    query_embedding vector(512),
    match_threshold float,
    match_count int,
    filter_type text DEFAULT NULL
)
RETURNS TABLE (
    id uuid,
    source_id text,
    title text,
    description text,
    image_url text,
    type text,
    created_at timestamptz,
    similarity float
)
LANGUAGE sql
STABLE
SET search_path = public
AS $$
    SELECT
        item.id,
        item.source_id,
        item.title,
        item.description,
        item.image_url,
        item.type,
        item.created_at,
        (1 - (item.embedding <=> query_embedding))::float AS similarity
    FROM public.items AS item
    WHERE (filter_type IS NULL OR item.type = filter_type)
      AND 1 - (item.embedding <=> query_embedding) >= match_threshold
    ORDER BY item.embedding <=> query_embedding ASC
    LIMIT GREATEST(match_count, 0);
$$;

ALTER TABLE public.items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_match_alerts ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE ON public.items TO service_role;
GRANT SELECT, INSERT, UPDATE ON public.admin_match_alerts TO service_role;
GRANT EXECUTE ON FUNCTION public.match_items(vector(512), float, int, text) TO service_role;