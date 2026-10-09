import os
from pathlib import Path

import httpx


async def store_image(data: bytes, filename: str, content_type: str) -> str:
    supabase_url = os.getenv("SUPABASE_URL", "").rstrip("/")
    service_role_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY")
    bucket = os.getenv("SUPABASE_STORAGE_BUCKET", "uploads")
    if supabase_url and service_role_key:
        endpoint = f"{supabase_url}/storage/v1/object/{bucket}/{filename}"
        headers = {
            "Authorization": f"Bearer {service_role_key}",
            "Content-Type": content_type,
            "x-upsert": "false",
        }
        try:
            async with httpx.AsyncClient(timeout=8) as client:
                response = await client.post(endpoint, content=data, headers=headers)
            if not response.is_error:
                return f"{supabase_url}/storage/v1/object/public/{bucket}/{filename}"
        except httpx.HTTPError:
            pass

    upload_dir = Path(os.getenv("UPLOAD_DIR", "static/uploads"))
    upload_dir.mkdir(parents=True, exist_ok=True)
    (upload_dir / filename).write_bytes(data)
    base_url = os.getenv("PUBLIC_BASE_URL", "https://fendly-api.onrender.com").rstrip("/")
    return f"{base_url}/static/uploads/{filename}"
