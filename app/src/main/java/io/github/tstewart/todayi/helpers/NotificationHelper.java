package io.github.tstewart.todayi.helpers;

import android.app.Notification;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import io.github.tstewart.todayi.R;

public class NotificationHelper {

    /* Default notification channel, for daily reminder notifications
    * The application initialises this channel on startup, and since there are no other notifications
    * in the application, this channel is enforced. */
    public static final String DAILY_REMINDERS_CHANNEL_ID = "daily-reminders";

    /* Application context */
    private Context mContext;

    public NotificationHelper(Context context) {
        mContext = context;
    }

    public void sendNotification(String title, String content) {
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(getContext(), DAILY_REMINDERS_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                //TODO change icon to support small icon
                .setSound(soundUri).setSmallIcon(R.mipmap.ic_launcher)
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
