package io.github.tstewart.todayi.helpers;

import org.ocpsoft.prettytime.PrettyTime;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
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
    public static String getRelativeDaysSinceString(Date date) {
        LocalDate currentDate = LocalDate.now();
        /* Convert Date to LocalDate */
        LocalDate providedDate = Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Calculate days between provided date and the current date
        long daysSince = ChronoUnit.DAYS.between(providedDate, currentDate);

        /* In special cases return day span as a fixed word (Tomorrow/Today/Yesterday), otherwise return a formatted span */
        switch((int) daysSince) {
            case -1: return "Tomorrow";
            case 0: return "Today";
            case 1: return "Yesterday";
            default: return new PrettyTime().format(date);
        }
    }
}
