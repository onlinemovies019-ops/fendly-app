BEGIN;

REVOKE CREATE ON SCHEMA public FROM PUBLIC, anon, authenticated;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    REVOKE ALL ON TABLES FROM PUBLIC, anon, authenticated;

ALTER TABLE IF EXISTS public.admin_match_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.device_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.found_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.items ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.lost_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.username_reservations ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.users ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES ON TABLE
    public.admin_match_alerts,
    public.device_tokens,
    public.found_items,
    public.items,
    public.lost_items,
    public.username_reservations,
    public.users
FROM PUBLIC, anon, authenticated;

GRANT SELECT, INSERT, UPDATE ON TABLE public.items TO service_role;
GRANT SELECT, INSERT, UPDATE ON TABLE public.admin_match_alerts TO service_role;

ALTER FUNCTION public.match_items(public.vector, double precision, integer, text)
    SET search_path = pg_catalog, public;

REVOKE ALL PRIVILEGES ON FUNCTION
    public.match_items(public.vector, double precision, integer, text)
FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION
    public.match_items(public.vector, double precision, integer, text)
TO service_role;

COMMIT;