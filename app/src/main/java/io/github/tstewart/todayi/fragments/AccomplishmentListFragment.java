package io.github.tstewart.todayi.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.DatabaseAccomplishmentLoader;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.object.Accomplishment;
import io.github.tstewart.todayi.sql.AccomplishmentQuery;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.AccomplishmentCursorAdapter;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class AccomplishmentListFragment extends ListFragment {

    private AccomplishmentCursorAdapter adapter;
    private Cursor cursor;

    private Button addNewAccomplishmentButton;
    private Date selectedDate = new Date();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_accomplishment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new AccomplishmentCursorAdapter(getActivity(), cursor, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);

        addNewAccomplishmentButton = new Button(getActivity());
        addNewAccomplishmentButton.setText(getResources().getText(R.string.new_accomplishment));
        addNewAccomplishmentButton.setOnClickListener(this::onButtonPressed);
        getListView().addFooterView(addNewAccomplishmentButton);
    }

    private void onButtonPressed(View view) {
        AlertDialog dialog = getAccomplishmentDialog();

        // Change the input mode, so the keyboard pops up when the dialog opens
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    AlertDialog getAccomplishmentDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(this.getResources().getString(R.string.new_accomplishment_dialog_title));

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_new_accomplishment, null);
        builder.setView(view);

        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText input = view.findViewById(R.id.editTextNewAccomplishment);
                String content = input.getText().toString();

                addAccomplishmentToDb(content);
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);

        return builder.create();
    }

    private void addAccomplishmentToDb(String content) {
        SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
        ContentValues cv = DBConstants.getContentValues(content, selectedDate);

        db.insert(DBConstants.ACCOMPLISHMENT_TABLE, null, cv);

        Cursor newCursor = new AccomplishmentQuery().getCursor(db, DBConstants.ACCOMPLISHMENT_QUERY, new Object[]{selectedDate});
        setCursor(newCursor);
    }

    public void setCursor(Cursor cursor) {
        Cursor currentCursor = adapter.getCursor();
        if(currentCursor != null) currentCursor.close();

        adapter.swapCursor(cursor);
    }

    public void setCurrentDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }
}
