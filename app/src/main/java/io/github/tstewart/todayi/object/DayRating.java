package io.github.tstewart.todayi.object;

import android.content.ContentValues;

import java.util.Date;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.utils.DateFormatter;

// Track opinion of the day from 1-5
// E.g. on a bad day you may vote the day a 1.
public class DayRating implements DatabaseObject {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private Date mDate;
    private int mDayRating;

    public DayRating(@NonNull Date date, int dayRating) {
        this.mDate = date;
        this.mDayRating = dayRating;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        if(mDayRating < MIN_RATING) {
            throw new IllegalArgumentException("Rating cannot be lower than " + MIN_RATING + ".");
        }
        else if(mDayRating > MAX_RATING) {
            throw new IllegalArgumentException("Rating cannot be higher than " + MAX_RATING + ".");
        }
    }

    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);

        if(mDate != null) {
            contentValues.put(DBConstants.COLUMN_DATE, dateFormatter.format(mDate));
        }

        contentValues.put(DBConstants.COLUMN_RATING, mDayRating);

        return contentValues;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public int getDayRating() {
        return mDayRating;
    }

    public void setDayRating(int dayRating) {
        this.mDayRating = dayRating;
    }
}
