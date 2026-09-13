package com.example.fendly.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.fendly.MainActivity;
import com.example.fendly.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public final class FendlyMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "fendly_matches";

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
        showNotification(title == null ? "Possible Fendly match" : title,
                body == null ? "A possible lost and found match needs review." : body);
    }

    private void showNotification(String title, String body) {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            return;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, "Fendly matches", NotificationManager.IMPORTANCE_DEFAULT));
        }
        PendingIntent intent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.fendly_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(intent)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(title.hashCode(), notification.build());
    }
}