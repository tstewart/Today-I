package io.github.tstewart.todayi.object;

import java.util.Date;

// Track opinion of the day from 1-10
// E.g. on a bad day you may vote the day a 1.
public class DayRating {

    private Date date;
    private int dayRating;

    public DayRating(Date date, int dayRating) {

        //Constrain values
        if (dayRating < 1) this.dayRating = 1;
        if (dayRating > 10) this.dayRating = 10;

        this.date = date;
        this.dayRating = dayRating;
    }

    public DayRating(int dayFeeling) {
        this(new Date(), dayFeeling);
    }
}
