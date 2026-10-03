package com.example.fendly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class SplashActivity extends Activity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        root.setPadding(dp(28), dp(32), dp(28), dp(24));
        
        int background = darkMode ? Color.rgb(18, 19, 25) : Color.rgb(247, 243, 238);
        int mutedText = darkMode ? Color.rgb(110, 112, 128) : Color.rgb(92, 94, 102);
        int gold = Color.rgb(232, 178, 74);
        
        root.setBackgroundColor(background);
        if (Build.VERSION.SDK_INT >= 29) {
            root.setForceDarkAllowed(false);
        }
        WindowInsetsHelper.applySafeArea(root);
        
        getWindow().getDecorView().setSystemUiVisibility(darkMode ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.fendly_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setAdjustViewBounds(true);
        logo.setContentDescription("Fendly logo");
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(140), dp(140));
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.setMargins(0, dp(6), 0, dp(14));
        root.addView(logo, logoParams);

        setContentView(root);

        boolean languageSelected = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .contains("selected_language_index");
        if (!languageSelected) {
            Intent intent = new Intent(this, LanguageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                boolean verified = task.isSuccessful() && user.isEmailVerified();
                if (verified) {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                            .putBoolean("email_verified", true)
                            .apply();
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }).addOnFailureListener(e -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            });
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
