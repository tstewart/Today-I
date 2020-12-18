package io.github.tstewart.todayi.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.threeten.bp.LocalTime;

import java.util.Calendar;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.notifications.receivers.DailyAlarmReceiver;

/* Helper class for registering and unregistering daily reminder notifications */
public class DailyReminderAlarmHelper {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private static final String CLASS_LOG_TAG = DailyReminderAlarmHelper.class.getSimpleName();

    public void registerAlarm(@NonNull Context context, LocalTime time) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        Calendar calendar = Calendar.getInstance();
        if(time != null) {
            calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
            calendar.set(Calendar.MINUTE,time.getMinute());
        }
            else {
            Log.i(CLASS_LOG_TAG, "No time provided for alarm. Defaulting to 6pm.");
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            calendar.set(Calendar.MINUTE, 0);
        }

        calendar.set(Calendar.SECOND, 0);

        /* If the set time has already passed, add an extra day so that the event fires tomorrow instead. */
        if(System.currentTimeMillis() > calendar.getTimeInMillis()) {
            Log.i(CLASS_LOG_TAG,"Provided notification time is in the past, pushing notification time forward a day.");
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        PendingIntent pendingIntent = getDailyAlarmIntent(context);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Log.i(CLASS_LOG_TAG,"Daily reminder notifications enabled.");
    }

    /* Remove daily reminder alarm, so that it stops sending notifications */
    public void unregisterAlarm(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(getDailyAlarmIntent(context));


        Log.i(CLASS_LOG_TAG,"Daily reminder notifications disabled.");
    }

    private PendingIntent getDailyAlarmIntent(Context context) {
        Intent serviceIntent = new Intent(context, DailyAlarmReceiver.class);

        return PendingIntent.getBroadcast(context,0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
