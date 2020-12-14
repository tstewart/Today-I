package io.github.tstewart.todayi;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ExportFailedException;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.helpers.db.DatabaseHelper;
import io.github.tstewart.todayi.helpers.NotificationHelper;
import io.github.tstewart.todayi.helpers.db.DayRatingTableHelper;

/*
 * Application class, called on application start
 * Manages automatic backup
 */
public class TodayI extends Application {
    /*
     Log tag, used for Logging
     Represents class name
    */
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    /* Backup Database every x hours */
    private static final int BACKUP_EVERY_HOURS = 24;

    /* User preferences */
    private UserPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE);
        mPreferences = new UserPreferences(sharedPrefs);

        /* Set default values for user preferences if they do not exist */
        mPreferences.setDefaultValue(getString(R.string.user_prefs_gestures_enabled), true);
        mPreferences.setDefaultValue(getString(R.string.user_prefs_clip_empty_lines), true);

        /* Set preference variables for this instance of the app */
        boolean gesturesEnabled = (boolean) mPreferences.get(getString(R.string.user_prefs_gestures_enabled), true);
        boolean clipEmptyLines = (boolean) mPreferences.get(getString(R.string.user_prefs_clip_empty_lines), true);
        UserPreferences.setEnableGestures(gesturesEnabled);
        UserPreferences.setAccomplishmentClipEmptyLines(clipEmptyLines);

        /* Setup notification channel if required */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createReminderNotificationChannel();
        }

        /* Database auto backup management */
        Context context = getApplicationContext();

        if (context != null && sharedPrefs != null) {
            boolean shouldBackup = false;

            if (mPreferences.contains(getString(R.string.user_prefs_last_backed_up_key))) {
                long lastBackedUp = (Long)mPreferences.get(getString(R.string.user_prefs_last_backed_up_key), -1);
                /* If the default value is selected, the app should attempt to backup data.
                If the last backup time is in the future (due to time modifications then we should backup anyway. (This may be changed)
                Additionally, If the app hasn't backed up within the number of hours defined in BACKUP_EVERY_HOURS, the app should attempt to backup data */
                if (lastBackedUp <= 0
                        || lastBackedUp > System.currentTimeMillis()
                        || hasNotBackedUpWithinHours(lastBackedUp)) shouldBackup = true;

                /* If the key couldn't be found, a backup should be run if the app contains at least one database entry. */
            } else if (!databasesEmpty(context)) {
                shouldBackup = true;
            }

            /* If there was a reason to backup */
            if (shouldBackup) {
                try {
                    /* Backup to local storage */
                    LocalDatabaseIO.backupDb(this, DBConstants.DB_NAME);

                    /* Set last time backed up */
                    mPreferences.set(getString(R.string.user_prefs_last_backed_up_key),System.currentTimeMillis());
                    Log.i(CLASS_LOG_TAG, "Application data backed up!");

                } catch (ExportFailedException e) {
                    Log.w(CLASS_LOG_TAG, e.getMessage(), e);
                    Toast.makeText(this, "Automatic backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.i(CLASS_LOG_TAG, "Application data did not need to backup.");
            }
        }
    }

    /* Create notification channel for daily reminder notifications.
    * This is required to show notifications in Android O and above. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createReminderNotificationChannel() {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        /* Channel id */
        String channelId = NotificationHelper.DAILY_REMINDERS_CHANNEL_ID;

        String channelName = getString(R.string.daily_reminder_notification_channel_name);

        /* Importance of the notification */
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        if(channelName != null) {
            NotificationChannel dailyReminderChannel = new NotificationChannel(channelId,channelName, importance);

            dailyReminderChannel.enableVibration(true);
            dailyReminderChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            manager.createNotificationChannel(dailyReminderChannel);
        }
    }

    /**
     * If there are no entries in either table of the database
     * @param context Application context
     * @return True if tables are empty
     */
    private boolean databasesEmpty(@NonNull Context context) {
        AccomplishmentTableHelper accomplishmentHelper = new AccomplishmentTableHelper(context);
        DayRatingTableHelper ratingHelper = new DayRatingTableHelper(context);

        return accomplishmentHelper.isEmpty() && ratingHelper.isEmpty();
    }

    /**
     * If the difference between the current date and the date last backed up
     * is greater than the time required between backups, return true
     * @param lastBackedUp Time last backed up
     * @return True if the time between backups is greater than BACKUP_EVERY_HOURS
     */
    private boolean hasNotBackedUpWithinHours(long lastBackedUp) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());

        c.add(Calendar.HOUR, -BACKUP_EVERY_HOURS);

        return lastBackedUp <= c.getTime().getTime();
    }
}
