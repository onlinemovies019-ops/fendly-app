import asyncio
import io
import os
import re
from pathlib import Path
from typing import cast
from urllib.parse import urlsplit

import httpx
import qrcode
from PIL import Image, ImageDraw, ImageFont, ImageOps, UnidentifiedImageError

from app_links import fendly_report_url
from models import FoundItem, LostItem
from social_content import sanitize_public_title

POSTER_WIDTH = 1080
POSTER_HEIGHT = 1350
INK = (31, 42, 45)
MUTED = (93, 105, 105)
GREEN = (22, 104, 79)
BACKGROUND = (248, 246, 239)
MAX_REPORT_PHOTO_BYTES = 10 * 1024 * 1024

ANIMAL_NAMES = (
    "guinea pig", "goldfish", "cockatiel", "lovebird", "parakeet", "hamster",
    "tortoise", "turtle", "rabbit", "kitten", "puppy", "parrot", "pigeon",
    "sparrow", "chicken", "duck", "goose", "horse", "pony", "cow", "calf",
    "buffalo", "goat", "sheep", "lamb", "piglet", "pig", "donkey", "snake",
    "lizard", "gecko", "iguana", "budgie", "peacock", "owl", "eagle", "crow",
    "bird", "fish", "cat", "dog",
)


def _font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = (
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold
        else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/Library/Fonts/Arial Bold.ttf" if bold else "/Library/Fonts/Arial.ttf",
    )
    for candidate in candidates:
        if Path(candidate).is_file():
            return ImageFont.truetype(candidate, size)
    return ImageFont.load_default()


def _home_subject(title: str, category: str) -> str:
    normalized_title = title.casefold()
    matches: list[tuple[int, int, str]] = []
    for species in ANIMAL_NAMES:
        pattern = re.compile(r"(?<![a-z])" + re.escape(species) + r"(?![a-z])")
        match = pattern.search(normalized_title)
        if match:
            matches.append((match.start(), -len(match.group()), species))
    if matches:
        return min(matches)[2].capitalize()
    if category.strip().casefold() in {"animal", "animals", "pet", "pets"}:
        return "Animal"
    return "Item"


def _draw_wrapped(draw: ImageDraw.ImageDraw, value: str, xy: tuple[int, int],
                  max_width: int,
                  font: ImageFont.FreeTypeFont | ImageFont.ImageFont,
                  max_lines: int = 2) -> None:
    x, y = xy
    lines: list[str] = []
    current = ""
    for word in value.split():
        candidate = f"{current} {word}".strip()
        if current and draw.textbbox((0, 0), candidate, font=font)[2] > max_width:
            lines.append(current)
            current = word
            if len(lines) >= max_lines:
                break
        else:
            current = candidate
    if current and len(lines) < max_lines:
        lines.append(current)
    for index, line in enumerate(lines):
        draw.text((x, y + index * 68), line, fill=INK, font=font)


def _rounded_photo(photo: Image.Image, size: tuple[int, int]) -> Image.Image:
    image = ImageOps.fit(photo.convert("RGB"), size, method=Image.Resampling.LANCZOS)
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=28, fill=255)
    image.putalpha(mask)
    return image


def render_community_poster(
    title: str, report_type: str, category: str,
    report_url: str, photo: Image.Image | None,
) -> bytes:
    poster = Image.new("RGB", (POSTER_WIDTH, POSTER_HEIGHT), BACKGROUND)
    draw = ImageDraw.Draw(poster)
    draw.rounded_rectangle((52, 48, POSTER_WIDTH - 52, 190), radius=34, fill=GREEN)
    draw.text((90, 66), "FENDLY  ·  COMMUNITY ALERT", fill="white", font=_font(48, bold=True))
    draw.text((90, 128), f"Help bring this {_home_subject(title, category)} home",
              fill="white", font=_font(28))

    _draw_wrapped(draw, title.strip() or "Lost item", (76, 220), POSTER_WIDTH - 152,
                  _font(56, bold=True))
    image_bounds = (76, 330, POSTER_WIDTH - 76, 820)
    draw.rounded_rectangle(image_bounds, radius=28, fill=(231, 229, 218))
    if photo is None:
        draw.text((112, 552), "Photo not available", fill=MUTED, font=_font(30))
    else:
        fitted_photo = _rounded_photo(
            photo,
            (image_bounds[2] - image_bounds[0], image_bounds[3] - image_bounds[1]),
        )
        poster.paste(fitted_photo, image_bounds[:2], fitted_photo.getchannel("A"))

    draw.text((78, 862), f"{report_type.title()} report · View details in Fendly",
              fill=INK, font=_font(34, bold=True))
    qr = cast(Image.Image, qrcode.make(report_url)).convert("RGB").resize(
        (250, 250), Image.Resampling.NEAREST
    )
    poster.paste(qr, (744, 980))
    draw.text((775, 1240), "SCAN FOR FENDLY", fill=MUTED, font=_font(22))
    output = io.BytesIO()
    poster.save(output, format="JPEG", quality=90, optimize=True)
    return output.getvalue()


def _allowed_photo_hosts() -> set[str]:
    hosts = {"fendly-api.onrender.com"}
    for variable in ("PUBLIC_BASE_URL", "SUPABASE_URL"):
        value = os.getenv(variable, "").strip()
        if value:
            host = urlsplit(value).hostname
            if host:
                hosts.add(host.lower())
    return hosts


async def _load_report_photo(image_url: str) -> Image.Image:
    parsed = urlsplit(image_url)
    if parsed.scheme != "https" or not parsed.hostname or parsed.hostname.lower() not in _allowed_photo_hosts():
        raise RuntimeError("Report photo host is not an approved Fendly image host")
    image_bytes = bytearray()
    async with httpx.AsyncClient(timeout=15, follow_redirects=False) as client:
        async with client.stream("GET", image_url) as response:
            if response.is_error:
                raise RuntimeError(f"Report photo could not be downloaded (HTTP {response.status_code})")
            content_length = response.headers.get("content-length")
            if content_length and int(content_length) > MAX_REPORT_PHOTO_BYTES:
                raise RuntimeError("Report photo exceeds the 10 MB poster limit")
            async for chunk in response.aiter_bytes():
                image_bytes.extend(chunk)
                if len(image_bytes) > MAX_REPORT_PHOTO_BYTES:
                    raise RuntimeError("Report photo exceeds the 10 MB poster limit")
    try:
        image = Image.open(io.BytesIO(image_bytes))
        return image.convert("RGB")
    except (UnidentifiedImageError, OSError) as error:
        raise RuntimeError("Report photo is not a readable image") from error


async def render_report_poster(report: LostItem | FoundItem, report_type: str) -> bytes:
    title = sanitize_public_title(report.title) or "Reported item"
    report_url = fendly_report_url(report.id)
    photo = await _load_report_photo(report.image_url) if report.image_url else None
    return await asyncio.to_thread(
        render_community_poster, title, report_type, report.category or "", report_url, photo,
    )
