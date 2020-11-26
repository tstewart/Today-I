package io.github.tstewart.todayi.sql;

import android.content.Context;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.object.Accomplishment;

public class AccomplishmentTableHelper {

    final Context mContext;
    final DatabaseHelper mHelper;

    public AccomplishmentTableHelper(@NonNull Context context) {
        this.mContext = context;
        this.mHelper = new DatabaseHelper(DBConstants.ACCOMPLISHMENT_TABLE);
    }

    public void insert(Accomplishment accomplishment) throws IllegalArgumentException {
        accomplishment.validate();

        mHelper.insert(mContext, accomplishment);
    }

    public void update(Accomplishment accomplishment, long id) throws IllegalArgumentException {
        accomplishment.validate();

        mHelper.update(mContext, accomplishment, DBConstants.COLUMN_ID + "=? ", new String[]{String.valueOf(id)});
    }

    public void delete(long id) throws IllegalArgumentException {
        mHelper.delete(mContext, DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
