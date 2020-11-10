package io.github.tstewart.todayi.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.tstewart.todayi.object.Accomplishment;

public class AccomplishmentQuery implements Query {

    public Cursor getCursor(SQLiteDatabase db, String query, Object[] args) {
        String date = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DBConstants.DATE_FORMAT);

        if(args != null) {
            for(Object arg : args) {
                if(arg instanceof Date) {
                    Date d = (Date)arg;
                    date = dateFormat.format(d);
                }
                if(arg instanceof String) {
                    date = (String) arg;
                }
            }
        }

        return db.rawQuery(query, new String[]{date});
    }

    @Override
    public ArrayList<Accomplishment> getTableResponses(SQLiteDatabase db, String query, Object[] args) {

        ArrayList<Accomplishment> accomplishments = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DBConstants.DATE_FORMAT);

        Cursor cursor = getCursor(db, query, args);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int idResponse = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_ID));
                    String dateResponse = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));
                    String contentResponse = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_CONTENT));

                    try {
                        accomplishments.add(new Accomplishment(idResponse, dateFormat.parse(dateResponse), contentResponse));
                    } catch (ParseException e) {
                        //TODO deal with parse exception
                        Log.e("SQL", e.toString());
                    }

                } while (cursor.moveToNext());
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return accomplishments;
    }

}
