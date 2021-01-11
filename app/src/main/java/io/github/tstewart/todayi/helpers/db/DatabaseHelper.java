package io.github.tstewart.todayi.helpers.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.threeten.bp.LocalDate;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.interfaces.DatabaseObject;

/**
 * Helper class. Provides functionality to manage table entries and get table data
 */
public class DatabaseHelper {

    /* Application environment context */
    final Context mContext;
    /* Table of database to read/write data from */
    private final String mTable;
    /* Database to read/write data from */
    private SQLiteDatabase mDb;

    public DatabaseHelper(@NonNull Context context, @NonNull String table) {
        this.mContext = context;
        this.mTable = table;
    }

    /**
     * Checks if the table has no records
     * @return True if the table has no records
     */
    public boolean isEmpty() {
        mDb = getDatabase();

        /* Get a cursor with the provided SQL query. */
        Cursor cursor = mDb.rawQuery("select ? from " + mTable, new String[]{DBConstants.COLUMN_ID});

        /* Get the number of returned records. */
        int rowCount = cursor.getCount();
        cursor.close();

        /* If the number of records is none, the table is empty */
        return rowCount == 0;
    }

    /**
     * Get all records from the table
     * @return Cursor containing all records for the table
     */
    public Cursor getAll() {
        return getDatabase().rawQuery("select * from " + mTable, null);
    }

    /**
     * Insert DatabaseObject into table
     * @param object DatabaseObject to be inserted into table
     */
    public void insert(@NonNull DatabaseObject object) {
        mDb = getDatabase();
        /* Generate ContentValues from the object's variables */
        ContentValues cv = object.createCV();

        /* If the ContentValues were generated successfully, insert into table */
        if (cv != null) {
            mDb.insert(this.mTable, null, cv);
        }

        /* Notify event listeners that the database was interacted with */
        OnDatabaseInteracted.notifyDatabaseInteracted();
    }

    /**
     * Update DatabaseObject from table
     * @param object DatabaseObject to update existing record with
     * @param whereClause Replace records that match clause
     * @param whereArgs Replaces '?' wildcards in whereClause
     */
    public void update(@NonNull DatabaseObject object, String whereClause, String[] whereArgs) {
        mDb = getDatabase();
        ContentValues cv = object.createCV();

        /* If the ContentValues were generated successfully, update record */
        if (cv != null) {
            mDb.update(this.mTable, cv, whereClause, whereArgs);
        }

        /* Notify event listeners that the database was interacted with */
        OnDatabaseInteracted.notifyDatabaseInteracted();
    }

    /**
     * Delete record from table
     * @param whereClause Delete records that match clause
     * @param whereArgs Replaces '?' wildcards in whereClause
     */
    public void delete(String whereClause, String[] whereArgs) {
        mDb = getDatabase();

        mDb.delete(this.mTable, whereClause, whereArgs);

        /* Notify event listeners that the database was interacted with */
        OnDatabaseInteracted.notifyDatabaseInteracted();
    }

    /**
     * Get a database instance from the provided context environment.
     * @return Returns a SQLiteDatabase instance
     */
    public SQLiteDatabase getDatabase() {
        return Database.getInstance(mContext).getReadableDatabase();
    }

    /* Get date query with wildcard at the end, to match all records with the same date without the time */
    public String getDateQueryWildcardFormat(LocalDate date) {
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT_NO_TIME);

        if(date != null) {
            return dateFormatter.format(date) + "%";
        }
        return null;
    }

}
