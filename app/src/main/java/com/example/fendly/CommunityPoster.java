package com.example.fendly;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

final class CommunityPoster {
    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1350;
    private static final int INK = Color.rgb(31, 42, 45);
    private static final int MUTED = Color.rgb(93, 105, 105);
    private static final int GREEN = Color.rgb(22, 104, 79);

    private CommunityPoster() {}

    static Bitmap render(
            String title,
            String location,
            String date,
            String itemType,
            String category,
            String itemUrl,
            Bitmap photo
    ) throws Exception {
        Bitmap poster = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(poster);
        canvas.drawColor(Color.rgb(248, 246, 239));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setColor(GREEN);
        canvas.drawRoundRect(new RectF(52, 48, WIDTH - 52, 190), 34, 34, paint);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        paint.setTextSize(48);
        canvas.drawText("FENDLY  ·  COMMUNITY ALERT", 90, 112, paint);
        paint.setTextSize(28);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        String subject = CommunityPosterSubject.homeSubject(title, category);
        canvas.drawText(
                "Help bring this " + ("Item".equals(subject) ? "item" : subject) + " home",
                90,
                158,
                paint
        );

        paint.setColor(INK);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextSize(56);
        drawWrapped(canvas, title == null || title.trim().isEmpty() ? "Lost item" : title, 76, 260, WIDTH - 152, 68, 2, paint);

        RectF imageBounds = new RectF(130, 330, 950, 1150);
        paint.setColor(Color.rgb(231, 229, 218));
        canvas.drawRoundRect(imageBounds, 28, 28, paint);
        if (photo != null && !photo.isRecycled()) {
            canvas.save();
            android.graphics.Path clip = new android.graphics.Path();
            clip.addRoundRect(imageBounds, 28, 28, android.graphics.Path.Direction.CW);
            canvas.clipPath(clip);
            canvas.drawBitmap(photo, null, fitRect(photo, imageBounds), paint);
            canvas.restore();
        } else {
            paint.setColor(MUTED);
            paint.setTextSize(30);
            paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            canvas.drawText("Photo not available", imageBounds.left + 36, imageBounds.centerY(), paint);
        }

        Bitmap qr = createQr(itemUrl);
        canvas.drawBitmap(qr, null, new RectF(842, 1170, 992, 1320), null);
        qr.recycle();
        paint.setColor(MUTED);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SCAN TO VIEW", 917, 1342, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(INK);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        String reportLabel = CommunityPosterSubject.reportType(itemType, title, category);
        drawWrapped(canvas, reportLabel, 78, 1200, 720, 42, 2, paint);
        paint.setColor(MUTED);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextSize(24);
        canvas.drawText(date == null || date.trim().isEmpty() ? "Date not provided" : date, 78, 1315, paint);
        return poster;
    }

    private static RectF fitRect(Bitmap bitmap, RectF bounds) {
        float scale = Math.min(
                bounds.width() / bitmap.getWidth(),
                bounds.height() / bitmap.getHeight()
        );
        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        float left = bounds.centerX() - width / 2f;
        float top = bounds.centerY() - height / 2f;
        return new RectF(left, top, left + width, top + height);
    }

    private static Bitmap createQr(String value) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = new MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, 360, 360, hints);
        Bitmap bitmap = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private static void drawWrapped(
            Canvas canvas,
            String value,
            float x,
            float y,
            float maxWidth,
            float lineHeight,
            int maxLines,
            Paint paint
    ) {
        String[] words = value.trim().split("\\s+");
        String line = "";
        int lineCount = 0;
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (paint.measureText(candidate) > maxWidth && !line.isEmpty()) {
                canvas.drawText(line, x, y + lineCount * lineHeight, paint);
                line = word;
                if (++lineCount >= maxLines) break;
            } else {
                line = candidate;
            }
        }
        if (lineCount < maxLines && !line.isEmpty()) canvas.drawText(line, x, y + lineCount * lineHeight, paint);
    }
}
