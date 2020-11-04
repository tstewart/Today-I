package io.github.tstewart.todayi.sql;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public interface Query<T> {
    List<T> getTableResponses(SQLiteDatabase db, String query, Object[] args);
}
