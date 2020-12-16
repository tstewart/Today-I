package io.github.tstewart.todayi.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.threeten.bp.LocalTime;

import java.util.Calendar;

import androidx.annotation.NonNull;

/* Helper class for registering and unregistering daily reminder notifications */
public class DailyReminderAlarmHelper {

    public void registerAlarm(@NonNull Context context, LocalTime time) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        Calendar calendar = Calendar.getInstance();
        if(time != null) {
            calendar.set(Calendar.HOUR, time.getHour());
            calendar.set(Calendar.MINUTE,time.getMinute());
        }
            else {
            Log.i(this.getClass().getSimpleName(), "No time provided for alarm. Defaulting to 6pm.");
            calendar.set(Calendar.HOUR, 18);
            calendar.set(Calendar.MINUTE, 0);
        }

        calendar.set(Calendar.SECOND, 0);

        /* If the set time has already passed, add an extra day so that the event fires tomorrow instead. */
        if(calendar.getTimeInMillis() > System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        PendingIntent pendingIntent = getDailyAlarmIntent(context);

        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    /* Remove daily reminder alarm, so that it stops sending notifications */
    public void unregisterAlarm(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(getDailyAlarmIntent(context));

    }

    private PendingIntent getDailyAlarmIntent(Context context) {
        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.putExtra("title", "Daily reminder");
        serviceIntent.putExtra("content","Hello!");

        return PendingIntent.getService(context,0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
