package io.github.tstewart.todayi.sql;

import android.content.Context;
import android.widget.Toast;

import java.util.Date;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.object.Accomplishment;

public class AccomplishmentTableHelper {

    final Context context;
    final DatabaseHelper helper;

    public AccomplishmentTableHelper(@NonNull Context context) {
        this.context = context;
        this.helper = new DatabaseHelper(DBConstants.ACCOMPLISHMENT_TABLE);
    }

    public void insert(Accomplishment accomplishment) throws IllegalArgumentException {
        accomplishment.validate();

        helper.insert(context, accomplishment);
    }

    public void update(Accomplishment accomplishment, long id) throws IllegalArgumentException {
        accomplishment.validate();

        helper.update(context, accomplishment, DBConstants.COLUMN_ID + "=? ", new String[]{String.valueOf(id)});
    }

    public void delete(long id) throws IllegalArgumentException {
        helper.delete(context, DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
