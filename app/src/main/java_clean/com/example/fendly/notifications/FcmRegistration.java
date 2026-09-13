package com.example.fendly.notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FcmRegistration {
    private static final String ENDPOINT = "https://fendly-api.onrender.com/api/devices/fcm-token";
    private static final ExecutorService NETWORK = Executors.newSingleThreadExecutor();

    private FcmRegistration() {
    }

    public static void registerCurrentToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(FcmRegistration::registerToken)
                .addOnFailureListener(ignored -> {
                });
    }

    public static void registerToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || token == null || token.isEmpty()) {
            return;
        }
        user.getIdToken(false).addOnSuccessListener(result -> {
            if (result == null || result.getToken() == null) {
                return;
            }
            NETWORK.execute(() -> postToken(token, result.getToken()));
        });
    }

    private static void postToken(String fcmToken, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] body = ("{\"token\":\"" + escapeJson(fcmToken) + "\",\"platform\":\"android\"}")
                    .getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }
            connection.getResponseCode();
        } catch (IOException ignored) {
            // A later app start or Firebase token refresh retries registration.
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}