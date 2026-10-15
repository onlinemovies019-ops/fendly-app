package com.example.fendly;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class FendlyLoadingView extends LinearLayout {
    private final ObjectAnimator rotation;
    private final ObjectAnimator pulseX;
    private final ObjectAnimator pulseY;

    FendlyLoadingView(
            Context context,
            String message,
            int textColor,
            int accentColor,
            boolean compact
    ) {
        super(context);
        setOrientation(compact ? HORIZONTAL : VERTICAL);
        setGravity(compact ? Gravity.CENTER_VERTICAL : Gravity.CENTER);
        int indicatorSize = dp(context, compact ? 30 : 72);
        int logoSize = dp(context, compact ? 18 : 44);

        FrameLayout indicator = new FrameLayout(context);
        RingView ring = new RingView(context, accentColor);
        indicator.addView(ring, new FrameLayout.LayoutParams(-1, -1));
        ImageView logo = new ImageView(context);
        logo.setImageResource(R.drawable.fendly_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setContentDescription("Fendly");
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(
                logoSize, logoSize, Gravity.CENTER
        );
        indicator.addView(logo, logoParams);
        addView(indicator, new LayoutParams(indicatorSize, indicatorSize));

        TextView label = new TextView(context);
        label.setText(message);
        label.setTextColor(textColor);
        label.setTextSize(compact ? 13 : 14);
        label.setGravity(compact ? Gravity.START : Gravity.CENTER);
        label.setIncludeFontPadding(false);
        LayoutParams labelParams = new LayoutParams(
                compact ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        if (compact) {
            labelParams.setMargins(dp(context, 10), 0, 0, 0);
        } else {
            labelParams.setMargins(0, dp(context, 14), 0, 0);
        }
        addView(label, labelParams);

        rotation = ObjectAnimator.ofFloat(ring, View.ROTATION, 0f, 360f);
        rotation.setDuration(1100L);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());

        pulseX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 1f, 1.04f, 1f);
        pulseX.setDuration(1500L);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);
        pulseX.setInterpolator(new AccelerateDecelerateInterpolator());

        pulseY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 1f, 1.04f, 1f);
        pulseY.setDuration(1500L);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setInterpolator(new AccelerateDecelerateInterpolator());
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    FendlyLoadingView(Context context) {
        this(context, "", Color.BLACK, Color.GRAY, false);
    }

    FendlyLoadingView(Context context, AttributeSet attrs) {
        this(context);
    }

    FendlyLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!FendlyMotion.animationsEnabled()) return;
        rotation.start();
        pulseX.start();
        pulseY.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        rotation.cancel();
        pulseX.cancel();
        pulseY.cancel();
        super.onDetachedFromWindow();
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static final class RingView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final int accentColor;

        RingView(Context context, int accentColor) {
            super(context);
            this.accentColor = accentColor;
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float stroke = Math.max(2f, getWidth() * 0.055f);
            float inset = stroke / 2f;
            bounds.set(inset, inset, getWidth() - inset, getHeight() - inset);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.argb(58, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (getWidth() - stroke) / 2f, paint);

            paint.setColor(accentColor);
            canvas.drawArc(bounds, -90f, 105f, false, paint);
        }
    }
}
