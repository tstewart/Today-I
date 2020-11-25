package io.github.tstewart.todayi.sql;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {

    private final String CREATE_TABLE_ACCOMPLISHMENT = "create table " + DBConstants.ACCOMPLISHMENT_TABLE + " "
            + "("
            + DBConstants.COLUMN_ID + " integer primary key autoincrement, "
            + DBConstants.COLUMN_DATE + " string not null, "
            + DBConstants.COLUMN_CONTENT + " text not null"
            + ")";

    private final String CREATE_TABLE_RATINGS = "create table " + DBConstants.RATING_TABLE + " "
            + "("
            + DBConstants.COLUMN_ID + " integer primary key autoincrement, "
            + DBConstants.COLUMN_DATE + " string unique not null, "
            + DBConstants.COLUMN_RATING + " int not null"
            + ")";

    // Initialize db with default settings
    public Database(@Nullable Context context) {
        super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }


    public void eraseAllData(SQLiteDatabase db, String table) {
        db.delete(table, null, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACCOMPLISHMENT);
        db.execSQL(CREATE_TABLE_RATINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
