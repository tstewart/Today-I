package io.github.tstewart.todayi.helpers.db;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.models.Accomplishment;

/**
 * Helper class. Provides functionality to insert, update, and remove accomplishments from the database
 */
public class AccomplishmentTableHelper extends DatabaseHelper {

    public AccomplishmentTableHelper(@NonNull Context context) {
        /* Default to generating a DatabaseHelper for the Accomplishment table */
        super(context, DBConstants.ACCOMPLISHMENT_TABLE);
    }

    /**
     * Insert an accomplishment into the Accomplishment table.
     * @param accomplishment Accomplishment to be added.
     * @throws IllegalArgumentException Thrown if the accomplishment object was not valid
     */
    public void insert(Accomplishment accomplishment) throws ValidationFailedException {
        /* Validate accomplishment */
        accomplishment.validate();

        /* Insert into database */
        super.insert(accomplishment);
    }

    /**
     * Update an existing accomplishment in the Accomplishment table
     * @param accomplishment Details of new accomplishment
     * @param id Identifier number of existing accomplishment to be replaced
     * @throws IllegalArgumentException Thrown if the new accomplishment object was not valid
     */
    public void update(Accomplishment accomplishment, long id) throws ValidationFailedException {
        /* Validate new accomplishment */
        accomplishment.validate();

        /* Insert into database, overwriting existing */
        super.updateDBObject(accomplishment, DBConstants.COLUMN_ID + "=? ", new String[]{String.valueOf(id)});
    }

    public void updatePosition(long id, int position) {
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COLUMN_POSITION, position);

        super.update(cv, DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, false);
    }

    /**
     * Deletes an existing accomplishment from the Accomplishment table
     * @param id Identifier number of accomplishment to be removed
     */
    public void delete(long id) {
        super.delete(DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
