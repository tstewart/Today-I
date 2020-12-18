package io.github.tstewart.todayi.notifications.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.tstewart.todayi.notifications.NotificationSender;

/* Receives Alarm requests for Daily Reminder notifications, and sends a request to NotificationSender */
public class DailyAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationSender sender = new NotificationSender(context);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        sender.sendNotification(pendingIntent, "Daily Reminder", "What have you done today?");
    }
}
