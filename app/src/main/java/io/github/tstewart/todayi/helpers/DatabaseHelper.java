package io.github.tstewart.todayi.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.interfaces.DatabaseObject;

/**
 * Helper class. Provides functionality to manage table entries and get table data
 */
public class DatabaseHelper {

    /* Table of database to read/write data from */
    private final String mTable;
    /* Database to read/write data from */
    private SQLiteDatabase mDb;

    public DatabaseHelper(@NonNull String table) {
        this.mTable = table;
    }

    /**
     * Checks if the table has no records
     * @param context Application environment context
     * @return True if the table has no records
     */
    public boolean isEmpty(@NonNull Context context) {
        mDb = getDatabase(context);

        /* Get a cursor with the provided SQL query. */
        Cursor cursor = mDb.rawQuery("select ? from " + mTable, new String[]{DBConstants.COLUMN_ID});

        /* Get the number of returned records. */
        int columnCount = cursor.getColumnCount();
        cursor.close();

        /* If the number of records is none, the table is empty */
        return columnCount == 0;
    }

    /**
     * Get all records from the table
     * @param context Application environment context
     * @return Cursor containing all records for the table
     */
    public Cursor getAll(@NonNull Context context) {
        return getDatabase(context).rawQuery("select * from " + mTable, null);
    }

    /**
     * Insert DatabaseObject into table
     * @param context Application environment context
     * @param object DatabaseObject to be inserted into table
     */
    public void insert(@NonNull Context context, @NonNull DatabaseObject object) {
        mDb = getDatabase(context);
        /* Generate ContentValues from the object's variables */
        ContentValues cv = object.createCV();

        /* If the ContentValues were generated successfully, insert into table */
        if (cv != null) {
            mDb.insert(this.mTable, null, cv);
        }

        /* Close database connection on end. */
        onEnd(mDb);
    }

    /**
     * Update DatabaseObject from table
     * @param context Application environment context
     * @param object DatabaseObject to update existing record with
     * @param whereClause Replace records that match clause
     * @param whereArgs Replaces '?' wildcards in whereClause
     */
    public void update(@NonNull Context context, @NonNull DatabaseObject object, String whereClause, String[] whereArgs) {
        mDb = getDatabase(context);
        ContentValues cv = object.createCV();

        /* If the ContentValues were generated successfully, update record */
        if (cv != null) {
            mDb.update(this.mTable, cv, whereClause, whereArgs);
        }

        /* Close database connection on end. */
        onEnd(mDb);
    }

    /**
     * Delete record from table
     * @param context Application environment context
     * @param whereClause Delete records that match clause
     * @param whereArgs Replaces '?' wildcards in whereClause
     */
    public void delete(@NonNull Context context, String whereClause, String[] whereArgs) {
        mDb = getDatabase(context);

        mDb.delete(this.mTable, whereClause, whereArgs);

        /* Close database connection on end. */
        onEnd(mDb);
    }

    /**
     * Get a database instance from the provided context environment.
     * @param context Application environment context
     * @return Returns a SQLiteDatabase instance
     */
    public SQLiteDatabase getDatabase(Context context) {
        return new Database(context).getReadableDatabase();
    }


    /**
     * Close database and notify event listeners that the database was interacted with
     * @param db Database to be closed
     */
    private void onEnd(SQLiteDatabase db) {
        /* If database is open, close it. */
        if (db != null && db.isOpen()) {
            db.close();
        }
        /* Notify event listeners that the database was interacted with */
        OnDatabaseInteracted.notifyDatabaseInteracted();
    }

}
