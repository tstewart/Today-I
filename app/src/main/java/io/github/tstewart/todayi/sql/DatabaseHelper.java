package io.github.tstewart.todayi.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.event.OnDatabaseInteracted;
import io.github.tstewart.todayi.object.DatabaseObject;

public class DatabaseHelper {

    private final String mTable;
    private SQLiteDatabase mDb;

    public DatabaseHelper(@NonNull String table) {
        this.mTable = table;
    }

    public boolean isEmpty(@NonNull Context context) {
        mDb = getDatabase(context);

        Cursor cursor = mDb.rawQuery("select ? from " + mTable, new String[]{DBConstants.COLUMN_ID});

        int columnCount = cursor.getColumnCount();
        cursor.close();

        return columnCount == 0;
    }

    public void insert(@NonNull Context context, @NonNull DatabaseObject object) {
        mDb = getDatabase(context);
        ContentValues cv = object.createCV();

        if (cv != null) {
            mDb.insert(this.mTable, null, cv);
        }

        onEnd(mDb);
    }

    public void update(@NonNull Context context, @NonNull DatabaseObject object, String whereClause, String[] whereArgs) {
        mDb = getDatabase(context);
        ContentValues cv = object.createCV();

        if (cv != null) {
            mDb.update(this.mTable, cv, whereClause, whereArgs);
        }

        onEnd(mDb);
    }

    public void delete(@NonNull Context context, String whereClause, String[] whereArgs) {
        mDb = getDatabase(context);

        mDb.delete(this.mTable, whereClause, whereArgs);

        onEnd(mDb);
    }

    public SQLiteDatabase getDatabase(Context context) {
        return new Database(context).getReadableDatabase();
    }


    private void onEnd(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
        OnDatabaseInteracted.notifyDatabaseInteracted();
    }

}
