package io.github.tstewart.todayi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

// TODO In later vesions, SimpleDateFormat should be replaced app wide with DateTimeFormatter
public class DateFormatter {

    private SimpleDateFormat mDateFormatter;

    private DateFormatter() {
    }

    public DateFormatter(String dateFormat) {
        this.mDateFormatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
    }

    public String format(Date date) {
        return mDateFormatter.format(date);
    }

    // Formats the date, returning days with day indicators attached (1st, 2nd, 3rd, etc)
    public String formatWithDayIndicators(Date date) {
        SimpleDateFormat indicatorDateFormatter = mDateFormatter;

        String dateFormat = mDateFormatter.toPattern();
        int indicatorPosition = dateFormat.indexOf("d ");

        if (indicatorPosition >= 0) {

            int day = getDayOfMonth(date);

            dateFormat = dateFormat.substring(0, indicatorPosition)
                    + "'"
                    + getOrdinal(day)
                    + "'"
                    + dateFormat.substring(indicatorPosition + 1);
        }

        indicatorDateFormatter.applyPattern(dateFormat);
        return indicatorDateFormatter.format(date);
    }

    public Date parse(String dateString) throws ParseException {
        return mDateFormatter.parse(dateString);
    }

    /*
        Author: Bohemian
        https://stackoverflow.com/a/6810409
     */
    private String getOrdinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    private int getDayOfMonth(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }
}
