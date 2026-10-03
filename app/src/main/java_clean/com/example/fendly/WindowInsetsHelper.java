package com.example.fendly;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

final class WindowInsetsHelper {
    private WindowInsetsHelper() {}

    static void applySafeArea(View view) {
        int initialLeft = view.getPaddingLeft();
        int initialTop = view.getPaddingTop();
        int initialRight = view.getPaddingRight();
        int initialBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (target, windowInsets) -> {
            Insets safeInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            target.setPadding(
                    initialLeft + safeInsets.left,
                    initialTop + safeInsets.top,
                    initialRight + safeInsets.right,
                    initialBottom + Math.max(safeInsets.bottom, imeInsets.bottom));
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    static int windowWidthDp(Activity activity) {
        return windowDimensionDp(activity, true);
    }

    static int windowHeightDp(Activity activity) {
        return windowDimensionDp(activity, false);
    }

    private static int windowDimensionDp(Activity activity, boolean width) {
        float density = activity.getResources().getDisplayMetrics().density;
        int pixels;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = activity.getWindowManager().getCurrentWindowMetrics().getBounds();
            pixels = width ? bounds.width() : bounds.height();
        } else {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            pixels = width ? metrics.widthPixels : metrics.heightPixels;
        }
        return Math.round(pixels / density);
    }
}
