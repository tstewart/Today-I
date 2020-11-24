package io.github.tstewart.todayi;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.DatabaseHelper;

import static android.content.SharedPreferences.Editor;

public class TodayI extends Application {
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    private final int BACKUP_EVERY_HOURS = 24;

    @Override
    public void onCreate() {
        super.onCreate();

        // Database auto backup management
        Context context = getApplicationContext();
        boolean shouldBackup = false;

        if(context != null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE);

            if (sharedPrefs.contains(getString(R.string.user_prefs_last_backed_up_key))) {
                long lastBackedUp = sharedPrefs.getLong(getString(R.string.user_prefs_last_backed_up_key), -1);
                /* If the default value is selected, the app should attempt to backup data.
                If the last backup time is in the future (due to time modifications then we should backup anyway. (This may be changed)
                Additionally, If the app hasn't backed up within the number of hours defined in BACKUP_EVERY_HOURS, the app should attempt to backup data */
                if(lastBackedUp<=0
                        || lastBackedUp > System.currentTimeMillis()
                        || hasNotBackedUpWithinHours(lastBackedUp)) shouldBackup = true;

            /* If the key couldn't be found, a backup should be run if the app contains at least one database entry. */
            } else if(databasesEmpty(context)) {
                shouldBackup = true;
            }

            if(shouldBackup) {
                //TODO BACKUP
                sharedPrefs.edit()
                        .putLong(getString(R.string.user_prefs_last_backed_up_key), System.currentTimeMillis())
                        .apply();
                Log.i(CLASS_LOG_TAG, "Application data backed up!");
            }
            else {
                Log.i(CLASS_LOG_TAG, "Application data did not need to backup.");
            }
        }
    }

    private boolean databasesEmpty(@NonNull  Context context) {
        DatabaseHelper accomplishmentHelper = new DatabaseHelper(DBConstants.ACCOMPLISHMENT_TABLE);
        DatabaseHelper ratingHelper = new DatabaseHelper(DBConstants.RATING_TABLE);

        return accomplishmentHelper.isEmpty(context) && ratingHelper.isEmpty(context);
    }

    private boolean hasNotBackedUpWithinHours(long lastBackedUp) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());

        c.add(Calendar.HOUR, -BACKUP_EVERY_HOURS);

        return lastBackedUp <= c.getTime().getTime();
    }
}
