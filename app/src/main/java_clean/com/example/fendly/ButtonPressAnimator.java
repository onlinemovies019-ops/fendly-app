package com.example.fendly;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

final class ButtonPressAnimator {
    private static final long PRESS_DURATION_MS = 85L;
    private static final long RELEASE_DURATION_MS = 150L;

    private ButtonPressAnimator() {}

    static void attach(View view) {
        view.setOnTouchListener((target, event) -> {
            if (!target.isEnabled()) return false;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    target.setPressed(true);
                    animate(target, 0.98f, PRESS_DURATION_MS);
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
                    animate(target, 1f, RELEASE_DURATION_MS);
                    if (performClick) target.performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    target.setPressed(false);
                    animate(target, 1f, RELEASE_DURATION_MS);
                    return true;
                default:
                    return true;
            }
        });
    }

    private static void animate(View view, float scale, long durationMs) {
        if (!FendlyMotion.animationsEnabled()) return;
        view.animate().cancel();
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(durationMs)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
