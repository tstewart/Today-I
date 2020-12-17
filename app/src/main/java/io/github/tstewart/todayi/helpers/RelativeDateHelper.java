package io.github.tstewart.todayi.helpers;

import org.ocpsoft.prettytime.PrettyTime;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Date;

/* Class to calculate days between Date objects and return relative time span strings */
public class RelativeDateHelper {

    /**
     * Get the provided date relative to the current date as a time span label
     * @param date Current date create a relative label from
     * @return Relative time span label
     */
    public static String getRelativeDaysSinceString(LocalDate date) {
        LocalDate currentDate = LocalDate.now();

        /* Calculate days between provided date and the current date */
        long daysSince = ChronoUnit.DAYS.between(date, currentDate);

        /* In special cases return day span as a fixed word (Tomorrow/Today/Yesterday), otherwise return a formatted span */
        switch((int) daysSince) {
            case -1: return "Tomorrow";
            case 0: return "Today";
            case 1: return "Yesterday";
            default:
                /* Get the selected date with a time of 23:59, so that the selected date cannot be interpreted by PrettyTime as a day before or after
                * E.g. if the current date is 1/1/20 12:00 and the selected date is 3/1/2020 11:59,
                * PrettyTime justifies this as in 1 day, not in 2 days */
                LocalDateTime selectedDateLocal = date.atTime(LocalTime.MAX);
                /* PrettyTime requires the use of java Date, so this needs to be converted */
                Date dateConverted = DateTimeUtils.toDate(selectedDateLocal.atZone(ZoneId.systemDefault()).toInstant());
                return new PrettyTime().format(dateConverted);
        }
    }
}
