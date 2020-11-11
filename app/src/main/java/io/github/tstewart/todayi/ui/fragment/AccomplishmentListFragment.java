package io.github.tstewart.todayi.ui.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.AccomplishmentCursorLoader;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.ui.dialog.AccomplishmentDialog;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.AccomplishmentCursorAdapter;

public class AccomplishmentListFragment extends ListFragment {

    private AccomplishmentCursorAdapter adapter;
    private Cursor cursor;

    private Date selectedDate = new Date();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_accomplishment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new AccomplishmentCursorAdapter(getActivity(), cursor);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this::onListItemClick);

        Button addNewAccomplishmentButton = new Button(getActivity());
        addNewAccomplishmentButton.setText(getResources().getText(R.string.new_accomplishment));
        addNewAccomplishmentButton.setOnClickListener(this::onButtonPressed);
        getListView().addFooterView(addNewAccomplishmentButton);
    }

    private void onListItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Object selectedItem = adapter.getItem(position);

        if(selectedItem instanceof Cursor) {
            Cursor cursor = (Cursor) selectedItem;
            String content = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_CONTENT));

            AccomplishmentDialog dialog = new AccomplishmentDialog(this.getContext(), AccomplishmentDialog.DialogType.EDIT);

            dialog.setPositiveClickListener((dialogInterface, which) -> {
                SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
                ContentValues vars = new ContentValues();

                EditText input = dialog.getView().findViewById(R.id.editTextAccomplishmentManage);
                String newContent = input.getText().toString();

                vars.put(DBConstants.COLUMN_CONTENT, newContent);
                db.update(DBConstants.ACCOMPLISHMENT_TABLE, vars, DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});

                setCursor(getNewCursor());
            });
            dialog.setNegativeButton(null);

            dialog.setText(content);

            dialog.create().show();
        }
    }

    private void onButtonPressed(View view) {
        AlertDialog dialog = getAccomplishmentDialog();
        dialog.show();
    }

    private AlertDialog getAccomplishmentDialog() {

        AccomplishmentDialog dialog = new AccomplishmentDialog(this.getContext(), AccomplishmentDialog.DialogType.NEW);

        dialog.setPositiveClickListener((dialogInterface, which) -> {
            EditText input = dialog.getView().findViewById(R.id.editTextAccomplishmentManage);
            String content = input.getText().toString();

            addAccomplishmentToDb(content);
        });

        dialog.setNegativeButton(null);

        return dialog.create();
    }

    private void addAccomplishmentToDb(String content) {
        SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
        ContentValues cv = DBConstants.getContentValues(content, selectedDate);

        db.insert(DBConstants.ACCOMPLISHMENT_TABLE, null, cv);

        setCursor(getNewCursor());
    }

    public void updateDateAndFetch(Date selectedDate) {
        setCurrentDate(selectedDate);
        setCursor(getNewCursor());
    }

    public void setCurrentDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public Cursor getNewCursor() {
        SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
        return AccomplishmentCursorLoader.getCursor(db, DBConstants.ACCOMPLISHMENT_QUERY, selectedDate);
    }

    public void setCursor(Cursor cursor) {
        Cursor currentCursor = adapter.getCursor();
        if(currentCursor != null) currentCursor.close();

        adapter.swapCursor(cursor);
    }

}
