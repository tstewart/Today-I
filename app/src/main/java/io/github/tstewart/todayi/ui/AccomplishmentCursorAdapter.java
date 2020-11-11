package io.github.tstewart.todayi.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.sql.DBConstants;

public class AccomplishmentCursorAdapter extends CursorAdapter {

    public AccomplishmentCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    public AccomplishmentCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.accomplishment_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView contentView = view.findViewById(R.id.textViewContent);

        String content = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_CONTENT));

        contentView.setText(content);
    }

}
