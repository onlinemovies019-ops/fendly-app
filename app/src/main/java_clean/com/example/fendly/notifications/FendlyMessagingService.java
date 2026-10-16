package com.example.fendly.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.fendly.FendlyWidgetProvider;
import com.example.fendly.LanguageManager;
import com.example.fendly.MainActivity;
import com.example.fendly.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public final class FendlyMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "fendly_updates";
    private static final String ACTION_USER_NOTIFICATION_RECEIVED =
            "com.example.fendly.USER_NOTIFICATION_RECEIVED";

    @Override
    public void onNewToken(String token) {
        FcmRegistration.registerToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String title = message.getData().get("title");
        String body = message.getData().get("body");
        if (title == null && message.getNotification() != null) {
            title = message.getNotification().getTitle();
        }
        if (body == null && message.getNotification() != null) {
            body = message.getNotification().getBody();
        }
        String notificationTitle = title == null ? "Fendly notification" : title;
        String notificationBody = body == null ? "Open Fendly to view this update." : body;
        String notificationType = message.getData().get("notification_type");
        if ("adoption_application_received".equals(notificationType)) {
            notificationTitle = LanguageManager.adoptionText(
                    this,
                    "adopt_notification_received_title"
            );
            notificationBody = LanguageManager.adoptionText(
                    this,
                    "adopt_notification_received_body"
            );
        } else if ("adoption_application_update".equals(notificationType)) {
            notificationTitle = LanguageManager.adoptionText(
                    this,
                    "adopt_notification_update_title"
            );
            notificationBody = LanguageManager.adoptionText(
                    this,
                    "adopt_notification_update_body"
            );
        }
        showNotification(notificationTitle, notificationBody, message.getData());
        sendBroadcast(new Intent(ACTION_USER_NOTIFICATION_RECEIVED).setPackage(getPackageName()));
        FendlyWidgetProvider.refresh(this);
    }

    private void showNotification(String title, String body, java.util.Map<String, String> data) {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID,
                    LanguageManager.adoptionText(this, "adopt_notification_channel"),
                    NotificationManager.IMPORTANCE_DEFAULT
            ));
        }
        Intent openIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String notificationId = data.get("notification_id");
        if (notificationId != null && !notificationId.isEmpty()) {
            openIntent.putExtra(MainActivity.EXTRA_USER_NOTIFICATION_ID, notificationId);
        }
        if ("adoption_application_received".equals(data.get("notification_type"))) {
            String listingId = data.get("adoption_listing_id");
            if (listingId != null && !listingId.isEmpty()) {
                openIntent.setAction("com.example.fendly.OPEN_ADOPTION_NOTIFICATION");
                openIntent.putExtra(MainActivity.EXTRA_ADOPTION_LISTING_ID, listingId);
            }
        }
        int requestCode = notificationId == null
                ? title.hashCode()
                : notificationId.hashCode();
        if (notificationId != null && !notificationId.isEmpty()) {
            openIntent.setData(android.net.Uri.parse("fendly://notification/" + notificationId));
        }
        PendingIntent intent = PendingIntent.getActivity(
                this, requestCode, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.fendly_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(intent)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(requestCode, notification.build());
    }
}