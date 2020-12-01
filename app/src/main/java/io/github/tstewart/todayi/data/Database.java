package io.github.tstewart.todayi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
            + DBConstants.COLUMN_CONTENT + " text not null"
            + ")";

    /* SQL command to create Ratings table */
    private static final String CREATE_TABLE_RATINGS = "create table " + DBConstants.RATING_TABLE + " "
            + "("
            + DBConstants.COLUMN_ID + " integer primary key autoincrement, "
            + DBConstants.COLUMN_DATE + " string unique not null, " /* Date must be unique, as only one rating can represent a date */
            + DBConstants.COLUMN_RATING + " int not null"
            + ")";

    private static Database mInstance = null;

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
        db.execSQL(CREATE_TABLE_ACCOMPLISHMENT);
        db.execSQL(CREATE_TABLE_RATINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO purge existing database and backup
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
