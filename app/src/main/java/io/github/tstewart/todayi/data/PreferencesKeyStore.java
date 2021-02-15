package io.github.tstewart.todayi.data;

import android.content.Context;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.R;

/* Helper class. Searches application resource files for the keys to specific preferences
* Certain preference keys are always the same, and these keys are static */
public class PreferencesKeyStore {

    /* Enable gestures key */
    public final String ENABLE_GESTURES_KEY;
    /* Enable clip accomplishments key */
    public final String ENABLE_CLIP_ACCOMPLISHMENT_KEY;
    /* Maximum day ratings key */
    public final String MAX_DAY_RATING_KEY;
    /* Enable notifications key */
    public final String ENABLE_NOTIFICATIONS_KEY;
    /* Notification time key */
    public final String NOTIFICATION_TIME_KEY;
    /* Import data key */
    public static final String IMPORT_DATA_KEY = "import";
    /* Export data key */
    public static final String EXPORT_DATA_KEY = "export";
    /* Erase data key */
    public static final String ERASE_DATA_KEY = "erase";
    /* Last backed up key */
    public static final String LAST_BACKED_UP_KEY = "last_backed_up";
    /* Version key */
    public static final String VERSION_KEY = "version";

    public PreferencesKeyStore(@NonNull Context context) {

        /* Search application resources for these keys  */
        this.ENABLE_GESTURES_KEY = context.getString(R.string.user_prefs_gestures_enabled);
        this.ENABLE_CLIP_ACCOMPLISHMENT_KEY = context.getString(R.string.user_prefs_clip_empty_lines);
        this.MAX_DAY_RATING_KEY = context.getString(R.string.user_prefs_num_day_ratings);
        this.ENABLE_NOTIFICATIONS_KEY = context.getString(R.string.user_prefs_notifications_enabled);
        this.NOTIFICATION_TIME_KEY = context.getString(R.string.user_prefs_notification_time);
    }
}
