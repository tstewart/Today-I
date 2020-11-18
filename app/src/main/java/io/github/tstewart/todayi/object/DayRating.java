package io.github.tstewart.todayi.object;

import android.content.ContentValues;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.sql.DBConstants;

// Track opinion of the day from 1-5
// E.g. on a bad day you may vote the day a 1.
public class DayRating implements DatabaseObject {

    private final int MIN_RATING = 1;
    private final int MAX_RATING = 5;

    private Date date;
    private int dayRating;

    public DayRating(@NonNull Date date, int dayRating) {
        this.date = date;
        this.dayRating = dayRating;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        if(dayRating < MIN_RATING) {
            throw new IllegalArgumentException("Rating cannot be lower than " + MIN_RATING + ".");
        }
        else if(dayRating > MAX_RATING) {
            throw new IllegalArgumentException("Rating cannot be higher than " + MAX_RATING + ".");
        }
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
