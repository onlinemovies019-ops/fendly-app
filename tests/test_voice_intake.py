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
                        "report_type": "lost",
                        "color": "blue",
                        "location_hint": "Central Park",
                        "city_tag": "",
                        "report_date": "14/10/2026",
                        "identifier_details": "Northstar X7",
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
        data={"language": "en", "current_date": "2026-10-15"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "transcription": "I lost a blue backpack near Central Park.",
        "title": "Backpack",
        "category": "item",
        "report_type": "lost",
        "color": "blue",
        "location_hint": "Central Park",
        "city_tag": "",
        "report_date": "14/10/2026",
        "identifier_details": "Northstar X7",
        "additional_notes": "",
    }
    assert FakeAsyncClient.calls[0][0].endswith("/audio/transcriptions")
    assert FakeAsyncClient.calls[0][1]["data"]["model"] == "whisper-1"
    assert FakeAsyncClient.calls[0][1]["files"]["file"][2] == content_type
    assert FakeAsyncClient.calls[1][1]["json"]["messages"][1]["content"] == json.dumps(
        {
            "transcript": "I lost a blue backpack near Central Park.",
            "target_language": "English",
            "current_date": "2026-10-15",
            "current_report_type": "lost",
        },
        ensure_ascii=False,
    )
    assert "Target output language: English." in FakeAsyncClient.calls[1][1]["json"]["messages"][0]["content"]


@pytest.mark.parametrize("filename", ["report.aac", "report.wav.exe", "report"])
def test_voice_intake_rejects_unsupported_file_types(voice_client, filename):
    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": (filename, b"audio bytes", "audio/wav")},
        data={"language": "en", "current_date": "2026-10-15"},
    )

    assert response.status_code == 415


def test_voice_intake_requires_authentication(monkeypatch):
    monkeypatch.setattr(intake_module, "_transcribe_audio", Mock())
    with TestClient(main.app) as client:
        response = client.post(
            "/api/intake/voice",
            files={"audio": ("report.mp3", b"audio bytes", "audio/mpeg")},
            data={"language": "en", "current_date": "2026-10-15"},
        )

    assert response.status_code == 401


@pytest.mark.parametrize(
    "language,current_date",
    [
        ("fr", "2026-10-15"),
        ("en", "2026-02-30"),
    ],
)
def test_voice_intake_rejects_unsupported_language_or_invalid_current_date(
    voice_client,
    language,
    current_date,
):
    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
        data={"language": language, "current_date": current_date},
    )

    assert response.status_code == 422
    assert FakeAsyncClient.calls == []


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
        data={"language": "en", "current_date": "2026-10-15"},
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
        data={"language": "en", "current_date": "2026-10-15"},
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


@pytest.mark.parametrize(
    "selected_language,expected_language,spoken_transcript,localized_title",
    [
        ("en", "English", "माझी निळी बॅग काल हरवली.", "Blue bag"),
        ("mr", "Marathi", "I lost my blue bag yesterday.", "निळी पिशवी"),
    ],
)
def test_voice_intake_uses_selected_app_language_and_extracts_date_and_brand(
    voice_client,
    selected_language,
    expected_language,
    spoken_transcript,
    localized_title,
):
    FakeAsyncClient.responses = [
        FakeResponse({"text": spoken_transcript}),
        FakeResponse({
            "choices": [{
                "message": {
                    "content": json.dumps({
                        "title": localized_title,
                        "category": "item",
                        "report_type": "lost",
                        "color": "blue" if selected_language == "en" else "नीला",
                        "location_hint": "",
                        "city_tag": "",
                        "report_date": "14/10/2026",
                        "identifier_details": "Northstar X7",
                        "additional_notes": "",
                    }, ensure_ascii=False)
                }
            }]
        }),
    ]

    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
        data={"language": selected_language, "current_date": "2026-10-15"},
    )

    assert response.status_code == 200
    assert response.json()["title"] == localized_title
    assert response.json()["report_type"] == "lost"
    assert response.json()["report_date"] == "14/10/2026"
    assert response.json()["identifier_details"] == "Northstar X7"
    extraction_payload = FakeAsyncClient.calls[1][1]["json"]
    assert f"Target output language: {expected_language}." in extraction_payload["messages"][0]["content"]
    assert json.loads(extraction_payload["messages"][1]["content"])["target_language"] == expected_language


@pytest.mark.parametrize(
    "transcript,current_type,extracted_type,title,category,area,city",
    [
        ("My wallet was lost near Mahal, Burdi in Nagpur.", "lost", "lost", "Wallet", "item", "Mahal, Burdi", "Nagpur"),
        ("I found a dog in Wardha.", "lost", "found", "Dog", "pet", "", "Wardha"),
        ("A missing person was found in Amravati.", "lost", "found", "Missing person", "person", "", "Amravati"),
    ],
)
def test_voice_intake_separates_area_city_and_classifies_report(
    voice_client,
    transcript,
    current_type,
    extracted_type,
    title,
    category,
    area,
    city,
):
    FakeAsyncClient.responses = [
        FakeResponse({"text": transcript}),
        FakeResponse({
            "choices": [{
                "message": {
                    "content": json.dumps({
                        "title": title,
                        "category": category,
                        "report_type": extracted_type,
                        "color": "",
                        "location_hint": area,
                        "city_tag": city,
                        "report_date": "",
                        "identifier_details": "",
                        "additional_notes": "",
                    })
                }
            }]
        }),
    ]

    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
        data={
            "language": "en",
            "current_date": "2026-10-15",
            "current_report_type": current_type,
        },
    )

    assert response.status_code == 200
    assert response.json()["report_type"] == extracted_type
    assert response.json()["category"] == category
    assert response.json()["location_hint"] == area
    assert response.json()["city_tag"] == city
    extraction_request = FakeAsyncClient.calls[1][1]["json"]
    assert "Put neighborhoods, local areas, streets, landmarks, and buildings only in location_hint." in (
        extraction_request["messages"][0]["content"]
    )
    assert json.loads(extraction_request["messages"][1]["content"])["current_report_type"] == current_type


def test_voice_intake_rejects_invalid_current_report_type(voice_client):
    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
        data={
            "language": "en",
            "current_date": "2026-10-15",
            "current_report_type": "adopt",
        },
    )

    assert response.status_code == 422
    assert FakeAsyncClient.calls == []


def test_voice_intake_preserves_selected_type_when_transcript_does_not_classify_it(
    voice_client,
):
    FakeAsyncClient.responses[1] = FakeResponse({
        "choices": [{
            "message": {
                "content": json.dumps({
                    "title": "Wallet",
                    "category": "item",
                    "color": "",
                    "location_hint": "",
                    "city_tag": "",
                    "report_date": "",
                    "identifier_details": "",
                    "additional_notes": "",
                })
            }
        }]
    })

    response = voice_client.post(
        "/api/intake/voice",
        files={"audio": ("report.m4a", b"audio bytes", "audio/mp4")},
        data={
            "language": "en",
            "current_date": "2026-10-15",
            "current_report_type": "found",
        },
    )

    assert response.status_code == 200
    assert response.json()["report_type"] == "found"
