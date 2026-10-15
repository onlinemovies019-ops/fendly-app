import json
from unittest.mock import Mock

import pytest
import httpx
from fastapi.testclient import TestClient

import main
from routers import intake as intake_module


class FakeResponse:
    def __init__(self, payload, status_code=200):
        self.payload = payload
        self.status_code = status_code
        self.text = json.dumps(payload)

    def raise_for_status(self):
        if self.status_code >= 400:
            request = httpx.Request("POST", "https://api.openai.com/v1/audio/transcriptions")
            response = httpx.Response(
                self.status_code,
                request=request,
                json=self.payload,
            )
            raise httpx.HTTPStatusError(
                "Provider request failed",
                request=request,
                response=response,
            )
        return None

    def json(self):
        return self.payload


class FakeAsyncClient:
    responses = []
    calls = []

    def __init__(self, **_kwargs):
        pass

    async def __aenter__(self):
        return self

    async def __aexit__(self, *_args):
        return False

    async def post(self, url, **kwargs):
        self.calls.append((url, kwargs))
        return self.responses.pop(0)


@pytest.fixture
def voice_client(monkeypatch):
    FakeAsyncClient.responses = [
        FakeResponse({"text": "I lost a blue backpack near Central Park."}),
        FakeResponse({
            "choices": [{
                "message": {
                    "content": json.dumps({
                        "title": "Backpack",
                        "category": "item",
                        "color": "blue",
                        "location_hint": "Central Park",
                        "additional_notes": "",
                    })
                }
            }]
        }),
    ]
    FakeAsyncClient.calls = []
    monkeypatch.setenv("OPENAI_API_KEY", "test-openai-key")
    monkeypatch.setattr(intake_module.httpx, "AsyncClient", FakeAsyncClient)
    main.app.dependency_overrides[intake_module.get_current_user] = lambda: "regular-user"
    client = TestClient(main.app)
    yield client
    client.close()
    main.app.dependency_overrides.pop(intake_module.get_current_user, None)


@pytest.mark.parametrize(
    "filename,content_type",
    [
        ("report.wav", "audio/wav"),
        ("report.m4a", "audio/mp4"),
        ("report.mp3", "audio/mpeg"),
    ],
)
def test_voice_intake_transcribes_and_extracts_report_details(
    voice_client,
    filename,
    content_type,
):
    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": (filename, b"audio bytes", content_type)},
    )

    assert response.status_code == 200
    assert response.json() == {
        "transcription": "I lost a blue backpack near Central Park.",
        "title": "Backpack",
        "category": "item",
        "color": "blue",
        "location_hint": "Central Park",
        "additional_notes": "",
    }
    assert FakeAsyncClient.calls[0][0].endswith("/audio/transcriptions")
    assert FakeAsyncClient.calls[0][1]["data"]["model"] == "whisper-1"
    assert FakeAsyncClient.calls[0][1]["files"]["file"][2] == content_type
    assert FakeAsyncClient.calls[1][1]["json"]["messages"][1]["content"] == json.dumps(
        {"transcript": "I lost a blue backpack near Central Park."}
    )


@pytest.mark.parametrize("filename", ["report.aac", "report.wav.exe", "report"])
def test_voice_intake_rejects_unsupported_file_types(voice_client, filename):
    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": (filename, b"audio bytes", "audio/wav")},
    )

    assert response.status_code == 415


def test_voice_intake_requires_authentication(monkeypatch):
    monkeypatch.setattr(intake_module, "_transcribe_audio", Mock())
    with TestClient(main.app) as client:
        response = client.post(
            "/api/intake/voice",
            files={"audio": ("report.mp3", b"audio bytes", "audio/mpeg")},
        )

    assert response.status_code == 401


@pytest.mark.parametrize(
    "provider_status,provider_message,expected_status,expected_detail",
    [
        (
            401,
            "Incorrect API key provided",
            503,
            "OpenAI transcription is not configured correctly. Please check the server API key.",
        ),
        (
            429,
            "Quota exceeded",
            503,
            "OpenAI transcription is temporarily rate-limited or out of quota. Check the provider usage limits and try again later.",
        ),
        (
            400,
            "Audio file is not valid",
            422,
            "The transcription service could not process this recording. Please record again and try.",
        ),
    ],
)
def test_voice_intake_reports_actionable_transcription_provider_errors(
    voice_client,
    provider_status,
    provider_message,
    expected_status,
    expected_detail,
):
    FakeAsyncClient.responses = [
        FakeResponse(
            {"error": {"message": provider_message}},
            status_code=provider_status,
        )
    ]

    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
    )

    assert response.status_code == expected_status
    assert response.json()["detail"] == expected_detail


def test_voice_intake_uses_groq_when_groq_api_key_is_configured(
    voice_client,
    monkeypatch,
):
    monkeypatch.setenv("GROQ_API_KEY", "test-groq-key")
    monkeypatch.delenv("GROQ_VOICE_INTAKE_MODEL", raising=False)

    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
    )

    assert response.status_code == 200
    transcription_call, extraction_call = FakeAsyncClient.calls
    assert transcription_call[0] == (
        "https://api.groq.com/openai/v1/audio/transcriptions"
    )
    assert transcription_call[1]["headers"]["Authorization"] == "Bearer test-groq-key"
    assert transcription_call[1]["data"]["model"] == "whisper-large-v3-turbo"
    assert extraction_call[0] == (
        "https://api.groq.com/openai/v1/chat/completions"
    )
    assert extraction_call[1]["headers"]["Authorization"] == "Bearer test-groq-key"
    assert extraction_call[1]["json"]["model"] == "openai/gpt-oss-120b"
