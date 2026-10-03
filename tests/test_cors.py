import pytest

import main


def test_cors_origins_are_trimmed_and_normalized(monkeypatch):
    monkeypatch.setenv(
        "CORS_ORIGINS",
        " https://admin.example.test/ ,https://app.example.test ",
    )
    monkeypatch.setenv("ENVIRONMENT", "development")

    assert main._cors_allowed_origins() == [
        "https://admin.example.test",
        "https://app.example.test",
    ]


def test_production_rejects_wildcard_cors_origin(monkeypatch):
    monkeypatch.setenv("CORS_ORIGINS", "*")
    monkeypatch.setenv("ENVIRONMENT", "production")

    with pytest.raises(RuntimeError, match="Wildcard CORS"):
        main._cors_allowed_origins()
