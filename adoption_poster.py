import io
from collections.abc import Sequence
from pathlib import Path
from textwrap import wrap

from PIL import Image, ImageDraw, ImageFont, ImageOps, UnidentifiedImageError


POSTER_SIZE = (1080, 1350)
BACKGROUND = (250, 247, 239)
FOREST = (22, 74, 58)
GREEN = (32, 111, 79)
GOLD = (232, 184, 91)
INK = (33, 49, 43)
MUTED = (100, 112, 105)


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


def _age_label(age_months: int) -> str:
    years, months = divmod(age_months, 12)
    if years and months:
        return f"{years} yr {months} mo"
    if years:
        return f"{years} {'year' if years == 1 else 'years'}"
    return f"{months} {'month' if months == 1 else 'months'}"


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
) -> bytes:
    photo_data_items = (
        [photo_data] if isinstance(photo_data, bytes) else list(photo_data)
    )[:3]
    if not photo_data_items:
        raise ValueError("At least one pet photo is required for an adoption poster")
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

    draw.text((74, 44), "FENDLY  /  PET ADOPTION", font=_font(26, True), fill=(255, 255, 255))
    draw.text((74, 100), "A loving home", font=_font(60, True), fill=(255, 255, 255))
    draw.text((74, 171), "is waiting for you", font=_font(60, True), fill=(255, 255, 255))

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
    badge_text = "READY FOR A HOME"
    badge_font = _font(19, True)
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
    name = " ".join(pet_name.split())[:34] or "Meet your new best friend"
    name_font = _font(54 if len(name) < 20 else 44, True)
    draw.text((88, 966), name, font=name_font, fill=INK)

    breed_text = " · ".join(part for part in (species.title(), (breed or "").strip()) if part)
    draw.text((90, 1035), breed_text[:60], font=_font(25), fill=MUTED)

    details = f"{_age_label(age_months)}   •   {location_city.strip()[:36]}"
    draw.text((90, 1090), details, font=_font(27, True), fill=GREEN)
    health = " ".join(health_status.split())
    if health:
        health_lines = wrap(health, width=68)[:2]
        draw.multiline_text(
            (90, 1135),
            "\n".join(health_lines),
            font=_font(21),
            fill=MUTED,
            spacing=5,
        )

    draw.rounded_rectangle((78, 1228, 1002, 1292), radius=32, fill=FOREST)
    _draw_centered(
        draw,
        "MEET YOUR NEW BEST FRIEND  ·  FENDLY",
        1245,
        _font(22, True),
        (255, 255, 255),
    )
    url = download_url.strip()
    url_font = _font(15)
    url_bounds = draw.textbbox((0, 0), url, font=url_font)
    if url_bounds[2] < 820:
        _draw_centered(draw, url, 1301, url_font, MUTED)

    output = io.BytesIO()
    poster.save(output, format="JPEG", quality=91, optimize=True)
    return output.getvalue()
