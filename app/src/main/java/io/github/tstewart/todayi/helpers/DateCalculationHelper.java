package io.github.tstewart.todayi.helpers;

import java.util.Calendar;
import java.util.Date;

/* Add/Subtract from Dates, calculate time between Dates */
public class DateCalculationHelper {

    /**
     * Add a number of a date unit to a provided date
     * @param date Date to add to
     * @param unit Unit to add (Uses java.util.Calendar units
     * @param amount Amount of the provided unit to add
     * @return Date with units added
     */
    public static Date addToDate(Date date, int unit, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(unit, amount);
        return c.getTime();
    }

    /**
     * Subtract a number of a date unit from a provided date
     * @param date Date to subtract from
     * @param unit Unit to subtract (Uses java.util.Calendar units
     * @param amount Amount of the provided unit to subtract
     * @return Date with units subtracted
     */
    public static Date subtractFromDate(Date date, int unit, int amount) {
        if(amount>0) return addToDate(date,unit,-amount);
        else return addToDate(date,unit,amount);
    }
}
