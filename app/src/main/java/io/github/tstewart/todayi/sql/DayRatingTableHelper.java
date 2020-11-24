package io.github.tstewart.todayi.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class DayRatingTableHelper {

    Context context;

    public DayRatingTableHelper(@NonNull Context context) {
        this.context = context;
    }

    public int getRatingOrDefault(Date date) {

        if(this.context != null && date != null) {
            SQLiteDatabase db = new Database(this.context).getReadableDatabase();

            String dateFormatted = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).format(date);
            Cursor cursor = db.rawQuery( DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            if(cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
            }

            cursor.close();
        }
        return -1;
    }

}
