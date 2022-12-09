package io.github.tstewart.todayi.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalTime;

import java.util.Calendar;

import io.github.tstewart.todayi.notifications.receivers.DailyAlarmReceiver;

/* Helper class for registering and unregistering daily reminder notifications */
public class DailyReminderAlarmHelper {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private static final String CLASS_LOG_TAG = DailyReminderAlarmHelper.class.getSimpleName();

    /* Private constructor prevents initialisation of helper class */
    private DailyReminderAlarmHelper() {
    }

    /**
     * Update currently active alarm. Replaces existing alarm time
     *
     * @param context Application context
     * @param time    Time for notification to be called on
     */
    public static void updateAlarm(@NonNull Context context, LocalTime time) {
        registerAlarm(context, time, true);
    }

    /**
     * Register an alarm to send notifications daily at the provided time
     *
     * @param context         Application context
     * @param time            Time to send notification
     * @param overrideCurrent If an alarm of this type already exists, should it be overwritten
     */
    public static void registerAlarm(@NonNull Context context, LocalTime time, boolean overrideCurrent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        Calendar calendar = Calendar.getInstance();
        /* If provided time is not null, set notification time to provided time */
        if (time != null) {
            calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
            calendar.set(Calendar.MINUTE, time.getMinute());
        }
        /* If provided time was null, default to a notification at 6pm */
        else {
            Log.i(CLASS_LOG_TAG, "No time provided for alarm. Defaulting to 6pm.");
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            calendar.set(Calendar.MINUTE, 0);
        }

        /* Set notification second to 0
         * Ensures notification is called at the start of the minute */
        calendar.set(Calendar.SECOND, 0);

        /* If the set time has already passed, add an extra day so that the event fires tomorrow instead. */
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            Log.i(CLASS_LOG_TAG, "Provided notification time is in the past, pushing notification time forward a day.");
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        /* Check to see if notification alarm should replace existing alarm
         * Also get what service should be called when alarm is elapsed */
        PendingIntent pendingIntent = getDailyAlarmIntent(context, overrideCurrent);

        if (pendingIntent != null) {
            /* Set inexact alarm, as the notification doesn't need to be shown at the exact provided time */
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.i(CLASS_LOG_TAG, "Daily reminder notifications enabled.");
        }
    }

    /**
     * Remove daily reminder alarm, so that it stops sending notifications
     *
     * @param context Application context
     */
    public static void unregisterAlarm(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(getDailyAlarmIntent(context, true));


        Log.i(CLASS_LOG_TAG, "Daily reminder notifications disabled.");
    }

    /**
     * Get service to be called when notification alarm time has elapsed
     *
     * @param context         Application context
     * @param overrideCurrent Should this replace existing alarm (if exists)
     * @return PendingIntent with provided service, and if this should replace existing alarm
     */
    private static PendingIntent getDailyAlarmIntent(Context context, boolean overrideCurrent) {
        Intent serviceIntent = new Intent(context, DailyAlarmReceiver.class);

        if (!overrideCurrent) {
            PendingIntent existsCheck = PendingIntent.getBroadcast(context, 0,
                    serviceIntent, PendingIntent.FLAG_IMMUTABLE);

            /* If the current alarm should not be overwritten, but FLAG_NO_CREATE has indicated that an alarm already exists
             * (by not returning null), then return null so that we do not register a new alarm of this type */
            if (existsCheck != null) return null;
        }
        return PendingIntent.getBroadcast(context, 0,
                serviceIntent, PendingIntent.FLAG_IMMUTABLE);
    }
}
