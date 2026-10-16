package com.example.fendly;

import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

final class ButtonPressAnimator {
    private static final long PRESS_DURATION_MS = 85L;
    private static final long RELEASE_DURATION_MS = 150L;
    private static final float PRESSED_SCALE = 0.975f;

    private ButtonPressAnimator() {}

    static void attach(View view) {
        float density = view.getResources().getDisplayMetrics().density;
        if (view.getElevation() == 0f) FendlyMotion.setElevation(view, 4f * density);
        else FendlyMotion.allowShadowOverflow(view);
        float restingElevation = view.getElevation();
        float restingTranslationY = view.getTranslationY();
        view.setOnTouchListener((target, event) -> {
            if (!target.isEnabled()) return false;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    target.setPressed(true);
                    animate(target, PRESSED_SCALE, restingElevation * 0.3f,
                            restingTranslationY + density * 1.5f, PRESS_DURATION_MS);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    boolean inside = event.getX() >= 0
                            && event.getX() < target.getWidth()
                            && event.getY() >= 0
                            && event.getY() < target.getHeight();
                    target.setPressed(inside);
                    return true;
                case MotionEvent.ACTION_UP:
                    boolean performClick = target.isPressed();
                    target.setPressed(false);
                    animate(target, 1f, restingElevation, restingTranslationY, RELEASE_DURATION_MS);
                    if (performClick) target.performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    target.setPressed(false);
                    animate(target, 1f, restingElevation, restingTranslationY, RELEASE_DURATION_MS);
                    return true;
                default:
                    return true;
            }
        });
    }

    private static void animate(View view, float scale, float elevation, float translationY, long durationMs) {
        if (!FendlyMotion.animationsEnabled()) {
            view.setScaleX(scale);
            view.setScaleY(scale);
            view.setElevation(elevation);
            view.setTranslationY(translationY);
            return;
        }
        view.animate().cancel();
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .translationY(translationY)
                .setDuration(durationMs)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        ObjectAnimator.ofFloat(view, "elevation", elevation)
                .setDuration(durationMs)
                .start();
    }
}
