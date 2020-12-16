package io.github.tstewart.todayi.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.LocalDatabaseIO;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ExportFailedException;
import io.github.tstewart.todayi.errors.ImportFailedException;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.notifications.DailyReminderAlarmHelper;

/*
Options Activity contains options for the application including data management, and user preferences
 */
public class OptionsActivity extends AppCompatActivity {
    /*
     Log tag, used for Logging
     Represents class name
    */
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    /* Is Debug Activity access enabled */
    private static final boolean DEBUG_ENABLED = true;
    /* Number of taps on the version TextView required to open debug menu */
    private static final int DEBUG_ACTIVITY_TAP_REQUIREMENT = 6;

    /* User preferences */
    private UserPreferences mPreferences;

    /* Current tap count */
    private int mDebugActivityTapCount = 0;
    /* Toast alerts user how many clicks they need to access debug menu */
    private Toast mClicksToDebugToast;

    /* View currently being pressed by user */
    private View mSelectedView = null;

    /* Notifies the user when the application was last backed up */
    TextView mLastBackedUpTv;
    /*
     Notifies the user of the current application version
     Also used to access the debug menu, by clicking on it (if enabled)
    */
    TextView mCurrentVersionTv;

    SwitchMaterial mGesturesSw;
    /* Switch to toggle gesture navigation */
    /* Switch to toggle auto clipping of blank lines from Accomplishments */
    SwitchMaterial mClipAccomplishments;

    /* Switch to toggle daily notifications */
    SwitchMaterial mNotificationsSw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE);
        this.mPreferences = new UserPreferences(sharedPrefs);

        LinearLayout mainLayout = findViewById(R.id.linearLayoutOptions);

        if(mainLayout != null) {
            for (View v : mainLayout.getTouchables()) {
                if(v instanceof Button) {
                    v.setOnTouchListener(this::onTouchEvent);
                    v.setOnClickListener(this::onButtonClicked);
                }
            }
        }

        /* Get TextViews that have functionality from layout */
        mLastBackedUpTv = findViewById(R.id.textViewLastBackedUp);
        mCurrentVersionTv = findViewById(R.id.textViewAboutVersion);

        mNotificationsSw = findViewById(R.id.switchEnableDailyNotifications);
        mGesturesSw = findViewById(R.id.switchEnableSwipeGesture);
        mClipAccomplishments = findViewById(R.id.switchClipAccomplishments);

        if (mCurrentVersionTv != null) {
            /* Get current version from PackageInfo */
            String currentVersion = getCurrentVersion();
            mCurrentVersionTv.setText(String.format(getString(R.string.about_version), currentVersion));

            /* Add onClick listener to access debug, if this functionality is enabled */
            if(DEBUG_ENABLED)
                mCurrentVersionTv.setOnClickListener(this::onDebugViewClickedListener);
        }

        /* Set if options switches are flipped or not */
        /* Set switch toggle listeners */
        if(mNotificationsSw != null) {
            boolean notificationsEnabled = (boolean)mPreferences.get(getString(R.string.user_prefs_notifications_enabled), false);

            mNotificationsSw.setChecked(notificationsEnabled);
            mNotificationsSw.setOnClickListener(this::onNotificationsSwitchClicked);
        }
        if(mGesturesSw != null) {
            boolean gesturesSelected = (boolean)mPreferences.get(getString(R.string.user_prefs_gestures_enabled), false);
            mGesturesSw.setChecked(gesturesSelected);
            mGesturesSw.setOnClickListener(this::onGestureSwitchClicked);
        }
        if(mClipAccomplishments != null) {
            boolean clipAccomplishmentsSelected = (boolean) mPreferences.get(getString(R.string.user_prefs_clip_empty_lines), false);
            mClipAccomplishments.setChecked(clipAccomplishmentsSelected);
            mClipAccomplishments.setOnClickListener(this::onClipAccomplishmentSwitchClicked);
        }

        /* Set top bar title */
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.activity_settings);
    }

    /* If a button is touched, set that button as the selected view until that button is released */
    private boolean onTouchEvent(View view, MotionEvent event) {
        /* If no button is held or this button is held */
        if(mSelectedView == null || view == mSelectedView) {
            /* If the button was just held */
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                mSelectedView = view;
            /* If the button was released, stop preventing clicks */
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                mSelectedView = null;
            }
            /* Touch event has not been captured, allowing other onTouchEvents to use this button */
            return false;
        }
        /* Touch event has been captured, preventing other onTouchEvents from using this button */
        return true;
    }

    /* Control for all buttons in the Activity, disables clicking multiple buttons at the same time */
    public void onButtonClicked(View view) {
        int id = view.getId();

        if(id == R.id.buttonRestoreBackup)
            this.onRestoreBackupButtonClicked();
        else if(id == R.id.buttonForceBackup)
            this.onForceBackupButtonClicked();
        else if(id == R.id.buttonEraseAll)
            this.eraseButtonClicked();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        /* Update last backed up TextView every time this Activity is un-paused */
        if (mLastBackedUpTv != null) {
            setLastBackedUpText();
        }

        /* Reset selected view */
        mSelectedView = null;
    }

    /* Called if a button is pressed in the top bar */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* If the item selected was the "back" button */
        if (item.getItemId() == android.R.id.home) {
            /* Alert the Activity that launched this Activity that it will not receive a response. */
            returnWithResponse(Activity.RESULT_CANCELED);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the current application version from PackageInfo
     * @return The current application version
     */
    private String getCurrentVersion() {
        Context appContext = getApplicationContext();
        if (appContext != null) {
            try {
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                return pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        /* If version could not be received from PackageInfo, return default value */
        return "Unknown";
    }

    /*
    Set last backed up label to a relative text string showing the last time since backing up the database
     */
    private void setLastBackedUpText() {
        mLastBackedUpTv.setText(String.format(getText(R.string.last_backed_up).toString(), getLastBackedUpRelativeString()));
    }

    /*
    Gets the last time the database was backed up, in a time relative to the current time.
     */
    private String getLastBackedUpRelativeString() {
        /* Get time last backed up from user preferences */
        long lastBackedUp = (Long)mPreferences.get(getString(R.string.user_prefs_last_backed_up_key), -1);

        /* If a valid time was returned from user preferences */
        if (lastBackedUp > 0) {
            /* Return relative time span */
            return DateUtils.getRelativeTimeSpanString(lastBackedUp).toString();
        } else {
            /* If default value (-1) was returned, or an invalid time was returned */
            return "Unknown";
        }
    }

    private void onNotificationsSwitchClicked(View view) {
        boolean isNotificationsEnabled = mNotificationsSw.isChecked();

        mPreferences.set(getString(R.string.user_prefs_notifications_enabled),isNotificationsEnabled);
        UserPreferences.setEnableNotifications(isNotificationsEnabled);

        DailyReminderAlarmHelper alarmHelper = new DailyReminderAlarmHelper();
        if(isNotificationsEnabled) alarmHelper.registerAlarm(this, UserPreferences.getNotificationTime());
        else alarmHelper.unregisterAlarm(this);
    }

    private void onGestureSwitchClicked(View view) {
        mPreferences.set(getString(R.string.user_prefs_gestures_enabled), mGesturesSw.isChecked());
        UserPreferences.setEnableGestures(mGesturesSw.isChecked());
    }

    private void onClipAccomplishmentSwitchClicked(View view) {
        mPreferences.set(getString(R.string.user_prefs_clip_empty_lines), mClipAccomplishments.isChecked());
        UserPreferences.setAccomplishmentClipEmptyLines(mClipAccomplishments.isChecked());
    }

    private void onDebugViewClickedListener(View view) {
        /* Increment count of how many times the version TextView has been clicked so far */
        mDebugActivityTapCount++;

        /* If the number of clicks required to open DebugActivity has been met */
        if (mDebugActivityTapCount >= DEBUG_ACTIVITY_TAP_REQUIREMENT) {
            /* Start DebugActivity */
            Intent intent = new Intent(this, DebugActivity.class);
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
            mClicksToDebugToast = Toast.makeText(this, clicksToDebugMenu, Toast.LENGTH_SHORT);
            mClicksToDebugToast.show();
        }
    }

    private void onRestoreBackupButtonClicked() {
        /* Open an alert dialog to confirm if the user wishes to restore from backup */
        new AlertDialog.Builder(this)
                .setTitle(R.string.button_restore_backup)
                .setMessage(R.string.restore_backup_confirmation)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getApplicationContext();
                        try {
                            /* Try and load from backup */
                            LocalDatabaseIO.importBackupDb(context, DBConstants.DB_NAME);

                            /* Exit options with result code. */
                            returnWithResponse(Activity.RESULT_OK);
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

    }

    private void onForceBackupButtonClicked() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.button_force_backup)
                .setMessage(R.string.force_backup_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    try {
                        /* Force backup database to file */
                        LocalDatabaseIO.backupDb(this, DBConstants.DB_NAME);
                        /* Update last backed up */
                        mPreferences.set(getString(R.string.user_prefs_last_backed_up_key),System.currentTimeMillis());
                    } catch (ExportFailedException e) {
                        /* If failed, alert user and log */
                        Log.w(CLASS_LOG_TAG, e.getMessage(), e);
                        Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();

        /* Update last backed up text */
        setLastBackedUpText();
    }

    private void eraseButtonClicked() {
        /* Open an alert dialog to confirm if the user wishes to erase all data */
        new AlertDialog.Builder(this)
                .setTitle(R.string.erase_all_warning_dialog_title)
                .setMessage(R.string.erase_all_warning_dialog_message)
                .setPositiveButton(R.string.button_yes, (dialogInterface, which) -> {
                    Database database = Database.getInstance(this);
                    SQLiteDatabase db = database.getWritableDatabase();

                    /* Delete data from both tables */
                    database.eraseAllData(db, DBConstants.ACCOMPLISHMENT_TABLE);
                    database.eraseAllData(db, DBConstants.RATING_TABLE);

                    Toast.makeText(this, R.string.erase_all_confirmed, Toast.LENGTH_LONG).show();

                    /* Return to parent Activity */
                    returnWithResponse(Activity.RESULT_OK);
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();
    }

    /*
    Return to parent Activity with response code
     */
    private void returnWithResponse(int response) {
        Intent returnIntent = new Intent();
        setResult(response, returnIntent);
        finish();
    }

}
