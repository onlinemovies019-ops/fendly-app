import pytest
from fastapi import HTTPException

from routers.vault import _storage_backend


def test_vault_storage_requires_supabase_even_when_local_path_is_configured(monkeypatch):
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)
    monkeypatch.setenv("VAULT_LOCAL_STORAGE_DIR", "/tmp/fendly-vault")

    with pytest.raises(HTTPException) as error:
        _storage_backend()

    assert error.value.status_code == 503


def test_vault_storage_uses_private_supabase_bucket(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "service-key")
    monkeypatch.setenv("VAULT_STORAGE_BUCKET", "private-vault")

    assert _storage_backend() == (
        "https://project.supabase.co",
        "service-key",
        "private-vault",
    )
