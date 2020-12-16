package io.github.tstewart.todayi.notifications;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import io.github.tstewart.todayi.ui.activities.MainActivity;

public class NotificationService extends IntentService {

    public NotificationService(String name) {
        super(name);
    }

    public NotificationService() {
        super("notification-service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                String notificationTitle = intent.getStringExtra("title");
                String notificationContent = intent.getStringExtra("content");

                if(notificationTitle != null && notificationContent != null) {

                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                            new Intent(this, MainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    new NotificationSender(this).sendNotification(pendingIntent, notificationTitle, notificationContent);

                    return;
                }
            }
        }
        Log.w(this.getClass().getSimpleName(), "Failed to send notification. Provided Intent was invalid.");
    }
}
