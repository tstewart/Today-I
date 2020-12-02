package io.github.tstewart.todayi.data;

/*
User preferences constants.
TODO For now, this does not link with Android preferences or Settings
 */
public class UserPreferences {

    /* Maximum rating allowed on sliding day rating scale */
    private static final int MAX_DAY_RATING = 5;

    /* Whether or not to remove empty lines from Accomplishment content */
    private static final boolean ACCOMPLISHMENT_REMOVE_EMPTY_LINES = true;

    private UserPreferences(){}

    public static int getMaxDayRating() {
        return MAX_DAY_RATING;
    }

    public static boolean shouldRemoveEmptyLines() {
        return ACCOMPLISHMENT_REMOVE_EMPTY_LINES;
    }
}
