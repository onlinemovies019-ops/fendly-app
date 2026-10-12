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
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                target.animate().cancel();
                target.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .setDuration(PRESS_DURATION_MS)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                target.animate().cancel();
                target.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(RELEASE_DURATION_MS)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
            return false;
        });
    }
}
