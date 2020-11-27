package io.github.tstewart.todayi.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

// TODO In later versions, SimpleDateFormat should be replaced app wide with DateTimeFormatter
// TODO replace Date object support?
/**
 * Helper class. Format Date object to formatted date, or vice versa
 */
public class DateFormatter {

    // Date formatter
    private SimpleDateFormat mDateFormatter;

    // Private constructor prevents initialisation
    private DateFormatter() {
    }

    public DateFormatter(String dateFormat) {
        // Initialise date formatter with default locale
        this.mDateFormatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
    }

    /**
     * Formats provided Date to string that matches the dateFormat.
     * @param date Date to be formatted
     * @return Formatted date
     */
    public String format(Date date) {
        return mDateFormatter.format(date);
    }

    /**
     * Formats the date, returning days with day indicators attached (1st, 2nd, 3rd, etc)
     * @param date Date to be formatted
     * @return Formatted date with day indicators
     */
    public String formatWithDayIndicators(Date date) {
        // Initialise indicator formatter as a copy of the original formatter
        SimpleDateFormat indicatorDateFormatter = mDateFormatter;

        // Get date format pattern
        String dateFormat = mDateFormatter.toPattern();
        // Get the position of the date option in the formatter pattern
        int indicatorPosition = dateFormat.indexOf("d ");

        // If the date option exists
        if (indicatorPosition >= 0) {

            // Get this date's day of the month
            int day = getDayOfMonth(date);

            // Append ordinal (number indicator) to after the date option in the format pattern
            // E.g. The pattern MM/dd with a date of the 1st April becomes MM/dd'st'
            // The ordinal is placed in quotes so it is not treated as a format option
            dateFormat = dateFormat.substring(0, indicatorPosition)
                    + "'"
                    + getOrdinal(day)
                    + "'"
                    + dateFormat.substring(indicatorPosition + 1);
        }

        // Apply pattern to formatter
        indicatorDateFormatter.applyPattern(dateFormat);
        // Format date
        return indicatorDateFormatter.format(date);
    }

    /**
     * Converts a date string and the pattern provided in the constructor to a valid date object
     * @param dateString Date in the form of the pattern provided in the constructor
     * @return Returns a parsed date
     * @throws ParseException Thrown if the parser could not create a date from the provided string.
     */
    public Date parse(String dateString) throws ParseException {
        return mDateFormatter.parse(dateString);
    }

    /**
    getOrdinal provided by:
    https://stackoverflow.com/a/6810409

     * Get the ordinal for the provided number.
     * @param i Number to get the ordinal for
     * @return Number ordinal (e.g. 1 = 1st, 2 = 2nd, 3 = 3rd)
     */
    private String getOrdinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                // 11th, 12th and 13th are edge cases, but the default should work for other numbers
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    /**
     * Get the day of the month of the provided date
     */
    private int getDayOfMonth(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }
}
