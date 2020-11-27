package io.github.tstewart.todayi.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;

public class AccomplishmentCursorLoader {

    public static Cursor getCursor(SQLiteDatabase db, String query, Date date) {
        DateFormatter dateFormat = new DateFormatter(DBConstants.DATE_FORMAT);
        String dateQuery = "";

        if (date != null) {
            dateQuery = dateFormat.format(date);
        }

        return db.rawQuery(query, new String[]{dateQuery});
    }
}
