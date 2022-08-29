package io.github.tstewart.todayi;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeParseException;

import java.util.Calendar;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.material.color.DynamicColors;

import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ExportFailedException;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.notifications.DailyReminderAlarmHelper;
import io.github.tstewart.todayi.notifications.NotificationSender;
import io.github.tstewart.todayi.helpers.db.DayRatingTableHelper;
import io.github.tstewart.todayi.ui.activities.PasswordActivity;
import io.github.tstewart.todayi.ui.activities.SettingsActivity;

/*
 * Application class, called on application start
 */
public class TodayI extends Application {
    /*
     Log tag, used for Logging
     Represents class name
    */
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    /* Backup Database every x hours */
    private static final int BACKUP_EVERY_HOURS = 24;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Apply dynamic colors */
        DynamicColors.applyToActivitiesIfAvailable(this);

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE);
        UserPreferences preferences = new UserPreferences(sharedPrefs);

        /* Set default values for user preferences if they do not exist */
        setDefaultPreferences(preferences);

        /* Set preference variables for this instance of the app */
        setInstancePreferences(preferences);

        /* Toggle notification alarm on, if notifications are enabled */
        if(UserPreferences.isEnableNotifications())
            DailyReminderAlarmHelper.registerAlarm(this, UserPreferences.getNotificationTime(), false);

        /* Setup notification channel if required */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createReminderNotificationChannel();
        }

        /* Database auto backup management */
        Context context = getApplicationContext();

        if (context != null && sharedPrefs != null) {
            if(shouldBackup(context, preferences)) {
                /* If there was a reason to backup */
                    try {
                        /* Backup to local storage */
                        LocalDatabaseIO.backupDb(this);

                        /* Set last time backed up */
                        preferences.set(getString(R.string.user_prefs_last_backed_up_key),System.currentTimeMillis());
                        Log.i(CLASS_LOG_TAG, "Application data backed up!");

                    } catch (ExportFailedException e) {
                        Log.w(CLASS_LOG_TAG, e.getMessage(), e);
                        Toast.makeText(this, String.format(getString(R.string.automatic_backup_failed), e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.i(CLASS_LOG_TAG, "Application data did not need to backup.");
                }
        }

        /* Watch for app start/stop events */
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
    }

    /* Set default preference values if certain preferences do not exist yet
    * E.g. on first run, the application needs to set default values */
    private void setDefaultPreferences(UserPreferences preferences) {
        if(preferences != null) {
            preferences.setDefaultValue(getString(R.string.user_prefs_tutorial_shown), false);
            preferences.setDefaultValue(getString(R.string.user_prefs_password_protection), false);
            preferences.setDefaultValue(getString(R.string.user_prefs_auto_lock), false);
            preferences.setDefaultValue(getString(R.string.user_prefs_notifications_enabled), false);
            preferences.setDefaultValue(getString(R.string.user_prefs_notification_time), "18:00");
            preferences.setDefaultValue(getString(R.string.user_prefs_gestures_enabled), true);
            preferences.setDefaultValue(getString(R.string.user_prefs_clip_empty_lines), true);
            preferences.setDefaultValue(getString(R.string.user_prefs_num_day_ratings), "5");
        }
    }

    /* Get preferences from file and set this instance of the application's values */
    private void setInstancePreferences(UserPreferences preferences) {
        boolean tutorialShown = (boolean) preferences.get(getString(R.string.user_prefs_tutorial_shown), true);
        boolean passwordProtection = (boolean) preferences.get(getString(R.string.user_prefs_password_protection), false);
        boolean autoLock = (boolean) preferences.get(getString(R.string.user_prefs_auto_lock), false);
        boolean notificationsEnabled = (boolean) preferences.get(getString(R.string.user_prefs_notifications_enabled), false);
        boolean gesturesEnabled = (boolean) preferences.get(getString(R.string.user_prefs_gestures_enabled), true);
        String numRatings = (String) preferences.get(getString(R.string.user_prefs_num_day_ratings), "5");
        String notificationTimeString = (String) preferences.get(getString(R.string.user_prefs_notification_time), "18:00");
        boolean clipEmptyLines = (boolean) preferences.get(getString(R.string.user_prefs_clip_empty_lines), true);

        UserPreferences.setTutorialShown(tutorialShown);
        UserPreferences.setEnablePasswordProtection(passwordProtection);
        UserPreferences.setEnableAutoLock(autoLock);
        UserPreferences.setEnableNotifications(notificationsEnabled);
        UserPreferences.setEnableGestures(gesturesEnabled);
        UserPreferences.setAccomplishmentClipEmptyLines(clipEmptyLines);
        try {
            UserPreferences.setMaxDayRating(Integer.parseInt(numRatings));
        } catch(NumberFormatException e) {
            Log.w(CLASS_LOG_TAG,"Failed to parse current max day rating. Defaulting to 5.");
            UserPreferences.setMaxDayRating(5);
        }

        /* Try and parse notification time from string */
        LocalTime notificationTime;
        try {
            notificationTime = LocalTime.parse(notificationTimeString);
        } catch (DateTimeParseException e) {
            /* Default to 6PM */
            Log.w(CLASS_LOG_TAG,"Failed to parse current notification time. Defaulting to 6pm.");
            notificationTime = LocalTime.of(18,0);
        }
        UserPreferences.setNotificationTime(notificationTime);
    }

    /* Checks if the application needs to run a backup */
    private boolean shouldBackup(Context appContext, UserPreferences preferences) {
        if (preferences.contains(getString(R.string.user_prefs_last_backed_up_key))) {
            long lastBackedUp = (Long)preferences.get(getString(R.string.user_prefs_last_backed_up_key), -1);
                /* If the default value is selected, the app should attempt to backup data.
                If the last backup time is in the future (due to time modifications then we should backup anyway. (This may be changed)
                Additionally, If the app hasn't backed up within the number of hours defined in BACKUP_EVERY_HOURS, the app should attempt to backup data */
            return lastBackedUp <= 0
                    || lastBackedUp > System.currentTimeMillis()
                    || hasNotBackedUpWithinHours(lastBackedUp);

            /* If the key couldn't be found, a backup should be run if the app contains at least one database entry. */
        } else return !databasesEmpty(appContext);
    }

    /* Create notification channel for daily reminder notifications.
    * This is required to show notifications in Android O and above. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createReminderNotificationChannel() {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        /* Channel id */
        String channelId = NotificationSender.DAILY_REMINDERS_CHANNEL_ID;

        String channelName = getString(R.string.daily_reminder_notification_channel_name);

        /* Importance of the notification */
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel dailyReminderChannel = new NotificationChannel(channelId,channelName, importance);

        dailyReminderChannel.enableVibration(true);
        dailyReminderChannel.setVibrationPattern(new long[]{100, 300});

        manager.createNotificationChannel(dailyReminderChannel);
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

    class AppLifecycleListener implements DefaultLifecycleObserver {

        AppLifecycleListener() {
            if(UserPreferences.isEnablePasswordProtection()) {
                Log.i(this.getClass().getSimpleName(), "Starting initial login.");
                goToLogin();
            }
        }

        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);

            if(UserPreferences.isEnablePasswordProtection() && UserPreferences.isEnableAutoLock()) {
                Log.i(this.getClass().getSimpleName(), "Returning to login page from application resume.");
                goToLogin();

            }
        }

        void goToLogin() {
            Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
