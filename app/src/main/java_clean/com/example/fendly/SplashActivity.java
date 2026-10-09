package com.example.fendly;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public final class SplashActivity extends Activity {
    private boolean splashReady;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> !splashReady);

        boolean languageSelected = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .contains("selected_language_index");
        if (!languageSelected) {
            openLanguageSelection();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            openMainActivity();
            return;
        }

        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful() && user.isEmailVerified()) {
                getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                        .putBoolean("email_verified", true)
                        .apply();
            }
            openMainActivity();
        });
    }

    private void openLanguageSelection() {
        splashReady = true;
        Intent intent = new Intent(this, LanguageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void openMainActivity() {
        splashReady = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
