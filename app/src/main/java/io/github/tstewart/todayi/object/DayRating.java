package io.github.tstewart.todayi.object;

import android.content.ContentValues;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.tstewart.todayi.sql.DBConstants;

// Track opinion of the day from 1-5
// E.g. on a bad day you may vote the day a 1.
public class DayRating extends DatabaseObject {

    private Date date;
    private int dayRating;

    public DayRating(Date date, int dayRating) {

        //Constrain values
        if (dayRating < 1) this.dayRating = 1;
        if (dayRating > 5) this.dayRating = 5;

        this.date = date;
        this.dayRating = dayRating;
    }

    public DayRating(int dayRating) {
        this(new Date(), dayRating);
    }

    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault());

        if(date != null) {
            contentValues.put(DBConstants.COLUMN_DATE, simpleDateFormat.format(date));
        }

        contentValues.put(DBConstants.COLUMN_RATING, dayRating);

        return contentValues;
    }
}
