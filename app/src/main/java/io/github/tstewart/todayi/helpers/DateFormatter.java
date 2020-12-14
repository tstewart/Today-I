package io.github.tstewart.todayi.helpers;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class. Format Date object to formatted date, or vice versa
 */
public class DateFormatter {

    /* Date formatter */
    private DateTimeFormatter mDateFormatter;

    /* Formatter pattern */
    private String mFormatPattern;

    /* Private constructor prevents initialisation */
    private DateFormatter() {
    }

    public DateFormatter(String dateFormat) {
        /* Initialise date formatter with default locale */
        this.mDateFormatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault());
        this.mFormatPattern = dateFormat;
    }

    /**
     * Formats provided Date to string that matches the dateFormat.
     * @param date Date to be formatted
     *             TemporalAccessor is used to allow for LocalDate, LocalDateTime support
     * @return Formatted date
     */
    public String format(TemporalAccessor date) {
        return mDateFormatter.format(date);
    }

    /**
     * Formats the date, returning days with day indicators attached (1st, 2nd, 3rd, etc)
     * @param date Date to be formatted
     *             TemporalAccessor is used to allow for LocalDate, LocalDateTime support
     * @return Formatted date with day indicators
     */
    public String formatWithDayIndicators(TemporalAccessor date) {
        /* Get date format pattern */
        if(mFormatPattern != null) {
            /* Get the position of the date option in the formatter pattern */
            int indicatorPosition = mFormatPattern.indexOf("d ");

            /* If the date option exists */
            if (indicatorPosition >= 0) {

                /* Get this date's day of the month */
                int day = date.get(ChronoField.DAY_OF_MONTH);
            /*
             Append ordinal (number indicator) to after the date option in the format pattern
             E.g. The pattern MM/dd with a date of the 1st April becomes MM/dd'st'
             The ordinal is placed in quotes so it is not treated as a format option
            */
                String ordinalDateFormat = mFormatPattern.substring(0, indicatorPosition)
                        + "'"
                        + getOrdinal(day)
                        + "'"
                        + mFormatPattern.substring(indicatorPosition + 1);

                /* Initialise indicator formatter with new pattern */
                DateTimeFormatter indicatorDateFormatter = DateTimeFormatter.ofPattern(ordinalDateFormat, Locale.getDefault());
                /* Format date */
                return indicatorDateFormatter.format(date);
            }
        }
        return null;
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
                /* 11th, 12th and 13th are edge cases, but the default should work for other numbers */
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }
}
