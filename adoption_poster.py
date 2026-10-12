import io
from collections.abc import Sequence
from pathlib import Path
from textwrap import wrap

from PIL import Image, ImageDraw, ImageFont, ImageOps, UnidentifiedImageError

from poster_localization import LANGUAGE_CODES, poster_text

POSTER_SIZE = (1080, 1350)
BACKGROUND = (250, 247, 239)
FOREST = (22, 74, 58)
GREEN = (32, 111, 79)
GOLD = (232, 184, 91)
INK = (33, 49, 43)
MUTED = (100, 112, 105)
FONT_DIRECTORY = Path(__file__).resolve().parent / "app/src/main/assets/fonts"
LANGUAGE_FONTS = {
    "hi": "NotoSansDevanagari[wdth,wght].ttf",
    "mr": "NotoSansDevanagari[wdth,wght].ttf",
    "gu": "NotoSansGujarati[wdth,wght].ttf",
    "bn": "NotoSansBengali[wdth,wght].ttf",
    "ta": "NotoSansTamil[wdth,wght].ttf",
    "te": "NotoSansTelugu[wdth,wght].ttf",
    "kn": "NotoSansKannada[wdth,wght].ttf",
    "ml": "NotoSansMalayalam[wdth,wght].ttf",
}


def _font(
    size: int,
    bold: bool = False,
    language_code: str = "en",
) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    normalized_language = (
        language_code.strip().lower().split("-", 1)[0]
        if language_code.strip()
        else "en"
    )
    language_font = FONT_DIRECTORY / LANGUAGE_FONTS.get(normalized_language, "")
    if normalized_language in LANGUAGE_FONTS and language_font.is_file():
        font = ImageFont.truetype(str(language_font), size, layout_engine=ImageFont.Layout.RAQM)
        if bold:
            font.set_variation_by_name("Bold")
        return font
    candidates = (
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold
        else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/Library/Fonts/Arial Bold.ttf" if bold else "/Library/Fonts/Arial.ttf",
    )
    for candidate in candidates:
        if Path(candidate).is_file():
            return ImageFont.truetype(candidate, size)
    return ImageFont.load_default()


def _age_label(age_months: int, language_code: str) -> str:
    years, months = divmod(age_months, 12)
    if years and months:
        return poster_text("age_join", language_code, years=str(years), months=str(months))
    if years:
        return poster_text(
            "age_year" if years == 1 else "age_years",
            language_code,
            count=str(years),
        )
    return poster_text(
        "age_month" if months == 1 else "age_months",
        language_code,
        count=str(months),
    )


def _draw_centered(
    draw: ImageDraw.ImageDraw,
    text: str,
    y: int,
    font: ImageFont.FreeTypeFont | ImageFont.ImageFont,
    fill: tuple[int, int, int],
) -> None:
    bounds = draw.textbbox((0, 0), text, font=font)
    x = (POSTER_SIZE[0] - (bounds[2] - bounds[0])) // 2
    draw.text((x, y), text, font=font, fill=fill)


def _load_photo(data: bytes) -> Image.Image:
    try:
        with Image.open(io.BytesIO(data)) as source:
            if source.width * source.height > 40_000_000:
                raise ValueError("Pet photo dimensions are too large for an adoption poster")
            return ImageOps.exif_transpose(source).convert("RGB")
    except (UnidentifiedImageError, OSError) as exc:
        raise ValueError("Pet photo could not be opened to create the social poster") from exc


def render_adoption_poster(
    photo_data: bytes | Sequence[bytes],
    *,
    pet_name: str,
    species: str,
    breed: str | None,
    age_months: int,
    location_city: str,
    health_status: str,
    download_url: str,
    language_code: str = "en",
) -> bytes:
    photo_data_items = (
        [photo_data] if isinstance(photo_data, bytes) else list(photo_data)
    )[:3]
    if not photo_data_items:
        raise ValueError("At least one pet photo is required for an adoption poster")
    language = (
        language_code.strip().lower().split("-", 1)[0]
        if language_code.strip()
        else "en"
    )
    if language not in LANGUAGE_CODES:
        language = "en"
    def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
        return _font(size, bold, language)
    photos = [_load_photo(data) for data in photo_data_items]
    poster = Image.new("RGB", POSTER_SIZE, BACKGROUND)
    draw = ImageDraw.Draw(poster)

    for y in range(0, 300):
        ratio = y / 300
        color = tuple(
            round(FOREST[channel] * (1 - ratio) + GREEN[channel] * ratio)
            for channel in range(3)
        )
        draw.line((0, y, POSTER_SIZE[0], y), fill=color)
    draw.rectangle((0, 288, POSTER_SIZE[0], 300), fill=GOLD)

    draw.text(
        (74, 44),
        poster_text("adoption_header", language),
        font=font(26, True),
        fill=(255, 255, 255),
    )
    draw.text(
        (74, 100),
        poster_text("adoption_slogan1", language),
        font=font(60, True),
        fill=(255, 255, 255),
    )
    draw.text(
        (74, 171),
        poster_text("adoption_slogan2", language),
        font=font(60, True),
        fill=(255, 255, 255),
    )

    photo_box = (66, 328, 1014, 1008)
    draw.rounded_rectangle(
        (photo_box[0] - 9, photo_box[1] - 9, photo_box[2] + 9, photo_box[3] + 9),
        radius=38,
        fill=(255, 255, 255),
    )
    left, top, right, bottom = photo_box
    gutter = 12
    if len(photos) == 1:
        tiles = [photo_box]
    elif len(photos) == 2:
        tile_width = (right - left - gutter) // 2
        tiles = [
            (left, top, left + tile_width, bottom),
            (left + tile_width + gutter, top, right, bottom),
        ]
    else:
        main_width = (right - left - gutter) * 2 // 3
        secondary_top = top + (bottom - top - gutter) // 2
        tiles = [
            (left, top, left + main_width, bottom),
            (left + main_width + gutter, top, right, secondary_top),
            (left + main_width + gutter, secondary_top + gutter, right, bottom),
        ]

    for photo, tile in zip(photos, tiles):
        tile_size = (tile[2] - tile[0], tile[3] - tile[1])
        fitted = ImageOps.fit(
            photo,
            tile_size,
            method=Image.Resampling.LANCZOS,
            centering=(0.5, 0.45),
        )
        mask = Image.new("L", tile_size, 0)
        ImageDraw.Draw(mask).rounded_rectangle(
            (0, 0, tile_size[0] - 1, tile_size[1] - 1),
            radius=30,
            fill=255,
        )
        poster.paste(fitted, tile[:2], mask)

    badge = (730, 354, 978, 414)
    draw.rounded_rectangle(badge, radius=30, fill=GOLD)
    badge_text = poster_text("adoption_ready", language)
    badge_font = font(19, True)
    badge_bounds = draw.textbbox((0, 0), badge_text, font=badge_font)
    draw.text(
        (
            badge[0] + (badge[2] - badge[0] - badge_bounds[2]) // 2,
            badge[1] + (badge[3] - badge[1] - (badge_bounds[3] - badge_bounds[1])) // 2 - 2,
        ),
        badge_text,
        font=badge_font,
        fill=FOREST,
    )

    draw.rounded_rectangle((48, 936, 1032, 1320), radius=36, fill=(255, 255, 255))
    name = " ".join(pet_name.split())[:34] or poster_text("adoption_fallback_name", language)
    name_font = font(54 if len(name) < 20 else 44, True)
    draw.text((88, 966), name, font=name_font, fill=INK)

    breed_text = " · ".join(part for part in (species.title(), (breed or "").strip()) if part)
    draw.text((90, 1035), breed_text[:60], font=font(25), fill=MUTED)

    details = f"{_age_label(age_months, language)}   •   {location_city.strip()[:36]}"
    draw.text((90, 1090), details, font=font(27, True), fill=GREEN)
    health = " ".join(health_status.split())
    if health:
        health_lines = wrap(health, width=68)[:2]
        draw.multiline_text(
            (90, 1135),
            "\n".join(health_lines),
            font=font(21),
            fill=MUTED,
            spacing=5,
        )

    draw.rounded_rectangle((78, 1228, 1002, 1292), radius=32, fill=FOREST)
    _draw_centered(
        draw,
        poster_text("adoption_meet_friend", language),
        1245,
        font(22, True),
        (255, 255, 255),
    )
    url = download_url.strip()
    url_font = font(15)
    url_bounds = draw.textbbox((0, 0), url, font=url_font)
    if url_bounds[2] < 820:
        _draw_centered(draw, url, 1301, url_font, MUTED)

    output = io.BytesIO()
    poster.save(output, format="JPEG", quality=91, optimize=True)
    return output.getvalue()
