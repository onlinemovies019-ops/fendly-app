package com.example.fendly;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public final class AiMatchService {
    private static final String API_BASE = "https://fendly-api.onrender.com";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 60000;

    private AiMatchService() {
    }

    public static String inferCategory(String title, String description) {
        String searchableText = " " + ((title == null ? "" : title) + " " + (description == null ? "" : description))
                .toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", " ").trim() + " ";
        String[][] categories = {
                {"Electronics", "smartphone", "mobile phone", "cell phone", "phone", "mobile", "television", "computer mouse", "wireless mouse", "laptop", "computer", "tablet", "charger", "headphone", "earphone", "camera", "smartwatch", "refrigerator", "fridge", "washing machine", "microwave", "speaker", "remote", "router", "monitor", "printer", "keyboard", "tube light", "led light", "light bulb", "flashlight", "torch", "lamp", "tv", "fan", "bulb"},
                {"Animals", "animal", "dog", "puppy", "cat", "kitten", "mouse", "mice", "cow", "goat", "sheep", "horse", "bird", "parrot", "rabbit", "pet", "fish", "snake"},
                {"People", "missing person", "person", "people", "man", "men", "woman", "women", "male", "female", "boy", "boys", "girl", "girls", "kid", "kids", "child", "children", "toddler"},
                {"Apparels and accessories", "wallet", "purse", "handbag", "backpack", "bag", "belt", "spectacles", "sunglasses", "goggles", "glasses", "eyeglasses", "spects", "specs", "clothing", "clothes", "apparel", "shirt", "trousers", "pants", "dress", "jacket", "coat", "shoes", "sandals", "footwear", "cap", "hat", "scarf", "gloves", "umbrella"},
                {"Automobile", "auto rickshaw", "motorcycle", "motorbike", "bicycle", "scooter", "scooty", "moped", "vehicle", "tractor", "truck", "bus", "car", "bike", "cycle", "van", "auto"},
                {"Documents", "identity card", "id card", "passport", "driver license", "driving license", "certificate", "document", "aadhaar", "pan card", "license", "paper"},
                {"Jewelry", "necklace", "bracelet", "earring", "jewelry", "jewellery", "bangle", "ring", "gold chain"},
                {"Keys", "keychain", "keys", "key"},
                {"Household items", "furniture", "utensils", "cookware", "sofa", "chair", "table", "bed", "pillow", "blanket", "curtain", "mattress"},
                {"Sports equipment", "cricket bat", "tennis racket", "football", "basketball", "volleyball", "racket", "bat", "ball"},
                {"Toys", "stuffed toy", "teddy bear", "toy", "doll", "puzzle"},
                {"Tools", "screwdriver", "wrench", "hammer", "drill", "toolbox", "tool"},
                {"Medical items", "medicine", "medication", "medical device", "inhaler"}
        };
        for (String[] category : categories) {
            for (int index = 1; index < category.length; index++) {
                if (searchableText.contains(" " + category[index] + " ")) return category[0];
            }
        }
        return "other";
    }

    public static ApiResponse createItem(
            String title,
            String description,
            String imageUrl,
            List<String> imageUrls,
            String type,
            double latitude,
            double longitude,
            String location,
            String date,
            String paymentId,
            String sourceLanguage,
            String idToken,
            String imeiNumber,
            String reportCategory
    ) throws Exception {
        JSONArray images = new JSONArray();
        if (imageUrls != null) {
            for (String image : imageUrls) {
                if (image != null && !image.trim().isEmpty()) images.put(image.trim());
            }
        }
        JSONObject payload = new JSONObject()
                .put("title", title)
                .put("description", description)
                .put("source_language", sourceLanguage)
                .put("imageUrl", imageUrl == null ? JSONObject.NULL : imageUrl)
                .put("imageUrls", images)
                .put("type", type == null ? "found" : type.toLowerCase(Locale.US))
                .put("lat", latitude)
                .put("lng", longitude)
                .put("report_location", location == null ? JSONObject.NULL : location)
                .put("report_date", date == null ? JSONObject.NULL : date)
                .put("category", reportCategory == null || reportCategory.trim().isEmpty()
                        ? inferCategory(title, description)
                        : reportCategory.trim())
                .put("payment_id", paymentId == null ? JSONObject.NULL : paymentId);
        if ("lost".equalsIgnoreCase(type) && imeiNumber != null && !imeiNumber.isEmpty()) {
            payload.put("imei_number", imeiNumber);
        }
        return post("/api/items", payload, idToken);
    }

    public static ApiResponse findImageMatches(String imageUrl, String targetType, String idToken) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("A valid imageUrl is required for visual matching");
        }
        String normalizedType = targetType == null ? "" : targetType.toLowerCase(Locale.US);
        if (!"lost".equals(normalizedType) && !"found".equals(normalizedType)) {
            throw new IllegalArgumentException("targetType must be lost or found");
        }
        JSONObject payload = new JSONObject()
                .put("imageUrl", imageUrl)
                .put("targetType", normalizedType);
        return post("/api/items/match", payload, idToken);
    }

    public static ApiResponse findMatches(String itemId, String itemType, String idToken) throws Exception {
        JSONObject payload = new JSONObject()
                .put("radius_degrees", 10.0);
        if ("found".equalsIgnoreCase(itemType)) {
            payload.put("found_item_id", itemId);
        } else if ("lost".equalsIgnoreCase(itemType)) {
            payload.put("lost_item_id", itemId);
        } else {
            throw new IllegalArgumentException("Item type must be lost or found");
        }
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

        public String getCreatedItemId() {
            try {
                return new JSONObject(body).optString("id", "");
            } catch (JSONException ignored) {
                return "";
            }
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
