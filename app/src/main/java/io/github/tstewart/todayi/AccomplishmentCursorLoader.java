package io.github.tstewart.todayi;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.utils.DateFormatter;

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
