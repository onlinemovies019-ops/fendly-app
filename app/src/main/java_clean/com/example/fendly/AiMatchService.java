package com.example.fendly;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class AiMatchService {
    private static final String API_BASE = "https://fendly-api.onrender.com";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 60000;

    private AiMatchService() {
    }

    public static ApiResponse createItem(
            String title,
            String description,
            String imageUrl,
            String type,
            double latitude,
            double longitude,
            String location,
            String date,
            String paymentId,
            String idToken
    ) throws Exception {
        JSONObject payload = new JSONObject()
                .put("title", title)
                .put("description", description)
                .put("imageUrl", imageUrl == null ? JSONObject.NULL : imageUrl)
                .put("type", type.toLowerCase(Locale.US))
                .put("lat", latitude)
                .put("lng", longitude)
                .put("report_location", location)
                .put("report_date", date)
                .put("category", "other")
                .put("payment_id", paymentId == null ? JSONObject.NULL : paymentId);
        return post("/api/items", payload, idToken);
    }

    public static ApiResponse findMatches(String foundItemId, String idToken) throws Exception {
        JSONObject payload = new JSONObject()
                .put("found_item_id", foundItemId)
                .put("radius_degrees", 10.0);
        return post("/api/items/match", payload, idToken);
    }

    private static ApiResponse post(String path, JSONObject payload, String idToken) throws Exception {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("A Firebase ID token is required");
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            byte[] requestBody = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestBody);
            }

            int statusCode = connection.getResponseCode();
            InputStream responseStream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            return new ApiResponse(statusCode, readResponse(responseStream));
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String readResponse(InputStream input) throws Exception {
        if (input == null) return "";
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    public static final class ApiResponse {
        private final int statusCode;
        private final String body;

        private ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }

        public String getErrorMessage() {
            try {
                JSONObject json = new JSONObject(body);
                return json.optString("detail", json.optString("error", body));
            } catch (JSONException ignored) {
                return body;
            }
        }
    }
}
