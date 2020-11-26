package io.github.tstewart.todayi.ui.fragment;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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
import io.github.tstewart.todayi.event.OnDatabaseInteracted;
import io.github.tstewart.todayi.event.OnDatabaseInteractionListener;
import io.github.tstewart.todayi.event.OnDateChanged;
import io.github.tstewart.todayi.event.OnDateChangedListener;
import io.github.tstewart.todayi.object.Accomplishment;
import io.github.tstewart.todayi.sql.AccomplishmentTableHelper;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.AccomplishmentCursorAdapter;
import io.github.tstewart.todayi.ui.dialog.AccomplishmentDialog;

public class AccomplishmentListFragment extends ListFragment implements OnDatabaseInteractionListener, OnDateChangedListener {

    private AccomplishmentCursorAdapter mAdapter;

    // Current dialog, restricts multiple dialogs from opening at once
    private AlertDialog mDialog;

    private Date mSelectedDate = new Date();

    private AccomplishmentTableHelper mTableHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_accomplishment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mTableHelper = new AccomplishmentTableHelper(getContext());
        this.mAdapter = new AccomplishmentCursorAdapter(getContext(), null);

        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this::onListItemClick);

        Button newItemButton = new Button(getContext());
        newItemButton.setText(getResources().getText(R.string.new_accomplishment));
        newItemButton.setOnClickListener(this::onNewItemButtonPressed);
        getListView().addFooterView(newItemButton);

        // Add listener to notify fragment of database updates
        OnDatabaseInteracted.addListener(this);
        // Add listener to notify fragment of date changes
        OnDateChanged.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Prevents dialogs remaining open if the user changes activities while a dialog is open
        dismissCurrentDialog();
    }

    private void onListItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (mAdapter == null) {
            Log.w(this.getClass().getName(), "List item click called before adapter initialised.");
            return;
        }

        Cursor cursor = (Cursor) mAdapter.getItem(position);

        // Position of the item clicked must be less than the total number of rows in the cursor
        if (cursor.getCount() > position) {

            String itemContent = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_CONTENT));
            dismissCurrentDialog();
            this.mDialog = new AccomplishmentDialog(this.getContext())
                    .setText(itemContent)
                    .setDialogType(AccomplishmentDialog.DialogType.EDIT)
                    .setConfirmClickListener((dialogView -> {
                        EditText input = dialogView.getRootView().findViewById(R.id.editTextAccomplishmentManage);

                        if (input != null) {
                            Accomplishment accomplishment = new Accomplishment(mSelectedDate, input.getText().toString());

                            try {
                                mTableHelper.update(accomplishment, id);
                            } catch (IllegalArgumentException e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }))
                    .setDeleteButtonListener((dialogView -> mTableHelper.delete(id)))
                    .create();

            this.mDialog.show();
        }
    }

    private void onNewItemButtonPressed(View view) {

        dismissCurrentDialog();
        this.mDialog = new AccomplishmentDialog(getContext())
                .setDialogType(AccomplishmentDialog.DialogType.NEW)
                .setConfirmClickListener((dialogView) -> {
                    EditText input = dialogView.getRootView().findViewById(R.id.editTextAccomplishmentManage);

                    if (input != null) {
                        Accomplishment accomplishment = new Accomplishment(mSelectedDate, input.getText().toString());

                        try {
                            mTableHelper.insert(accomplishment);
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();

        this.mDialog.show();
    }

    private void refreshCursor() {
        setCursor(getNewCursor());
    }

    private Cursor getNewCursor() {
        SQLiteDatabase db = new Database(getContext()).getWritableDatabase();
        return AccomplishmentCursorLoader.getCursor(db, DBConstants.ACCOMPLISHMENT_QUERY, mSelectedDate);
    }

    private void setCursor(Cursor cursor) {
        Cursor currentCursor = mAdapter.getCursor();
        if (currentCursor != null) currentCursor.close();

        mAdapter.swapCursor(cursor);
    }

    public void dismissCurrentDialog() {
        if (this.mDialog != null) mDialog.dismiss();
    }

    @Override
    public void onDatabaseInteracted() {
        refreshCursor();
    }

    @Override
    public void onDateChanged(Date date) {
        this.mSelectedDate = date;
        refreshCursor();
    }
}
