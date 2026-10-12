package com.example.fendly;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class PhotoCropActivity extends Activity {
    public static final String EXTRA_BITMAP = "com.example.fendly.extra.CROP_BITMAP";
    public static final String EXTRA_IMAGE_URI = "com.example.fendly.extra.CROP_IMAGE_URI";
    public static final String EXTRA_CROPPED_IMAGE_URI = "com.example.fendly.extra.CROPPED_IMAGE_URI";

    private CropImageView cropImageView;
    private boolean darkMode;
    private int backgroundColor;
    private int surfaceColor;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int accentColor;
    private int onAccentColor;

    public static Bitmap getBitmapExtra(android.content.Intent intent, String key) {
        if (intent == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(key, Bitmap.class);
        }
        return intent.getParcelableExtra(key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        setTheme(darkMode ? R.style.Theme_Fendly_Dark : R.style.Theme_Fendly);
        super.onCreate(savedInstanceState);
        backgroundColor = darkMode ? Color.rgb(18, 19, 25) : Color.rgb(247, 243, 238);
        surfaceColor = darkMode ? Color.rgb(24, 29, 36) : Color.WHITE;
        primaryTextColor = darkMode ? Color.rgb(247, 249, 252) : Color.rgb(35, 37, 43);
        secondaryTextColor = darkMode ? Color.rgb(190, 195, 204) : Color.rgb(83, 86, 94);
        accentColor = Color.rgb(232, 178, 74);
        onAccentColor = Color.rgb(43, 29, 5);

        Bitmap bitmap = loadSourceBitmap();
        if (bitmap == null || bitmap.isRecycled()) {
            Toast.makeText(
                    this,
                    LanguageManager.adoptionText(this, "adopt_camera_unavailable"),
                    Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        getWindow().setStatusBarColor(backgroundColor);
        getWindow().setNavigationBarColor(backgroundColor);
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(!darkMode);
        insetsController.setAppearanceLightNavigationBars(!darkMode);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(backgroundColor);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView title = new TextView(this);
        title.setText(LanguageManager.adoptionText(this, "crop_photo"));
        title.setTextColor(primaryTextColor);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));

        TextView instructions = new TextView(this);
        instructions.setText(LanguageManager.adoptionText(this, "crop_photo_instructions"));
        instructions.setTextColor(secondaryTextColor);
        instructions.setTextSize(14);
        instructions.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams instructionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        instructionParams.bottomMargin = dp(8);
        root.addView(instructions, instructionParams);

        FrameLayout preview = new FrameLayout(this);
        cropImageView = new CropImageView(bitmap);
        preview.addView(cropImageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(preview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        actionsParams.topMargin = dp(12);
        root.addView(actions, actionsParams);

        Button cancel = new Button(this);
        ButtonPressAnimator.attach(cancel);
        cancel.setText(LanguageManager.adoptionText(this, "adopt_cancel"));
        styleButton(cancel, surfaceColor, primaryTextColor);
        cancel.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, -1, 1f));

        Button confirm = new Button(this);
        ButtonPressAnimator.attach(confirm);
        confirm.setText(LanguageManager.adoptionText(this, "crop_photo_confirm"));
        styleButton(confirm, accentColor, onAccentColor);
        confirm.setOnClickListener(view -> confirmCrop());
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(0, -1, 1f);
        confirmParams.leftMargin = dp(12);
        actions.addView(confirm, confirmParams);

        setContentView(root);
        WindowInsetsHelper.applySafeArea(root);
    }

    private Bitmap loadSourceBitmap() {
        Bitmap bitmap = getBitmapExtra(getIntent(), EXTRA_BITMAP);
        if (bitmap != null) return bitmap;
        String uriValue = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (uriValue == null || uriValue.isBlank()) return null;
        Uri uri = Uri.parse(uriValue);
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
            int sampleSize = 1;
            while (Math.max(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) > 3072) {
                sampleSize *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap decoded;
            try (InputStream imageInput = getContentResolver().openInputStream(uri)) {
                if (imageInput == null) return null;
                decoded = BitmapFactory.decodeStream(imageInput, null, options);
            }
            return applyExifOrientation(uri, decoded);
        } catch (Exception error) {
            return null;
        }
    }

    private Bitmap applyExifOrientation(Uri uri, Bitmap bitmap) {
        if (bitmap == null) return null;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) return bitmap;
            int orientation = new ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                default:
                    return bitmap;
            }
            Bitmap oriented = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (oriented != bitmap) bitmap.recycle();
            return oriented;
        } catch (Exception error) {
            return bitmap;
        }
    }

    private void confirmCrop() {
        Bitmap cropped = cropImageView.createCroppedBitmap();
        if (cropped == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        File directory = new File(getFilesDir(), "image_provider");
        if (!directory.exists() && !directory.mkdirs()) {
            cropped.recycle();
            showCropFailure();
            return;
        }
        File output = new File(directory, "cropped_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream stream = new FileOutputStream(output)) {
            if (!cropped.compress(Bitmap.CompressFormat.JPEG, 92, stream)) {
                throw new IllegalStateException("Could not encode cropped photo");
            }
            stream.flush();
            Uri imageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", output);
            setResult(RESULT_OK, new android.content.Intent()
                    .putExtra(EXTRA_CROPPED_IMAGE_URI, imageUri.toString()));
            finish();
        } catch (IOException | IllegalStateException error) {
            showCropFailure();
            setResult(RESULT_CANCELED);
            finish();
        } finally {
            cropped.recycle();
        }
    }

    private void showCropFailure() {
        Toast.makeText(
                this,
                LanguageManager.adoptionText(this, "adopt_camera_unavailable"),
                Toast.LENGTH_LONG).show();
    }

    private void styleButton(Button button, int fillColor, int textColor) {
        button.setAllCaps(false);
        button.setTextColor(textColor);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        button.setBackgroundColor(fillColor);
        button.setElevation(dp(2));
        button.setPadding(dp(16), dp(8), dp(16), dp(8));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class CropImageView extends View {
        private static final int MOVE = 0;
        private static final int RESIZE_TOP_LEFT = 1;
        private static final int RESIZE_TOP_RIGHT = 2;
        private static final int RESIZE_BOTTOM_LEFT = 3;
        private static final int RESIZE_BOTTOM_RIGHT = 4;

        private final Bitmap bitmap;
        private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF imageBounds = new RectF();
        private final RectF cropBounds = new RectF();
        private float lastX;
        private float lastY;
        private int gesture = MOVE;
        private boolean initialized;

        CropImageView(Bitmap bitmap) {
            super(PhotoCropActivity.this);
            this.bitmap = bitmap;
            shadePaint.setColor(0x99000000);
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(dp(2));
            handlePaint.setColor(Color.rgb(232, 178, 74));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (bitmap.isRecycled() || getWidth() == 0 || getHeight() == 0) return;
            float scale = Math.min(getWidth() / (float) bitmap.getWidth(),
                    getHeight() / (float) bitmap.getHeight());
            float width = bitmap.getWidth() * scale;
            float height = bitmap.getHeight() * scale;
            float left = (getWidth() - width) / 2f;
            float top = (getHeight() - height) / 2f;
            imageBounds.set(left, top, left + width, top + height);
            if (!initialized) {
                float insetX = Math.min(dp(12), width * 0.025f);
                float insetY = Math.min(dp(12), height * 0.025f);
                cropBounds.set(imageBounds);
                cropBounds.inset(insetX, insetY);
                initialized = true;
            } else {
                cropBounds.intersect(imageBounds);
            }
            canvas.drawBitmap(bitmap, null, imageBounds, imagePaint);

            Path outside = new Path();
            outside.addRect(imageBounds, Path.Direction.CW);
            Path selected = new Path();
            selected.addRect(cropBounds, Path.Direction.CW);
            outside.op(selected, Path.Op.DIFFERENCE);
            canvas.drawPath(outside, shadePaint);
            canvas.drawRect(cropBounds, borderPaint);
            float handleRadius = dp(7);
            canvas.drawCircle(cropBounds.left, cropBounds.top, handleRadius, handlePaint);
            canvas.drawCircle(cropBounds.right, cropBounds.top, handleRadius, handlePaint);
            canvas.drawCircle(cropBounds.left, cropBounds.bottom, handleRadius, handlePaint);
            canvas.drawCircle(cropBounds.right, cropBounds.bottom, handleRadius, handlePaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!initialized || bitmap.isRecycled()) return false;
            float x = event.getX();
            float y = event.getY();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    gesture = chooseGesture(x, y);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = x - lastX;
                    float dy = y - lastY;
                    updateCrop(dx, dy);
                    lastX = x;
                    lastY = y;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    performClick();
                    return true;
                default:
                    return true;
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        Bitmap createCroppedBitmap() {
            if (!initialized || cropBounds.width() < dp(32) || cropBounds.height() < dp(32)) {
                return null;
            }
            float scaleX = bitmap.getWidth() / imageBounds.width();
            float scaleY = bitmap.getHeight() / imageBounds.height();
            int left = Math.max(0, Math.round((cropBounds.left - imageBounds.left) * scaleX));
            int top = Math.max(0, Math.round((cropBounds.top - imageBounds.top) * scaleY));
            int right = Math.min(bitmap.getWidth(),
                    Math.round((cropBounds.right - imageBounds.left) * scaleX));
            int bottom = Math.min(bitmap.getHeight(),
                    Math.round((cropBounds.bottom - imageBounds.top) * scaleY));
            if (right <= left || bottom <= top) return null;
            try {
                return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
            } catch (IllegalArgumentException error) {
                return null;
            }
        }

        private int chooseGesture(float x, float y) {
            float threshold = dp(36);
            if (near(x, y, cropBounds.left, cropBounds.top, threshold)) return RESIZE_TOP_LEFT;
            if (near(x, y, cropBounds.right, cropBounds.top, threshold)) return RESIZE_TOP_RIGHT;
            if (near(x, y, cropBounds.left, cropBounds.bottom, threshold)) return RESIZE_BOTTOM_LEFT;
            if (near(x, y, cropBounds.right, cropBounds.bottom, threshold)) return RESIZE_BOTTOM_RIGHT;
            if (!cropBounds.contains(x, y)) return MOVE;
            return MOVE;
        }

        private void updateCrop(float dx, float dy) {
            float minSize = dp(64);
            switch (gesture) {
                case RESIZE_TOP_LEFT:
                    cropBounds.left = clamp(cropBounds.left + dx,
                            imageBounds.left, cropBounds.right - minSize);
                    cropBounds.top = clamp(cropBounds.top + dy,
                            imageBounds.top, cropBounds.bottom - minSize);
                    break;
                case RESIZE_TOP_RIGHT:
                    cropBounds.right = clamp(cropBounds.right + dx,
                            cropBounds.left + minSize, imageBounds.right);
                    cropBounds.top = clamp(cropBounds.top + dy,
                            imageBounds.top, cropBounds.bottom - minSize);
                    break;
                case RESIZE_BOTTOM_LEFT:
                    cropBounds.left = clamp(cropBounds.left + dx,
                            imageBounds.left, cropBounds.right - minSize);
                    cropBounds.bottom = clamp(cropBounds.bottom + dy,
                            cropBounds.top + minSize, imageBounds.bottom);
                    break;
                case RESIZE_BOTTOM_RIGHT:
                    cropBounds.right = clamp(cropBounds.right + dx,
                            cropBounds.left + minSize, imageBounds.right);
                    cropBounds.bottom = clamp(cropBounds.bottom + dy,
                            cropBounds.top + minSize, imageBounds.bottom);
                    break;
                default:
                    float moveX = clamp(dx, imageBounds.left - cropBounds.left,
                            imageBounds.right - cropBounds.right);
                    float moveY = clamp(dy, imageBounds.top - cropBounds.top,
                            imageBounds.bottom - cropBounds.bottom);
                    cropBounds.offset(moveX, moveY);
            }
        }

        private boolean near(float x, float y, float targetX, float targetY, float threshold) {
            return Math.abs(x - targetX) <= threshold && Math.abs(y - targetY) <= threshold;
        }

        private float clamp(float value, float minimum, float maximum) {
            return Math.max(minimum, Math.min(value, maximum));
        }

        private float dp(float value) {
            return value * getResources().getDisplayMetrics().density;
        }
    }
}
