package io.github.tstewart.todayi;

import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.github.tstewart.todayi.object.Accomplishment;
import io.github.tstewart.todayi.sql.AccomplishmentQuery;
import io.github.tstewart.todayi.sql.DBConstants;

// Load accomplishments from SQLite
public class DatabaseAccomplishmentLoader {

    // With no date set, get all accomplishments.
    public ArrayList<Accomplishment> getAccomplishmentsFromDatabase(SQLiteDatabase db, String arg) {
        return new AccomplishmentQuery().getTableResponses(db, DBConstants.ACCOMPLISHMENT_QUERY, new Object[]{arg});
    }

    public ArrayList<Accomplishment> getAccomplishmentsFromDatabase(SQLiteDatabase db, Date date) {
        String dateArg = getDateAsArgument(date);
        return getAccomplishmentsFromDatabase(db, dateArg);
    }

    private String getDateAsArgument(Date date) {
        return new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).format(date);
    }

}
