package io.github.tstewart.todayi.models;

import android.content.ContentValues;

import java.util.Date;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.interfaces.DatabaseObject;
import io.github.tstewart.todayi.helpers.DateFormatter;

/*
 * Object to store day rating data for a selected date
 * Opinion of the day tracked on a rating scale (MIN_RATING - MAX_RATING)
 */
public class DayRating implements DatabaseObject {

    // Minimum accepted day rating
    private static final int MIN_RATING = 1;
    // Maximum accepted day rating
    private static final int MAX_RATING = 5;
    // Day rated
    private Date mDate;
    // Rating
    private int mDayRating;

    public DayRating(@NonNull Date date, int dayRating) {
        this.mDate = date;
        this.mDayRating = dayRating;
    }

    /**
     * Validates the Day Rating object to Database standards.
     * @throws IllegalArgumentException If the validation failed for any reason (e.g. Rating was outside bounds)
     */
    @Override
    public void validate() throws IllegalArgumentException {
        // If rating is less than the minimum accepted
        if (mDayRating < MIN_RATING) {
            throw new IllegalArgumentException("Rating cannot be lower than " + MIN_RATING + ".");
        // If rating is more than the maximum accepted
        } else if (mDayRating > MAX_RATING) {
            throw new IllegalArgumentException("Rating cannot be higher than " + MAX_RATING + ".");
        }
    }

    /**
     * Bundle variables into ContentValues object, for insertion into Database
     * @return ContentValues instance with variables bundled
     */
    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        // Get date formatter with settings that match the database date format
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);

        // Format date and add to content values if not null
        if (mDate != null) {
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
