package io.github.tstewart.todayi;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.tstewart.todayi.sql.DBConstants;

public class AccomplishmentCursorLoader {

    public static Cursor getCursor(SQLiteDatabase db, String query, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault());
        String dateQuery = "";

        if (date != null) {
            dateQuery = dateFormat.format(date);
        }

        return db.rawQuery(query, new String[]{dateQuery});
    }
}
