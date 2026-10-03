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


def test_production_ignores_wildcard_cors_origin(monkeypatch, caplog):
    monkeypatch.setenv("CORS_ORIGINS", "*")
    monkeypatch.setenv("ENVIRONMENT", "production")

    assert main._cors_allowed_origins() == ["https://fendly-api.onrender.com"]
    assert "Ignoring wildcard CORS origin in production" in caplog.text


def test_production_keeps_explicit_origins_and_ignores_wildcard(monkeypatch, caplog):
    monkeypatch.setenv(
        "CORS_ORIGINS",
        "*,https://admin.example.test",
    )
    monkeypatch.setenv("ENVIRONMENT", "production")

    assert main._cors_allowed_origins() == ["https://admin.example.test"]
    assert "Ignoring wildcard CORS origin in production" in caplog.text
