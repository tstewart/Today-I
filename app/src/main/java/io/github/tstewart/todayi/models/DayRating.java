package io.github.tstewart.todayi.models;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalDate;

import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.interfaces.DatabaseObject;
import io.github.tstewart.todayi.helpers.DateFormatter;

/*
 * Object to store day rating data for a selected date
 * Opinion of the day tracked on a rating scale (MIN_RATING - MAX_RATING)
 */
public class DayRating implements DatabaseObject {

    /* Minimum accepted day rating */
    private static final int MIN_RATING = 1;
    /* Day rated */
    private LocalDate mDate;
    /* Rating */
    private final int mDayRating;
    /* Rating represented as a percentage of the current maximum value */
    private final int mDayRatingPercent;

    public DayRating(@NonNull LocalDate date, int dayRating) {
        this.mDate = date;
        this.mDayRating = dayRating;

        this.mDayRatingPercent = ratingToPercent(dayRating);
    }

    /**
     * Validates the Day Rating object to Database standards.
     * @throws IllegalArgumentException If the validation failed for any reason (e.g. Rating was outside bounds)
     */
    @Override
    public void validate() throws ValidationFailedException {

        int maxRating = UserPreferences.getMaxDayRating();

        /* If rating is less than the minimum accepted */
        if (mDayRating < MIN_RATING) {
            throw new ValidationFailedException("Rating cannot be lower than " + MIN_RATING + ".");
            /* If rating is more than the maximum accepted */
        } else if (mDayRating > maxRating) {
            throw new ValidationFailedException("Rating cannot be higher than " + maxRating + ".");
        }
    }

    /**
     * Get rating as a percentage of the max value
     * Where the min value represents 0%, and the max value represents 100%
     * @param rating Rating to convert to a percentage value
     * @return Rating as a percentage of the max value
     */
    public static int ratingToPercent(int rating) {
        /* Get current max rating */
        int maxRating = UserPreferences.getMaxDayRating();

        /* If the provided rating is greater than 0, it can be converted to a percentage */
        if(rating>0) {
            /* How much one rating of the current max represents
            * E.g. a max of rating of 5 will take 20% per rating */
            int percentagePerRating = 100/maxRating;
            return rating*percentagePerRating;

        } else if(rating == 0) return 0;
        return -1;
    }

    /**
     * Get rating from the percentage representation (of 100%)
     * @param percent Percentage to convert to a rating
     * @return Rating from the provided percentage
     */
    public static int percentToRating(int percent) {
        /* Get current max rating */
        int maxRating = UserPreferences.getMaxDayRating();

        /* If percentage is within bounds */
        if(percent>0 && percent<=100) {
            /* Current rating percent as a value of 100%
            * E.g. a rating of 20% represents 0.2/1 */
            float ratingValue = (float)percent/100;
            /* Get rating as a rounded calculation of the max rating * the rating value */
            int rating = Math.round(ratingValue*maxRating);

            /* Fix for rounding down to a rating of 0
            * E.g. a value of 0.4 should be represented as a rating of 1, not 0. */
            if(rating==0) rating += 1;

            return rating;
        } else if(percent == 0) return 0;
        return -1;
    }

    /**
     * Bundle variables into ContentValues object, for insertion into Database
     * @return ContentValues instance with variables bundled
     */
    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        /* Get date formatter with settings that match the database date format */
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);

        /* Format date and add to content values if not null */
        if (mDate != null) {
            contentValues.put(DBConstants.COLUMN_DATE, dateFormatter.format(mDate));
        }

        contentValues.put(DBConstants.COLUMN_RATING, mDayRatingPercent);

        return contentValues;
    }

    public LocalDate getDate() {
        return mDate;
    }

    public void setDate(LocalDate date) {
        this.mDate = date;
    }
}
