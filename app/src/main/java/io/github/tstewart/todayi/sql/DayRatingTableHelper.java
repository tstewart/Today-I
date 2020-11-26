package io.github.tstewart.todayi.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.utils.DateFormatter;

public class DayRatingTableHelper {

    final Context context;

    public DayRatingTableHelper(@NonNull Context context) {
        this.context = context;
    }

    public int getRatingOrDefault(Date date) {

        if(date != null) {
            SQLiteDatabase db = new Database(this.context).getReadableDatabase();

            String dateFormatted = new DateFormatter(DBConstants.DATE_FORMAT).format(date);
            Cursor cursor = db.rawQuery( DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            if(cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
            }

            cursor.close();
        }
        return -1;
    }

}
