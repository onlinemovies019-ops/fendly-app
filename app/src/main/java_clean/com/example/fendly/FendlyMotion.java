package com.example.fendly;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
        if (logo == null) return;
        logo.setAlpha(1f);
        setElevation(logo, liftPx * 1.5f);
        logo.setTranslationZ(liftPx * 0.5f);
        logo.setCameraDistance(8000f * logo.getResources().getDisplayMetrics().density);
        if (!animationsEnabled()) return;
        LogoAnimation animation = new LogoAnimation(logo, liftPx);
        logo.addOnAttachStateChangeListener(animation);
        if (logo.isAttachedToWindow()) animation.start();
    }

    static void setElevation(View view, float elevationPx) {
        if (view == null) return;
        view.setElevation(elevationPx);
        allowShadowOverflow(view);
    }

    static void allowShadowOverflow(View view) {
        if (view == null) return;
        if (view.isAttachedToWindow()) {
            disableAncestorClipping(view);
            return;
        }
        View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View attachedView) {
                disableAncestorClipping(attachedView);
                attachedView.removeOnAttachStateChangeListener(this);
            }

            @Override
            public void onViewDetachedFromWindow(View detachedView) { }
        };
        view.addOnAttachStateChangeListener(listener);
    }

    private static void disableAncestorClipping(View view) {
        ViewParent parent = view.getParent();
        while (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            group.setClipChildren(false);
            group.setClipToPadding(false);
            parent = group.getParent();
        }
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

            ObjectAnimator tiltX = ObjectAnimator.ofFloat(
                    logo, View.ROTATION_X, 0f, 2f, 0f, -2f, 0f
            );
            tiltX.setDuration(4800L);
            tiltX.setRepeatCount(ValueAnimator.INFINITE);

            ObjectAnimator tiltY = ObjectAnimator.ofFloat(
                    logo, View.ROTATION_Y, 0f, -3f, 0f, 3f, 0f
            );
            tiltY.setDuration(4800L);
            tiltY.setRepeatCount(ValueAnimator.INFINITE);

            animatorSet = new AnimatorSet();
            animatorSet.playTogether(floating, tiltX, tiltY);
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
            logo.setRotationX(0f);
            logo.setRotationY(0f);
        }
    }
}
