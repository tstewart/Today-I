package io.github.tstewart.todayi.ui.activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ExportFailedException;
import io.github.tstewart.todayi.errors.ImportFailedException;
import io.github.tstewart.todayi.notifications.DailyReminderAlarmHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        /* Notification time key */
        private static final int NOTIFICATION_TIME_KEY = R.string.user_prefs_notification_time;
        /* Import data key */
        private static final String IMPORT_DATA_KEY = "import";
        /* Export data key */
        private static final String EXPORT_DATA_KEY = "export";
        /* Erase data key */
        private static final String ERASE_DATA_KEY = "erase";
        /* Last backed up key */
        private static final String LAST_BACKED_UP_KEY = "last_backed_up";
        /* Version key */
        private static final String VERSION_KEY = "version";

        /* Is Debug Activity access enabled */
        private static final boolean DEBUG_ENABLED = true;
        /* Number of taps on the version TextView required to open debug menu */
        private static final int DEBUG_ACTIVITY_TAP_REQUIREMENT = 6;
        /* Current debug tap count */
        private int mDebugActivityTapCount = 0;
        /* Toast alerts user how many clicks they need to access debug menu */
        private Toast mClicksToDebugToast;

        /* User preference manager */
        UserPreferences mUserPreferences;

        /* Last backed up label preference */
        Preference mLastBackedUp;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            /* Setup preferences to this app's preference file */
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(getString(R.string.user_prefs_file_location_key));
            this.mUserPreferences = new UserPreferences(preferenceManager.getSharedPreferences());

            setPreferencesFromResource(R.xml.main_preferences, rootKey);

            /* Set on click listeners for preferences with custom functionality */

            Preference notificationTime = findPreference(getString(NOTIFICATION_TIME_KEY));
            if(notificationTime != null) {
                notificationTime.setOnPreferenceClickListener(this::onNotificationTimeSelected);
                /* Set subtitle for this preference to the currently selected time */
                notificationTime.setSummary((String) mUserPreferences.get(getString(NOTIFICATION_TIME_KEY), "18:00"));
            }

            /* Import, export, and erase data buttons act as Preference buttons for functionality with PreferenceFragment*/
            Preference importData = findPreference(IMPORT_DATA_KEY);
            Preference exportData = findPreference(EXPORT_DATA_KEY);
            Preference eraseData = findPreference(ERASE_DATA_KEY);

            if(importData != null)
                importData.setOnPreferenceClickListener(this::onRestoreBackupClicked);
            if(exportData != null)
                exportData.setOnPreferenceClickListener(this::onForceBackupClicked);
            if(eraseData != null)
                eraseData.setOnPreferenceClickListener(this::onEraseClicked);

            /* Set last backed up label */
            mLastBackedUp = findPreference(LAST_BACKED_UP_KEY);
            if(mLastBackedUp != null)
                setLastBackedUpText(mLastBackedUp);


            /* Set version click listener for accessing hidden debug menu */
            Preference version = findPreference(VERSION_KEY);
            if(version != null) {

                /* Set version number */
                version.setSummary(getCurrentVersion());

                if (DEBUG_ENABLED) {
                    version.setOnPreferenceClickListener(this::onVersionClicked);
                }
            }

        }

        private boolean onNotificationTimeSelected(Preference preference) {

            /* Get notification time from preferences and attempt to parse into a LocalTime object */
            LocalTime currentNotificationTime;
            try {
                currentNotificationTime = LocalTime.parse(
                        (String) mUserPreferences.get(getString(NOTIFICATION_TIME_KEY), "18:00")
                );
            }
            catch (DateTimeParseException e) {
                /* Default to 18:00 if failed to parse */
                currentNotificationTime = LocalTime.of(18,0);
            }

            /* Show a time picker */
            new TimePickerDialog(getContext(),
                    (timeView, hourOfDay, minute) -> {
                        LocalTime selectedTime = LocalTime.of(hourOfDay,minute);
                        String selectedTimeString = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                        /* Set summary text for this preference to the selected time */
                        preference.setSummary(selectedTimeString);

                        /* Set notification time value in settings */
                        if(mUserPreferences != null)
                            mUserPreferences.set(getString(NOTIFICATION_TIME_KEY), selectedTimeString);

                        /* Restart notification alarm */
                        // TODO simplify/move?
                        if(getContext() != null)
                            new DailyReminderAlarmHelper().registerAlarm(getContext(),selectedTime,true);
                    },
                    currentNotificationTime.getHour(),
                    currentNotificationTime.getMinute(),
                    true)
                    .show();

            return true;
        }


        private boolean onRestoreBackupClicked(Preference preference) {
            /* Open an alert dialog to confirm if the user wishes to restore from backup */
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.setting_restore_backup)
                    .setMessage(R.string.restore_backup_confirmation)
                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Context context = getContext();
                            try {
                                /* Try and load from backup */
                                LocalDatabaseIO.importBackupDb(context, DBConstants.DB_NAME);

                                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show();

                            } catch (ImportFailedException e) {
                                /* If failed, alert user and log */
                                Log.w(this.getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));

                                Toast.makeText(context, "Failed to import backup:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.button_no, null)
                    .create()
                    .show();
            return true;
        }

        private boolean onForceBackupClicked(Preference preference) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.setting_force_backup)
                    .setMessage(R.string.force_backup_confirmation)
                    .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                        try {
                            /* Force backup database to file */
                            LocalDatabaseIO.backupDb(getContext(), DBConstants.DB_NAME);
                            /* Update last backed up */
                            mUserPreferences.set(getString(R.string.user_prefs_last_backed_up_key),System.currentTimeMillis());

                            /* Update last backed up */
                            if(mLastBackedUp != null)
                                setLastBackedUpText(mLastBackedUp);
                        } catch (ExportFailedException e) {
                            /* If failed, alert user and log */
                            Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
                            Toast.makeText(getContext(), "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.button_no, null)
                    .create()
                    .show();

            return true;
        }

        private boolean onEraseClicked(Preference preference) {
            /* Open an alert dialog to confirm if the user wishes to erase all data */
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.erase_all_warning_dialog_title)
                    .setMessage(R.string.erase_all_warning_dialog_message)
                    .setPositiveButton(R.string.button_yes, (dialogInterface, which) -> {
                        Database database = Database.getInstance(getContext());
                        SQLiteDatabase db = database.getWritableDatabase();

                        /* Delete data from both tables */
                        database.eraseAllData(db, DBConstants.ACCOMPLISHMENT_TABLE);
                        database.eraseAllData(db, DBConstants.RATING_TABLE);

                        Toast.makeText(getContext(), R.string.erase_all_confirmed, Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton(R.string.button_no, null)
                    .create()
                    .show();

            return true;
        }

        private boolean onVersionClicked(Preference preference) {
            /* Increment count of how many times the version TextView has been clicked so far */
            mDebugActivityTapCount++;

            /* If the number of clicks required to open DebugActivity has been met */
            if (mDebugActivityTapCount >= DEBUG_ACTIVITY_TAP_REQUIREMENT) {
                /* Start DebugActivity */
                Intent intent = new Intent(getContext(), DebugActivity.class);
                startActivity(intent);
                /* Reset tap count */
                mDebugActivityTapCount = 0;
            } else {
                /* Get number of taps required to enter debug */
                int tapsToDebugMenu = DEBUG_ACTIVITY_TAP_REQUIREMENT - mDebugActivityTapCount;
                String clicksToDebugMenu = getResources().getQuantityString(R.plurals.clicks_to_debug_menu,
                        tapsToDebugMenu, tapsToDebugMenu);

                /* Cancel existing toast if currently showing */
                if (mClicksToDebugToast != null) mClicksToDebugToast.cancel();

                /* Show a toast alerting user how many taps are required to open debug */
                mClicksToDebugToast = Toast.makeText(getContext(), clicksToDebugMenu, Toast.LENGTH_SHORT);
                mClicksToDebugToast.show();
            }
            return true;
        }

        /*
        Set last backed up label to a relative text string showing the last time since backing up the database
         */
        private void setLastBackedUpText(Preference preference) {
            preference.setSummary(getLastBackedUpRelativeString());
        }

        /*
        Gets the last time the database was backed up, in a time relative to the current time.
         */
        private String getLastBackedUpRelativeString() {
            /* Get time last backed up from user preferences */
            long lastBackedUp = (Long)mUserPreferences.get(getString(R.string.user_prefs_last_backed_up_key), -1);

            /* If a valid time was returned from user preferences */
            if (lastBackedUp > 0) {
                /* Return relative time span */
                return DateUtils.getRelativeTimeSpanString(lastBackedUp).toString();
            } else {
                /* If default value (-1) was returned, or an invalid time was returned */
                return "Unknown";
            }
        }

        /**
         * Get the current application version from PackageInfo
         * @return The current application version
         */
        private String getCurrentVersion() {
            Context context = getContext();
            if (context != null) {
                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    return pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            /* If version could not be received from PackageInfo, return default value */
            return "Unknown";
        }

    }
}