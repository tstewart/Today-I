package io.github.tstewart.todayi.helpers.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalDate;

import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.db.DatabaseHelper;
import io.github.tstewart.todayi.models.DayRating;

/**
 * Helper class. Provides functionality to insert, update, and remove ratings from the database
 */
public class DayRatingTableHelper extends DatabaseHelper {

    public DayRatingTableHelper(@NonNull Context context) {
        /* Default to generating a DatabaseHelper for the Ratings table */
        super(context, DBConstants.RATING_TABLE);
    }

    /**
     * Set rating for the provided date
     * @param date Date to set rating for
     * @param rating Rating to set for provided date
     * @throws IllegalArgumentException If the rating is invalid (e.g. the rating is outside the provided bounds)
     */
    public void setRating(LocalDate date, int rating) throws ValidationFailedException {
        if (date != null) {
            DayRating dayRating = new DayRating(date, rating);

            /* Validate the day rating object, to check the variables for invalid values */
            dayRating.validate();

            SQLiteDatabase db = getDatabase();
            /* Format the date to database requirements */
            String dateFormatted = new DateFormatter(DBConstants.DATE_FORMAT_NO_TIME).format(date);
            /* Get a cursor with the provided date, to check if the Ratings table already contains a value for this date */
            Cursor existingRowCheck = db.rawQuery(DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            /* If the cursor is able to move to a record for this date, then a record already exists */
            if (existingRowCheck.moveToFirst()) {
                /* Update the existing record */
                super.update( dayRating, DBConstants.COLUMN_DATE + "=?", new String[]{dateFormatted});
            } else {
                /* Insert a new record for this date */
                super.insert(dayRating);
            }

            existingRowCheck.close();
        }
    }

    /**
     * Get a rating for the provided date
     * @param date Date to get rating for
     * @param defaultValue Default value, returned if there is no record for this date
     * @return Returns the rating for the date, or a defaultValue if no rating was recorded for the provided date
     */
    public int getRating(LocalDate date, int defaultValue) {
        int rating = defaultValue;

        if (date != null) {
            SQLiteDatabase db = getDatabase();

            /* Format the date to database requirements */
            String dateFormatted = new DateFormatter(DBConstants.DATE_FORMAT_NO_TIME).format(date);
            /* Get a cursor with the provided date, to check if the Ratings table contains a value for this date */
            Cursor cursor = db.rawQuery(DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            /* If the cursor is able to move to a record for this date, then the record exists */
            if (cursor.moveToFirst()) {
                /* Get and return the rating for this date */
                rating = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
            }

            cursor.close();
        }
        /* If a value was not returned, return the default value */
        return rating;
    }

}
