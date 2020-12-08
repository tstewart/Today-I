package io.github.tstewart.todayi.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;

/**
 * Pulls data from the Accomplishments table of the database, and converts it's data into a ListItem
 */
public class AccomplishmentCursorAdapter extends CursorAdapter {

    /* Flags for CursorAdapter constructor are set to 0 to prevent use of Deprecated constructor that utilises AUTO_QUERY */
    public AccomplishmentCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_accomplishment, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        /* Get content TextView from layout */
        TextView contentView = view.findViewById(R.id.textViewContent);
        /* Get time posted TextView from layout */
        TextView datePostedView = view.findViewById(R.id.textViewTimePosted);
        /* Get the content of the next item in the Accomplishment table */
        String content = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_CONTENT));
        /* As older versions of the database may not have been updated, try and get the timePosted string but if it fails, don't add it. */
        try {
            /* Get the time posted of the next item in the Accomplishment table */
            String timePosted = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_TIME));

            /* If there was a time posted, add set date TextView to this. */
            if(timePosted != null) {
                datePostedView.setText(timePosted);
            }
        }
        catch(SQLiteException | IllegalArgumentException e) {
            Log.w(AccomplishmentCursorAdapter.class.getSimpleName(), e.getMessage(), e);
        }

        contentView.setText(content);
    }

}
