package io.github.tstewart.todayi.data;

import android.content.SharedPreferences;

import org.threeten.bp.LocalTime;

/*
Helper class for storing application-wide preferences
Also provides functionality to easily add/modify SharedPreferences
 */
public class UserPreferences {

    /* Maximum rating allowed on sliding day rating scale */
    private static int sMaxDayRating = 5;

    /* Has tutorial been shown */
    private static boolean sTutorialShown =  true;

    /* Whether or not to enable daily notifications */
    private static boolean sEnableNotifications = false;

    /* Daily notification time */
    private static LocalTime sNotificationTime = null;

    /* Whether or not to enable gesture navigation */
    private static boolean sEnableGestures = true;

    /* Whether or not to remove empty lines from Accomplishment content */
    private static boolean sAccomplishmentClipEmptyLines = true;

    /* Whether or not to add time picking to Accomplishments */
    private static boolean sEnableTimePicker = true;

    /* Preferences location to check */
    private final SharedPreferences mPreferences;

    public UserPreferences(SharedPreferences prefs) {
        this.mPreferences = prefs;
    }

    /**
     * Get user preference from SharedPreference, or return default
     * @param key Key to return value from
     * @param defaultValue Returned if the provided key had no value
     * @return Value from provided key or default value
     */
    public Object get(String key, Object defaultValue) {
        if(mPreferences != null) {
            Object value = mPreferences.getAll().get(key);
            /* Return default value if not found, otherwise return found value */
            return (value == null) ? defaultValue : value;
        }
        return defaultValue;
    }

    /**
     * Save value to SharedPreference
     * @param key Key to set value for
     * @param value Value to set for provided key
     * @return True if the value was saved, otherwise returns false
     */
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

    /**
     * Set default preference value, if the value doesn't already exist in SharedPreference
     * @param key Key to set default value for
     * @param value Default value
     * @return True if a default value was set (i.e. there was not an existing value for this preference).
     */
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
        return sMaxDayRating;
    }

    public static void setMaxDayRating(int maxDayRating) {
        UserPreferences.sMaxDayRating = maxDayRating;
    }

    public static boolean isTutorialShown() {
        return sTutorialShown;
    }

    public static void setTutorialShown(boolean sTutorialShown) {
        UserPreferences.sTutorialShown = sTutorialShown;
    }

    public static boolean isEnableNotifications() {
        return sEnableNotifications;
    }

    public static void setEnableNotifications(boolean enableNotifications) {
        sEnableNotifications = enableNotifications;
    }

    public static LocalTime getNotificationTime() {
        return sNotificationTime;
    }

    public static void setNotificationTime(LocalTime notificationTime) {
        sNotificationTime = notificationTime;
    }

    public static boolean isGesturesEnabled() {
        return sEnableGestures;
    }

    public static void setEnableGestures(boolean enableGestures) {
        UserPreferences.sEnableGestures = enableGestures;
    }

    public static boolean isAccomplishmentClipEmptyLines() {
        return sAccomplishmentClipEmptyLines;
    }

    public static void setAccomplishmentClipEmptyLines(boolean accomplishmentClipEmptyLines) {
        UserPreferences.sAccomplishmentClipEmptyLines = accomplishmentClipEmptyLines;
    }

    public static boolean isEnableTimePicker() {
        return sEnableTimePicker;
    }

    public static void setEnableTimePicker(boolean enableTimePicker) {
        sEnableTimePicker = enableTimePicker;
    }
}
