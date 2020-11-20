package io.github.tstewart.todayi.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.object.DatabaseObject;
import io.github.tstewart.todayi.event.OnDatabaseInteracted;

public class DatabaseHelper {

    private String table;

    public DatabaseHelper(@NonNull String table) {
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

    public void delete(@NonNull Context context, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getDatabase(context);

        db.delete(this.table, whereClause, whereArgs);

        onEnd(db);
    }

    public SQLiteDatabase getDatabase(Context context) {
        return new Database(context).getReadableDatabase();
    }


    private void onEnd(SQLiteDatabase db) {
        db.close();
        OnDatabaseInteracted.notifyDatabaseInteracted();
    }

}
