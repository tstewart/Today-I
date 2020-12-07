package io.github.tstewart.todayi.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.events.OnSwipePerformedListener;
import io.github.tstewart.todayi.helpers.DateCalculationHelper;
import io.github.tstewart.todayi.interfaces.OnDatabaseInteractionListener;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.models.Accomplishment;
import io.github.tstewart.todayi.helpers.AccomplishmentTableHelper;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.adapters.AccomplishmentCursorAdapter;
import io.github.tstewart.todayi.ui.activities.MainActivity;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;

/**
 * Fragment for viewing, adding, editing, and deleting Accomplishments
 * Watches for database and date changes
 */
public class AccomplishmentListFragment extends ListFragment implements OnDatabaseInteractionListener, OnDateChangedListener {

    /* Cursor adapter, loads posts to ListView with provided Cursor */
    private AccomplishmentCursorAdapter mCursorAdapter;

    /* Current dialog, restricts multiple dialogs from opening at once */
    private AlertDialog mDialog;

    /* Currently selected date (Application-wide) */
    private Date mSelectedDate = new Date();

    /* Database table helper, assists with Database interaction */
    private AccomplishmentTableHelper mTableHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_accomplishment_list, container, false);

        /* Add gesture support, allowing user to change dates by swiping the ListView */
        ListView listView = view.findViewById(android.R.id.list);
        if(listView != null) {
            listView.setOnTouchListener(new OnSwipePerformedListener(getContext()) {
                @Override
                public void onLeftSwipe() {
                    super.onLeftSwipe();
                    if(mSelectedDate == null) mSelectedDate = new Date();
                    mSelectedDate = DateCalculationHelper.subtractFromDate(mSelectedDate, Calendar.DAY_OF_MONTH,1);
                    OnDateChanged.notifyDateChanged(mSelectedDate);
                }

                @Override
                public void onRightSwipe() {
                    super.onRightSwipe();
                    if(mSelectedDate == null) mSelectedDate = new Date();
                    mSelectedDate = DateCalculationHelper.addToDate(mSelectedDate, Calendar.DAY_OF_MONTH,1);
                    OnDateChanged.notifyDateChanged(mSelectedDate);
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mTableHelper = new AccomplishmentTableHelper(getContext());

        this.mCursorAdapter = new AccomplishmentCursorAdapter(getContext(), null);

        setListAdapter(mCursorAdapter);
        getListView().setOnItemClickListener(this::onListItemClick);

        /* Append New button to end of ListView */
        Button newItemButton = new Button(getContext());
        newItemButton.setText(getResources().getText(R.string.new_accomplishment));
        newItemButton.setOnClickListener(this::onNewItemButtonPressed);
        getListView().addFooterView(newItemButton);

        /* Add listener to notify fragment of database updates */
        OnDatabaseInteracted.addListener(this);
        /* Add listener to notify fragment of date changes */
        OnDateChanged.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        /* Prevents dialogs remaining open if the user changes activities while a dialog is open */
        dismissCurrentDialog();
    }

    private void onListItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (mCursorAdapter == null) {
            Log.w(this.getClass().getName(), "List item click called before adapter initialised.");
            return;
        }

        /* Get cursor for currently selected Accomplishment */
        Cursor cursor = (Cursor) mCursorAdapter.getItem(position);

        /* Position of the item clicked must be less than the total number of rows in the cursor */
        if (cursor.getCount() > position) {

            /* Get content of the currently selected Accomplishment */
            String itemContent = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_CONTENT));

            /*
             Dismiss current dialog if one is currently open
             Prevents multiple dialogs from opening
            */
            dismissCurrentDialog();

            this.mDialog = new AccomplishmentDialog(this.getContext())
                    .setText(itemContent)
                    .setDialogType(AccomplishmentDialog.DialogType.EDIT)
                    .setConfirmClickListener((dialogView -> {
                        EditText input = dialogView.getRootView().findViewById(R.id.editTextAccomplishmentManage);

                        if (input != null) {
                            /* Create Accomplishment object from new values */
                            Accomplishment accomplishment = new Accomplishment(mSelectedDate, input.getText().toString());

                            try {
                                /* Update Database entry with new content */
                                mTableHelper.update(accomplishment, id);
                            } catch (ValidationFailedException e) {
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

        /*
         Dismiss current dialog if one is currently open
         Prevents multiple dialogs from opening
        */
        dismissCurrentDialog();

        this.mDialog = new AccomplishmentDialog(getContext())
                .setDialogType(AccomplishmentDialog.DialogType.NEW)
                .setConfirmClickListener(dialogView -> {
                    EditText input = dialogView.getRootView().findViewById(R.id.editTextAccomplishmentManage);

                    if (input != null) {
                        /* Create Accomplishment object from new values */
                        Accomplishment accomplishment = new Accomplishment(mSelectedDate, input.getText().toString());

                        try {
                            /* Insert Accomplishment into Database */
                            mTableHelper.insert(accomplishment);
                        } catch (ValidationFailedException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();

        this.mDialog.show();
    }

    /**
     * Refresh current cursor
     * Checks Database for entries on the selected date again
     */
    private void refreshCursor() {
        setCursor(getNewCursor());
    }

    /*
     * Get new cursor by checking Database for Accomplishments on selected date
     */
    private Cursor getNewCursor() {
        Context context = getContext();
        if(context != null) {
            SQLiteDatabase db = Database.getInstance(getContext()).getWritableDatabase();

            /* Format current date to database format */
            String dateFormatted = mTableHelper.getDatabaseHelper().getDateAsDatabaseFormat(mSelectedDate);

            return db.rawQuery(DBConstants.ACCOMPLISHMENT_QUERY,new String[]{dateFormatted});
        }
        return null;
    }

    /*
       Set cursor to new cursor, closing current cursor
     */
    private void setCursor(Cursor cursor) {
        Cursor currentCursor = mCursorAdapter.getCursor();
        if (currentCursor != null) currentCursor.close();

        mCursorAdapter.swapCursor(cursor);
    }

    /**
     * Close the current dialog instance if one is open
     */
    public void dismissCurrentDialog() {
        if (this.mDialog != null) mDialog.dismiss();
    }

    @Override
    public void onDatabaseInteracted() {
        /* Search Database again for posts */
        refreshCursor();
    }

    @Override
    public void onDateChanged(Date date) {
        this.mSelectedDate = date;
        refreshCursor();
    }
}
