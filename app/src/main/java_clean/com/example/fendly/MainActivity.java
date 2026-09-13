package com.example.fendly;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
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
        root.setPadding(48, 72, 48, 48);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Fendly\nLost & Found");
        title.setTextColor(Color.rgb(31, 53, 74));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("Sign in to register this device for match notifications.");
        status.setTextSize(16);
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, 40, 0, 24);
        root.addView(status, statusParams);

        Button signIn = new Button(this);
        signIn.setText("Continue with Firebase");
        signIn.setOnClickListener(view -> FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    status.setText("Connected. Push notifications are enabled for this device.");
                    FcmRegistration.registerCurrentToken();
                })
                .addOnFailureListener(error -> status.setText("Connection failed: " + error.getMessage())));
        root.addView(signIn, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }
}