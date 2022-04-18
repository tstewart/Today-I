package io.github.tstewart.todayi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
    Database class, creates the tables inside the database and provides helper functionality
 */
public class Database extends SQLiteOpenHelper {

    /* SQL command to create Accomplishment table */
    private static final String CREATE_TABLE_ACCOMPLISHMENT = "create table " + DBConstants.ACCOMPLISHMENT_TABLE + " "
            + "("
            + DBConstants.COLUMN_ID + " integer primary key autoincrement, "
            + DBConstants.COLUMN_DATE + " string not null, "
            + DBConstants.COLUMN_TITLE + " text not null,"
            + DBConstants.COLUMN_DESCRIPTION + " text"
            + ")";

    /* SQL command to create Ratings table */
    private static final String CREATE_TABLE_RATINGS = "create table " + DBConstants.RATING_TABLE + " "
            + "("
            + DBConstants.COLUMN_ID + " integer primary key autoincrement, "
            + DBConstants.COLUMN_DATE + " string unique not null, " /* Date must be unique, as only one rating can represent a date */
            + DBConstants.COLUMN_RATING + " int not null"
            + ")";

    private static Database mInstance = null;

    /* Restrict database access to a single instance
    * Prevents threads opening a database instance and not closing it */
    public static Database getInstance(@NonNull Context context) {
        if(mInstance == null) {
            mInstance = new Database(context.getApplicationContext());
        }
        return mInstance;
    }

    /* Initialize database with default settings */
    private Database(@Nullable Context context) {
        super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /* Run SQL command to create Accomplishment table */
        db.execSQL(CREATE_TABLE_ACCOMPLISHMENT);
        /* Run SQL command to create Ratings table */
        db.execSQL(CREATE_TABLE_RATINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* If upgrading database, delete Accomplishment table and try and recreate it
        * Fixes old database models not containing proper time-based Accomplishment data */
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.ACCOMPLISHMENT_TABLE);
            db.execSQL(CREATE_TABLE_ACCOMPLISHMENT);
        }
        catch(SQLiteException e) {
            Log.w(Database.class.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Deletes all data from the provided table
     * @param db Database to delete from
     * @param table Table to delete from
     */
    public void eraseAllData(SQLiteDatabase db, String table) {
        db.delete(table, null, null);
    }
}
