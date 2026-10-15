package com.example.fendly;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

final class FendlyMotion {
    private static final long ENTRANCE_DURATION_MS = 220L;

    private FendlyMotion() {}

    static boolean animationsEnabled() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled();
    }

    static void animateEntrance(View view, long delayMs, float distancePx) {
        if (view == null) return;
        view.animate().cancel();
        if (!animationsEnabled()) {
            view.setTranslationY(0f);
            return;
        }
        view.setTranslationY(distancePx);
        view.animate()
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(ENTRANCE_DURATION_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    static void animateScreenContent(ViewGroup container, float distancePx) {
        if (container == null) return;
        animateEntrance(container, 0L, distancePx);
    }

    static void animateLogo(ImageView logo, float liftPx) {
        if (logo == null || !animationsEnabled()) return;
        logo.setAlpha(1f);
        LogoAnimation animation = new LogoAnimation(logo, liftPx);
        logo.addOnAttachStateChangeListener(animation);
        if (logo.isAttachedToWindow()) animation.start();
    }

    private static final class LogoAnimation implements View.OnAttachStateChangeListener {
        private final ImageView logo;
        private final AnimatorSet animatorSet;

        LogoAnimation(ImageView logo, float liftPx) {
            this.logo = logo;
            ObjectAnimator floating = ObjectAnimator.ofFloat(
                    logo, View.TRANSLATION_Y, 0f, -liftPx, 0f
            );
            floating.setDuration(2400L);
            floating.setRepeatCount(ValueAnimator.INFINITE);

            ObjectAnimator pulseX = ObjectAnimator.ofFloat(
                    logo, View.SCALE_X, 1f, 1.02f, 1f
            );
            pulseX.setDuration(2400L);
            pulseX.setRepeatCount(ValueAnimator.INFINITE);

            ObjectAnimator pulseY = ObjectAnimator.ofFloat(
                    logo, View.SCALE_Y, 1f, 1.02f, 1f
            );
            pulseY.setDuration(2400L);
            pulseY.setRepeatCount(ValueAnimator.INFINITE);

            animatorSet = new AnimatorSet();
            animatorSet.playTogether(floating, pulseX, pulseY);
        }

        void start() {
            if (animationsEnabled() && !animatorSet.isStarted()) animatorSet.start();
        }

        @Override
        public void onViewAttachedToWindow(View view) {
            start();
        }

        @Override
        public void onViewDetachedFromWindow(View view) {
            animatorSet.cancel();
            logo.setTranslationY(0f);
            logo.setScaleX(1f);
            logo.setScaleY(1f);
        }
    }
}
