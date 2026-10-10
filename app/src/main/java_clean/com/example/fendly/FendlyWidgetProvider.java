package com.example.fendly;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class FendlyWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "FendlyWidget";
    private static final String API_BASE = "https://fendly-api.onrender.com";
    private static final String ACTION_REFRESH = "com.example.fendly.action.REFRESH_WIDGET";
    private static final String PREFERENCES = "fendly_widget_state";
    private static final String KEY_LOST = "lost_count";
    private static final String KEY_FOUND = "found_count";
    private static final String KEY_UNREAD = "unread_count";
    private static final String KEY_USER_UID = "user_uid";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void refresh(Context context) {
        Intent intent = new Intent(context, FendlyWidgetProvider.class)
                .setAction(ACTION_REFRESH)
                .setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH.equals(intent.getAction())) {
            refreshWidgets(context);
            return;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        refreshWidgets(context);
    }

    private void refreshWidgets(Context context) {
        Context applicationContext = context.getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(applicationContext);
        int[] widgetIds = manager.getAppWidgetIds(
                new ComponentName(applicationContext, FendlyWidgetProvider.class));
        if (widgetIds.length == 0) {
            return;
        }
        renderWidgets(applicationContext);
        PendingResult pendingResult = goAsync();
        EXECUTOR.execute(() -> {
            try {
                refreshCounts(applicationContext);
            } catch (Exception error) {
                Log.e(TAG, "Unable to refresh widget counts; keeping the last saved values", error);
            } finally {
                try {
                    renderWidgets(applicationContext);
                } catch (RuntimeException error) {
                    Log.e(TAG, "Unable to render refreshed widget counts", error);
                } finally {
                    pendingResult.finish();
                }
            }
        });
    }

    private static void refreshCounts(Context context) throws Exception {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if (user == null) {
            preferences.edit()
                    .putInt(KEY_LOST, 0)
                    .putInt(KEY_FOUND, 0)
                    .putInt(KEY_UNREAD, 0)
                    .remove(KEY_USER_UID)
                    .apply();
            return;
        }

        String idToken = Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS).getToken();
        if (idToken == null || idToken.isEmpty()) {
            throw new IOException("Firebase returned an empty authentication token");
        }

        String reportsJson = getAuthorized("/api/items/mine", idToken);
        String notificationsJson = getAuthorized("/api/users/notifications", idToken);
        int[] reportCounts = countReportTypes(reportsJson);
        int unreadCount = readUnreadCount(notificationsJson);
        preferences.edit()
                .putString(KEY_USER_UID, user.getUid())
                .putInt(KEY_LOST, reportCounts[0])
                .putInt(KEY_FOUND, reportCounts[1])
                .putInt(KEY_UNREAD, unreadCount)
                .apply();
    }

    static int[] countReportTypes(String responseBody) throws JSONException {
        JSONArray reports = new JSONArray(responseBody);
        int lostCount = 0;
        int foundCount = 0;
        for (int index = 0; index < reports.length(); index++) {
            JSONObject report = reports.optJSONObject(index);
            if (report == null) {
                continue;
            }
            String type = report.optString("type", "").trim();
            if ("LOST".equalsIgnoreCase(type)) {
                lostCount++;
            } else if ("FOUND".equalsIgnoreCase(type)) {
                foundCount++;
            }
        }
        return new int[]{lostCount, foundCount};
    }

    static int readUnreadCount(String responseBody) throws JSONException {
        JSONObject response = new JSONObject(responseBody);
        Object unreadValue = response.get("unread_count");
        if (!(unreadValue instanceof Number)) {
            throw new JSONException("Notification response has no numeric unread_count");
        }
        return Math.max(0, ((Number) unreadValue).intValue());
    }

    private static String getAuthorized(String path, String idToken) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = readBody(stream);
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("GET " + path + " returned HTTP " + responseCode
                        + (responseBody.isEmpty() ? "" : ": " + responseBody));
            }
            return responseBody;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static void renderWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, FendlyWidgetProvider.class));
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser == null ? null : currentUser.getUid();
        boolean hasCurrentUserCounts = currentUid != null
                && currentUid.equals(preferences.getString(KEY_USER_UID, null));
        int lostCount = hasCurrentUserCounts ? preferences.getInt(KEY_LOST, 0) : 0;
        int foundCount = hasCurrentUserCounts ? preferences.getInt(KEY_FOUND, 0) : 0;
        int unreadCount = hasCurrentUserCounts ? preferences.getInt(KEY_UNREAD, 0) : 0;
        for (int widgetId : widgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fendly_widget);
            boolean hasNotifications = unreadCount > 0 && hasCurrentUserCounts;
            int background = hasNotifications
                    ? R.drawable.fendly_widget_background_alert
                    : R.drawable.fendly_widget_background;
            views.setInt(R.id.widget_capsule, "setBackgroundResource", background);
            if (hasNotifications) {
                views.setViewVisibility(R.id.widget_dashboard, View.GONE);
                views.setViewVisibility(R.id.widget_notifications_count, View.VISIBLE);
                views.setTextViewText(R.id.widget_notifications_count,
                        LanguageManager.localizedDigits(context, String.valueOf(unreadCount)));
            } else {
                views.setViewVisibility(R.id.widget_dashboard, View.VISIBLE);
                views.setViewVisibility(R.id.widget_notifications_count, View.GONE);
                String lostLabel = LanguageManager.profileText(context, "widget_lost");
                String foundLabel = LanguageManager.profileText(context, "widget_found");
                String lostValue = hasCurrentUserCounts
                        ? LanguageManager.localizedDigits(context, String.valueOf(lostCount))
                        : "—";
                String foundValue = hasCurrentUserCounts
                        ? LanguageManager.localizedDigits(context, String.valueOf(foundCount))
                        : "—";
                views.setTextViewText(R.id.widget_lost_count, lostValue);
                views.setTextViewText(R.id.widget_found_count, foundValue);
                views.setContentDescription(R.id.widget_root, String.format(
                        Locale.getDefault(), "%s %s, %s %s",
                        lostLabel, lostValue, foundLabel, foundValue));
            }

            Intent openApp = new Intent(context, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            if (hasNotifications) {
                views.setContentDescription(R.id.widget_root, String.format(
                        Locale.getDefault(), "%s: %s",
                        LanguageManager.profileText(context, "widget_notifications"),
                        LanguageManager.localizedDigits(context, String.valueOf(unreadCount))));
            } else if (!hasCurrentUserCounts) {
                views.setContentDescription(R.id.widget_root,
                        LanguageManager.profileText(context, currentUid == null
                                ? "widget_sign_in"
                                : "widget_refresh"));
            }
            manager.updateAppWidget(widgetId, views);
        }
    }
}
