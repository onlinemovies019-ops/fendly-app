package com.example.fendly;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.fendly.notifications.FcmRegistration;
import com.google.firebase.auth.FirebaseAuth;

public final class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(13, 12, 9);
    private static final int SURFACE = Color.rgb(25, 22, 17);
    private static final int GOLD = Color.rgb(212, 175, 55);
    private static final int GOLD_DARK = Color.rgb(138, 104, 24);
    private static final int SILVER = Color.rgb(199, 201, 204);
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        buildScreen();
        FcmRegistration.registerCurrentToken();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(66, 54, 66, 32);
        root.setBackgroundColor(BACKGROUND);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.fendly_brand);
        logo.setAdjustViewBounds(true);
        logo.setContentDescription("Fendly");
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(760, 570);
        root.addView(logo, logoParams);

        TextView tagline = new TextView(this);
        tagline.setText("Lost & Found across India");
        tagline.setTextColor(SILVER);
        tagline.setTextSize(22);
        tagline.setGravity(Gravity.CENTER);
        root.addView(tagline, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("Help is right here - get started.");
        status.setTextSize(24);
        status.setTextColor(Color.WHITE);
        status.setTypeface(null, android.graphics.Typeface.BOLD);
        status.setGravity(Gravity.START);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, 82, 0, 42);
        root.addView(status, statusParams);

        Button createProfile = new Button(this);
        createProfile.setText("Create my profile");
        createProfile.setTextColor(Color.WHITE);
        createProfile.setTextSize(20);
        createProfile.setAllCaps(false);
        createProfile.setTypeface(null, android.graphics.Typeface.BOLD);
        createProfile.setBackground(goldButton());
        createProfile.setBackgroundTintList(null);
        createProfile.setOnClickListener(view -> FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    status.setText("Profile started. Push notifications are enabled.");
                    FcmRegistration.registerCurrentToken();
                })
                .addOnFailureListener(error -> status.setText("Connection failed: " + error.getMessage())));
        root.addView(createProfile, new LinearLayout.LayoutParams(-1, -2));

        Button pinLogin = new Button(this);
        pinLogin.setText("Login with PIN");
        pinLogin.setTextColor(Color.WHITE);
        pinLogin.setTextSize(20);
        pinLogin.setAllCaps(false);
        pinLogin.setBackground(outlineButton());
        pinLogin.setBackgroundTintList(null);
        pinLogin.setOnClickListener(view -> status.setText("PIN login will be available after profile setup."));
        LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams(-1, -2);
        pinParams.setMargins(0, 24, 0, 0);
        root.addView(pinLogin, pinParams);

        TextView continueText = actionText("Continue to app", SILVER);
        continueText.setOnClickListener(view -> status.setText("Sign in or create a profile to continue."));
        LinearLayout.LayoutParams continueParams = new LinearLayout.LayoutParams(-1, -2);
        continueParams.setMargins(0, 68, 0, 0);
        root.addView(continueText, continueParams);

        TextView adminText = actionText("Admin Login", GOLD);
        adminText.setOnClickListener(view -> status.setText("Admin access is available to authorized staff."));
        LinearLayout.LayoutParams adminParams = new LinearLayout.LayoutParams(-1, -2);
        adminParams.setMargins(0, 84, 0, 0);
        root.addView(adminText, adminParams);
        setContentView(root);
    }

    private TextView actionText(String value, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(color);
        text.setTextSize(20);
        text.setGravity(Gravity.CENTER);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
        return text;
    }

    private GradientDrawable goldButton() {
        GradientDrawable button = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{GOLD_DARK, GOLD, Color.rgb(247, 231, 161)});
        button.setCornerRadius(28);
        button.setStroke(3, Color.rgb(255, 237, 157));
        return button;
    }

    private GradientDrawable outlineButton() {
        GradientDrawable button = new GradientDrawable();
        button.setColor(SURFACE);
        button.setCornerRadius(28);
        button.setStroke(3, Color.rgb(128, 116, 75));
        return button;
    }
}