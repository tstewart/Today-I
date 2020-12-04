package io.github.tstewart.todayi.data;

import android.content.SharedPreferences;

/*
User preferences constants.
TODO For now, this does not link with Android preferences or Settings
 */
public class UserPreferences {

    /* Maximum rating allowed on sliding day rating scale */
    private static final int MAX_DAY_RATING = 5;

    /* Whether or not to remove empty lines from Accomplishment content */
    private static final boolean ACCOMPLISHMENT_REMOVE_EMPTY_LINES = true;

    /* Preferences location to check */
    private final SharedPreferences mPreferences;

    public UserPreferences(SharedPreferences prefs) {
        this.mPreferences = prefs;
    }

    /* Get object from preferences, or default value if not found */
    public Object get(String key, Object defaultValue) {
        if(mPreferences != null) {
            Object value = mPreferences.getAll().get(key);
            /* Return default value if not found, otherwise return found value */
            return (value == null) ? defaultValue : value;
        }
        return defaultValue;
    }

    /* Save value to user preferences.
    * If value couldn't be saved, return false */
    public boolean set(String key, Object value) {
        /* Shared Preferences will only allow saving of these specific types */

        SharedPreferences.Editor editor = mPreferences.edit();

        if(value instanceof String) editor.putString(key, (String) value);
        else if(value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
        else if(value instanceof Long) editor.putLong(key, (Long) value);
        else if(value instanceof Float) editor.putFloat(key, (Float) value);
        else return false;

        editor.apply();
        return true;
    }

    /* Set default preference value, if the value doesn't already exist in preferences */
    public boolean setDefaultValue(String key, Object value) {
        if(!mPreferences.contains(key)) {
            return set(key,value);
        }
        return false;
    }

    public boolean contains(String key) {
        return mPreferences.contains(key);
    }

    public static int getMaxDayRating() {
        return MAX_DAY_RATING;
    }

    public static boolean shouldRemoveEmptyLines() {
        return ACCOMPLISHMENT_REMOVE_EMPTY_LINES;
    }
}
