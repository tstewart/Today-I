package io.github.tstewart.todayi.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;

/*
Helper class to return a cursor with a provided SQL query
TODO remove this class, or generify
 */
public class CursorLoader {

    /**
     * Get a cursor for the provided query.
     * @param db Database to query
     * @param query Query to run in the database. Requires one date argument
     * @param date Date argument for the query
     * @return A cursor of results for the provided query.
     */
    public static Cursor getCursorForDateQuery(SQLiteDatabase db, String query, Date date) {
        DateFormatter dateFormat = new DateFormatter(DBConstants.DATE_FORMAT);
        String dateQuery = "";

        // If the date is not null, format it to the default database format
        if (date != null) {
            dateQuery = dateFormat.format(date);
        }

        return db.rawQuery(query, new String[]{dateQuery});
    }
}
