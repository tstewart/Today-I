package io.github.tstewart.todayi.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;

public class AccomplishmentCursorAdapter extends CursorAdapter {

    public AccomplishmentCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_accomplishment, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView contentView = view.findViewById(R.id.textViewContent);

        String content = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_CONTENT));

        contentView.setText(content);
    }

}
