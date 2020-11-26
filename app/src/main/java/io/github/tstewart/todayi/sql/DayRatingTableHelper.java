package io.github.tstewart.todayi.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.object.DayRating;
import io.github.tstewart.todayi.utils.DateFormatter;

public class DayRatingTableHelper {

    final Context context;
    final DatabaseHelper helper;

    public DayRatingTableHelper(@NonNull Context context) {
        this.context = context;
        this.helper = new DatabaseHelper(DBConstants.RATING_TABLE);
    }

    public void setRating(Date date, int rating) throws IllegalArgumentException {
        if(date != null) {
            DayRating dayRating = new DayRating(date, rating);

            dayRating.validate();

            SQLiteDatabase db = helper.getDatabase(this.context);
            String dateFormatted = new DateFormatter(DBConstants.DATE_FORMAT).format(date);
            Cursor existingRowCheck = db.rawQuery(DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            if(existingRowCheck.moveToFirst()) {
                helper.update(this.context, dayRating, DBConstants.COLUMN_DATE + "=?", new String[]{dateFormatted});
            }
            else {
                helper.insert(this.context, dayRating);
            }

            existingRowCheck.close();
        }
    }

    public int getRating(Date date, int defaultValue) {

        if(date != null) {
            SQLiteDatabase db = new Database(this.context).getReadableDatabase();

            String dateFormatted = new DateFormatter(DBConstants.DATE_FORMAT).format(date);
            Cursor cursor = db.rawQuery( DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            if(cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
            }

            cursor.close();
        }
        return defaultValue;
    }

}
