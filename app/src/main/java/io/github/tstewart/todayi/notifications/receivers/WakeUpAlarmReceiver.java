package io.github.tstewart.todayi.notifications.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.notifications.DailyReminderAlarmHelper;

/* Receives broadcasts when the user's device has woken up from being offline.
* In this case, the alarm must be reactivated. */
public class WakeUpAlarmReceiver extends BroadcastReceiver {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private static final String CLASS_LOG_TAG = WakeUpAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        /* Confirm that the correct action was received
        * This ensures the WakeUpReceiver request is only completed on boot completion. */
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(CLASS_LOG_TAG, "Received wake up request. Checking for alarm toggled permission.");

            if (context != null) {
                UserPreferences preferences = new UserPreferences(
                        context.getSharedPreferences(context.getString(R.string.user_prefs_file_location_key),
                                Context.MODE_PRIVATE
                        ));

                boolean isNotificationsEnabled = (boolean) preferences.get(context.getString(R.string.user_prefs_notifications_enabled), false);

                if (isNotificationsEnabled) {
                    DailyReminderAlarmHelper.registerAlarm(context, UserPreferences.getNotificationTime(), true);
                }
            }
        }
    }
}
