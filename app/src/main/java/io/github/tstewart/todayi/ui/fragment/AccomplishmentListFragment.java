package io.github.tstewart.todayi.ui.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.AccomplishmentCursorLoader;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.AccomplishmentCursorAdapter;
import io.github.tstewart.todayi.ui.dialog.AccomplishmentDialog;

public class AccomplishmentListFragment extends ListFragment {

    private AccomplishmentCursorAdapter adapter;

    private Date selectedDate = new Date();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_accomplishment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new AccomplishmentCursorAdapter(getActivity(), null);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this::onListItemClick);

        Button addNewAccomplishmentButton = new Button(getActivity());
        addNewAccomplishmentButton.setText(getResources().getText(R.string.new_accomplishment));
        addNewAccomplishmentButton.setOnClickListener(this::onButtonPressed);
        getListView().addFooterView(addNewAccomplishmentButton);
    }

    private void onListItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Object selectedItem = adapter.getItem(position);

        if (selectedItem instanceof Cursor) {
            Cursor cursor = (Cursor) selectedItem;
            SQLiteDatabase db = new Database(getContext()).getWritableDatabase();

            String content = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_CONTENT));

            AccomplishmentDialog dialog = new AccomplishmentDialog(this.getContext(), AccomplishmentDialog.DialogType.EDIT);

            dialog.setConfirmClickListener((dialogView) -> {
                EditText input = dialog.getView().findViewById(R.id.editTextAccomplishmentManage);
                String newContent = input.getText().toString();

                if (content.length() >= 1 && content.length() <= 200) {
                    ContentValues vars = new ContentValues();

                    vars.put(DBConstants.COLUMN_CONTENT, newContent);
                    db.update(DBConstants.ACCOMPLISHMENT_TABLE, vars, DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});

                    refreshCursor();
                } else {
                    Toast.makeText(this.getContext(), R.string.new_accomplishment_invalid_text, Toast.LENGTH_SHORT).show();
                }
            });

            dialog.setDeleteButtonListener((dialogView) -> {
                db.delete(DBConstants.ACCOMPLISHMENT_TABLE, DBConstants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                refreshCursor();
            });

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

        dialog.setConfirmClickListener((dialogView) -> {
            EditText input = dialog.getView().findViewById(R.id.editTextAccomplishmentManage);
            String content = input.getText().toString();

            if (content.length() >= 1 && content.length() <= 200) {
                addAccomplishmentToDb(content);
            } else {
                Toast.makeText(this.getContext(), R.string.new_accomplishment_invalid_text, Toast.LENGTH_SHORT).show();
            }
        });

        return dialog.create();
    }

    private void addAccomplishmentToDb(String content) {
        SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
        ContentValues cv = DBConstants.getContentValues(content, selectedDate);

        db.insert(DBConstants.ACCOMPLISHMENT_TABLE, null, cv);

        refreshCursor();
    }

    public void updateDateAndFetch(Date selectedDate) {
        setCurrentDate(selectedDate);
        refreshCursor();
    }

    private void setCurrentDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    private void refreshCursor() {
        setCursor(getNewCursor());
    }

    private Cursor getNewCursor() {
        SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
        return AccomplishmentCursorLoader.getCursor(db, DBConstants.ACCOMPLISHMENT_QUERY, selectedDate);
    }

    private void setCursor(Cursor cursor) {
        Cursor currentCursor = adapter.getCursor();
        if (currentCursor != null) currentCursor.close();

        adapter.swapCursor(cursor);
    }

}
