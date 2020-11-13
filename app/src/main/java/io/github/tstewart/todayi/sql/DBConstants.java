package io.github.tstewart.todayi.sql;

import android.content.ContentValues;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class DBConstants {

    public static final String DB_NAME = "accomplishment_db";
    public static final int DB_VERSION = 1;

    public static final String ACCOMPLISHMENT_TABLE = "accomplishments";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CONTENT = "content";

    public static final String RATING_TABLE = "dayratings";
    public static final String COLUMN_RATING = "rating";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String ACCOMPLISHMENT_QUERY = "select * from " + ACCOMPLISHMENT_TABLE + " where date = ?";
    public static final String ACCOMPLISHMENT_DATE_GROUP_QUERY = "select " + COLUMN_DATE + "  from " + ACCOMPLISHMENT_TABLE + " group by " + COLUMN_DATE;

    private DBConstants() {
    }

    public static ContentValues getContentValues(@NonNull String content, @NonNull Date date) {
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COLUMN_CONTENT, content);
        cv.put(DBConstants.COLUMN_DATE, new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).format(date));

        return cv;
    }
}
