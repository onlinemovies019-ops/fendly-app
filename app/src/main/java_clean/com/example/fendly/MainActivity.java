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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.fendly.notifications.FcmRegistration;
import com.google.firebase.auth.FirebaseAuth;

public final class MainActivity extends Activity {
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
        root.setPadding(48, 40, 48, 32);
        root.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.fendly_logo);
        logo.setAdjustViewBounds(true);
        logo.setContentDescription("Fendly");
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(280, 190);
        root.addView(logo, logoParams);

        TextView tagline = new TextView(this);
        tagline.setText("Lost & Found across India");
        tagline.setTextColor(Color.rgb(100, 100, 100));
        tagline.setTextSize(16);
        tagline.setGravity(Gravity.CENTER);
        root.addView(tagline, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("Help is right here - get started.");
        status.setTextSize(17);
        status.setTextColor(Color.rgb(125, 125, 125));
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, 30, 0, 24);
        root.addView(status, statusParams);

        Button createProfile = new Button(this);
        createProfile.setText("Create my profile");
        createProfile.setTextColor(Color.WHITE);
        createProfile.setBackgroundColor(Color.rgb(36, 96, 175));
        createProfile.setOnClickListener(view -> FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    status.setText("Profile started. Push notifications are enabled.");
                    FcmRegistration.registerCurrentToken();
                })
                .addOnFailureListener(error -> status.setText("Connection failed: " + error.getMessage())));
        root.addView(createProfile, new LinearLayout.LayoutParams(-1, -2));

        Button pinLogin = new Button(this);
        pinLogin.setText("Login with PIN");
        pinLogin.setOnClickListener(view -> status.setText("PIN login will be available after profile setup."));
        LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams(-1, -2);
        pinParams.setMargins(0, 12, 0, 0);
        root.addView(pinLogin, pinParams);
        setContentView(root);
    }
}