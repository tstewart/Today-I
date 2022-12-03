package io.github.tstewart.todayi.ui.fragments;

import android.Manifest;
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

import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.TodayI;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.data.PreferencesKeyStore;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ExportFailedException;
import io.github.tstewart.todayi.errors.ImportFailedException;
import io.github.tstewart.todayi.notifications.DailyReminderAlarmHelper;
import io.github.tstewart.todayi.ui.activities.DebugActivity;
import io.github.tstewart.todayi.ui.activities.BackupFileSelectorActivity;

/*
Fragment of settings views and their functionality.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    /* Preference key store */
    private PreferencesKeyStore mPreferenceKeys;

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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        /* Setup preferences to this app's preference file */
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(getString(R.string.user_prefs_file_location_key));
        this.mUserPreferences = new UserPreferences(preferenceManager.getSharedPreferences());

        setPreferencesFromResource(R.xml.main_preferences, rootKey);

        /* Initialize key store */
        Context context = getContext();
        if(context != null) {
            this.mPreferenceKeys = new PreferencesKeyStore(getContext());
        }

        /* Set on preference changed listeners for preferences to update settings for the current instance of the app
        * When settings are changed, their value in the preferences file is updated. However, these changes are not reflected until
        * the application is restarted. This is a fix for that.  */
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int preferenceCount = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if(preference instanceof PreferenceCategory) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) preferenceScreen.getPreference(i);

                int preferenceChildCount = preferenceCategory.getPreferenceCount();

                for (int j = 0; j < preferenceChildCount; j++) {
                    Preference childPreference = preferenceCategory.getPreference(j);
                    childPreference.setOnPreferenceChangeListener(this::onPreferenceChanged);
                }
            }
        }

        /* Toggle notification icon */
        SwitchPreferenceCompat notificationPreference = findPreference(mPreferenceKeys.ENABLE_NOTIFICATIONS_KEY);
        if (notificationPreference != null) toggleNotificationIcon(notificationPreference);

        /* Set on click listeners for preferences with custom functionality */

        /* Set current notification time summary */
        setCurrentNotificationTime();

        /* Import, export, and erase data buttons act as Preference buttons for functionality with PreferenceFragment*/
        setDataManagementListeners();

        /* Set last backed up label */
        setLastBackedUpText();

        /* Set version click listener for accessing hidden debug menu */
        Preference version = findPreference(PreferencesKeyStore.VERSION_KEY);
        if(version != null) {

            /* Set version number */
            version.setSummary(getCurrentVersion());

            if (DEBUG_ENABLED) {
                version.setOnPreferenceClickListener(this::onVersionClicked);
            }
        }
    }

    private void setCurrentNotificationTime() {
        Preference notificationTime = findPreference(mPreferenceKeys.NOTIFICATION_TIME_KEY);
        if(notificationTime != null) {
            notificationTime.setOnPreferenceClickListener(this::onNotificationTimeSelected);
            /* Set subtitle for this preference to the currently selected time */
            notificationTime.setSummary((String) mUserPreferences.get(mPreferenceKeys.NOTIFICATION_TIME_KEY, "18:00"));
        }
    }

    private void setDataManagementListeners() {
        Preference importFromDLData = findPreference(PreferencesKeyStore.IMPORT_FROM_DL_DATA_KEY);
        Preference exportFromDLData = findPreference(PreferencesKeyStore.EXPORT_TO_DL_DATA_KEY);
        Preference importData = findPreference(PreferencesKeyStore.IMPORT_DATA_KEY);
        Preference exportData = findPreference(PreferencesKeyStore.EXPORT_DATA_KEY);
        Preference eraseData = findPreference(PreferencesKeyStore.ERASE_DATA_KEY);

        if(importFromDLData != null)
            importFromDLData.setOnPreferenceClickListener(this::onRestoreBackupFromDLClicked);
        if(exportFromDLData != null)
            exportFromDLData.setOnPreferenceClickListener(this::onForceBackupToDLClicked);
        if(importData != null)
            importData.setOnPreferenceClickListener(this::onRestoreBackupClicked);
        if(exportData != null)
            exportData.setOnPreferenceClickListener(this::onForceBackupClicked);
        if(eraseData != null)
            eraseData.setOnPreferenceClickListener(this::onEraseClicked);
    }

    private boolean onPreferenceChanged(Preference preference, Object newValue) {

        String preferenceKey = preference.getKey();

        if(preferenceKey.equals(mPreferenceKeys.ENABLE_CLIP_ACCOMPLISHMENT_KEY)) {
            UserPreferences.setAccomplishmentClipEmptyLines((boolean)newValue);
        }
        else if(preferenceKey.equals(mPreferenceKeys.MAX_DAY_RATING_KEY)) {
            try {
                /* Try and cast string response from preference to an integer value */
                int maxRating = Integer.parseInt((String)newValue);
                UserPreferences.setMaxDayRating(maxRating);
            } catch (ClassCastException | NumberFormatException e) {
                Toast.makeText(getContext(),R.string.setting_update_failed, Toast.LENGTH_LONG).show();
            }
        }
        else if(preferenceKey.equals(mPreferenceKeys.ENABLE_NOTIFICATIONS_KEY)) {
            boolean isEnabled = (boolean)newValue;
            UserPreferences.setEnableNotifications(isEnabled);

            /* Change notification icon depending on if the value was enabled/disabled */
            toggleNotificationIcon(preference);

            Context context = getContext();
            if(context != null) {
                if (isEnabled) DailyReminderAlarmHelper.registerAlarm(getContext(), UserPreferences.getNotificationTime(), true);
                else DailyReminderAlarmHelper.unregisterAlarm(getContext());
            }

        }
        else if(preferenceKey.equals(mPreferenceKeys.NOTIFICATION_TIME_KEY)) {
            String timeString = (String)newValue;
            try {
                LocalTime notificationTime = LocalTime.parse(timeString);

                UserPreferences.setNotificationTime(notificationTime);
            }
            catch(DateTimeParseException e) {
                Toast.makeText(getContext(), R.string.setting_update_failed, Toast.LENGTH_LONG).show();
            }
        }
        else if(preferenceKey.equals(mPreferenceKeys.ENABLE_PASSWORD_PROTECTION)) {
            boolean passwordEnabled = (boolean)newValue;

            UserPreferences.setEnablePasswordProtection(passwordEnabled);
        }
        else if(preferenceKey.equals(mPreferenceKeys.ENABLE_AUTO_LOCK)) {
            boolean autoLockEnabled = (boolean)newValue;

            UserPreferences.setEnableAutoLock(autoLockEnabled);
        }

        return true;
    }

    private void toggleNotificationIcon(Preference preference) {
        boolean enabled = UserPreferences.isEnableNotifications();

        int notificationIcon = enabled ? R.drawable.settings_icon_notifications_on : R.drawable.settings_icon_notifications_off;

        preference.setIcon(notificationIcon);
    }

    private boolean onNotificationTimeSelected(Preference preference) {

        /* Get notification time from preferences and attempt to parse into a LocalTime object */
        LocalTime currentNotificationTime;
        try {
            currentNotificationTime = LocalTime.parse(
                    (String) mUserPreferences.get(mPreferenceKeys.NOTIFICATION_TIME_KEY, "18:00")
            );
        }
        catch (DateTimeParseException e) {
            /* Default to 18:00 if failed to parse */
            currentNotificationTime = LocalTime.of(18,0);
        }

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentNotificationTime.getHour())
                .setMinute(currentNotificationTime.getMinute())
                .build();

        timePicker.addOnPositiveButtonClickListener(picker -> {
            LocalTime selectedTime = LocalTime.of(timePicker.getHour(),timePicker.getMinute());
            String selectedTimeString = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

            /* Set summary text for this preference to the selected time */
            preference.setSummary(selectedTimeString);

            /* Set notification time value in settings */
            if(mUserPreferences != null)
                mUserPreferences.set(mPreferenceKeys.NOTIFICATION_TIME_KEY, selectedTimeString);

            /* Restart notification alarm */
            if(getContext() != null)
                DailyReminderAlarmHelper.updateAlarm(getContext(),selectedTime);
        });

        timePicker.show(getParentFragmentManager(), timePicker.getClass().getSimpleName());

        return true;
    }


    private boolean onForceBackupToDLClicked(Preference preference) {
        Context context = getContext();

        requestStoragePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.setting_force_backup_to_downloads)
                .setMessage(R.string.force_backup_to_dl_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    try {
                        /* Force backup database to downloads */
                        LocalDatabaseIO.backupDbToFile(context, DBConstants.DB_NAME);

                        Toast.makeText(context, R.string.setting_export_backup_success, Toast.LENGTH_SHORT).show();
                    } catch (ExportFailedException e) {
                        /* If failed, alert user and log */
                        Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
                        Toast.makeText(getContext(), String.format(getString(R.string.setting_force_backup_failed), e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();

        return true;
    }

    private boolean onRestoreBackupFromDLClicked(Preference preference) {
        Context context = getContext();

        requestStoragePermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.setting_restore_from_downloads)
                .setMessage(R.string.restore_backup_to_dl_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    Intent fileSelector = new Intent(getActivity(), BackupFileSelectorActivity.class);
                    startActivity(fileSelector);

                    /* Prevent auto lock */
                    TodayI.sIsFileSelecting = true;
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();

        return true;
    }

    private boolean onRestoreBackupClicked(Preference preference) {
        /* Open an alert dialog to confirm if the user wishes to restore from backup */
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.setting_restore_backup)
                .setMessage(R.string.restore_backup_confirmation)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getContext();
                        try {
                            /* Try and load from backup */
                            LocalDatabaseIO.importBackupDb(context);

                            Toast.makeText(context, R.string.setting_restore_backup_success, Toast.LENGTH_SHORT).show();

                        } catch (ImportFailedException e) {
                            /* If failed, alert user and log */
                            Log.w(this.getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));

                            Toast.makeText(context, String.format(getString(R.string.setting_restore_backup_failed), e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();
        return true;
    }

    private boolean onForceBackupClicked(Preference preference) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.setting_force_backup)
                .setMessage(R.string.force_backup_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    try {
                        /* Force backup database to file */
                        LocalDatabaseIO.backupDb(getContext());
                        /* Update last backed up */
                        mUserPreferences.set(getString(R.string.user_prefs_last_backed_up_key),System.currentTimeMillis());

                        /* Update last backed up */
                        setLastBackedUpText();

                        Toast.makeText(getContext(),R.string.setting_force_backup_success, Toast.LENGTH_SHORT).show();
                    } catch (ExportFailedException e) {
                        /* If failed, alert user and log */
                        Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
                        Toast.makeText(getContext(), String.format(getString(R.string.setting_force_backup_failed), e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();

        return true;
    }

    private boolean onEraseClicked(Preference preference) {
        Context context = getContext();
        if(context != null) {
            /* Open an alert dialog to confirm if the user wishes to erase all data */
            new MaterialAlertDialogBuilder(getContext())
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
        }
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
    private void setLastBackedUpText() {
        Preference lastBackedUp = findPreference(PreferencesKeyStore.LAST_BACKED_UP_KEY);
        if(lastBackedUp != null)
            lastBackedUp.setSummary(getLastBackedUpRelativeString());
    }

    /*
    Gets the last time the database was backed up, in a time relative to the current time.
     */
    private String getLastBackedUpRelativeString() {
        /* Get time last backed up from user preferences */
        long lastBackedUp = (Long)mUserPreferences.get(getString(R.string.user_prefs_last_backed_up_key), -1L);

        /* If a valid time was returned from user preferences */
        if (lastBackedUp > 0) {
            /* Return relative time span */
            return DateUtils.getRelativeTimeSpanString(lastBackedUp).toString();
        } else {
            /* If default value (-1) was returned, or an invalid time was returned */
            return "Never";
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

    public void requestStoragePermission(String permission) {;
        int permissionStatus = getContext().checkCallingOrSelfPermission(permission);

        if(permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{permission},
                    1);
        }
    }

}