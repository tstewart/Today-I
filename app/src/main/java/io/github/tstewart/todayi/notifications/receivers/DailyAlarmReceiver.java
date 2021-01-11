package io.github.tstewart.todayi.notifications.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.tstewart.todayi.notifications.NotificationSender;
import io.github.tstewart.todayi.ui.activities.MainActivity;

/* Receives Alarm requests for Daily Reminder notifications, and sends a request to NotificationSender */
//TODO RESOURCE STRING
public class DailyAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationSender sender = new NotificationSender(context);

        Intent mainActivityIntent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        sender.sendNotification(pendingIntent, false,"Daily Reminder", "What have you done today?");
    }
}
