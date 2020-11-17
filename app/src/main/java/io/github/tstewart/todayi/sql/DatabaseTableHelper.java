package io.github.tstewart.todayi.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.object.DatabaseObject;

public class DatabaseTableHelper {

    private String table;

    public DatabaseTableHelper(@NonNull String table) {
        this.table = table;
    }

    public void insert(@NonNull Context context, @NonNull DatabaseObject object) {
        SQLiteDatabase db = getDatabase(context);
        ContentValues cv = object.createCV();

        if(cv != null) {
            db.insert(this.table, null, cv);
        }

        onEnd(db);
    }

    public void update(@NonNull Context context, @NonNull DatabaseObject object, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getDatabase(context);
        ContentValues cv = object.createCV();

        if(cv != null) {
            db.update(this.table, cv, whereClause, whereArgs);
        }

        onEnd(db);
    }

    private SQLiteDatabase getDatabase(Context context) {
        return new Database(context).getReadableDatabase();
    }


    private void onEnd(SQLiteDatabase db) {
        db.close();
    }

}
