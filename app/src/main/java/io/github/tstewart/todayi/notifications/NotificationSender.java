package io.github.tstewart.todayi.notifications;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import io.github.tstewart.todayi.R;

/* Sends notifications to the user, with title and content as defined in the function */
public class NotificationSender {

    /* Default notification channel, for daily reminder notifications
    * The application initialises this channel on startup, and since there are no other notifications
    * in the application, this channel is enforced. */
    public static final String DAILY_REMINDERS_CHANNEL_ID = "daily-reminders";

    /* Application context */
    private Context mContext;

    public NotificationSender(Context context) {
        mContext = context;
    }

    public void sendNotification(PendingIntent launchIntent, String title, String content) {
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(getContext(), DAILY_REMINDERS_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(launchIntent)
                .setSound(soundUri).setSmallIcon(R.drawable.notification_logo)
                .build();
        NotificationManagerCompat.from(getContext()).notify(0, notification);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

}
