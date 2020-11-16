package io.github.tstewart.todayi.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.object.DatabaseObject;

public class DatabaseTableHelper {

    public SQLiteDatabase getWritableDatabase(Context context) {
        return new Database(context).getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase(Context context) {
        return new Database(context).getReadableDatabase();
    }

    public void insert(@NonNull Context context, @NonNull String table, @NonNull DatabaseObject object) {
        SQLiteDatabase db = getWritableDatabase(context);
        ContentValues cv = object.createCV();

        if(cv != null) {
            db.insert(table, null, cv);
        }

        onEnd(db);
    }

    private void onEnd(SQLiteDatabase db) {
        db.close();
    }

}
