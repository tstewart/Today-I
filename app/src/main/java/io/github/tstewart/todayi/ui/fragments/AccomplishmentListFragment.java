package io.github.tstewart.todayi.ui.fragments;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.adapters.AccomplishmentCursorAdapter;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.events.OnSwipePerformedListener;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.interfaces.OnDatabaseInteractionListener;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.models.Accomplishment;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;

/**
 * Fragment for viewing, adding, editing, and deleting Accomplishments
 * Watches for database and date changes
 */
public class AccomplishmentListFragment extends ListFragment implements OnDatabaseInteractionListener, OnDateChangedListener {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    /* Cursor adapter, loads posts to ListView with provided Cursor */
    private AccomplishmentCursorAdapter mCursorAdapter;

    /* Current dialog, restricts multiple dialogs from opening at once */
    private AlertDialog mDialog;

    /* Currently selected date (Application-wide) */
    private LocalDate mSelectedDate = LocalDate.now();

    /* Database table helper, assists with Database interaction */
    private AccomplishmentTableHelper mTableHelper;

    /* */

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
                public void onSwipe(SwipeDirection direction) {
                    /* If we should do anything with swipe gestures (controlled by settings) */
                    if(UserPreferences.isGesturesEnabled()) {

                        if (mSelectedDate == null) mSelectedDate = LocalDate.now();

                        if (direction == SwipeDirection.LEFT) {
                            mSelectedDate = mSelectedDate.plusDays(1);
                        } else {
                            mSelectedDate = mSelectedDate.minusDays(1);
                        }

                        OnDateChanged.notifyDateChanged(mSelectedDate);
                    }
                }
            });
            /* Add listener for scroll events on ListView
            * Show or hide the indicator that tells the user if the ListView overflows off screen */

            /* Get indicator imageView */
            ImageView indicator = view.findViewById(R.id.imageViewListDownIndicator);

            indicator.setVisibility(View.VISIBLE);

            if(indicator != null) {
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) { /* Not required */}

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                        /* If can scroll, animate indicator to an alpha of 1 (Visible) */
                        if (view.canScrollVertically(1)) indicator.animate().alpha(1).setDuration(200);
                        /* If can scroll, animate indicator to an alpha of 0 (Invisible) */
                        else indicator.animate().alpha(0).setDuration(200);

                    }
                });
            }
        }


        /* Add click listener to new accomplishment button */
        Button newAccomplishmentButton = view.findViewById(R.id.buttonNewAccomplishment);
        if(newAccomplishmentButton != null)
            newAccomplishmentButton.setOnClickListener(this::onNewItemButtonPressed);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mTableHelper = new AccomplishmentTableHelper(getContext());

        this.mCursorAdapter = new AccomplishmentCursorAdapter(getContext(), null);

        setListAdapter(mCursorAdapter);
        getListView().setOnItemClickListener(this::onListItemClick);

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

            /* Get time posted if available, otherwise default to current time */
            LocalDateTime selectedDate = null;
            String timePosted;
            try {
                /* Get time posted and attempt to parse into a date object */
                timePosted = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                if(timePosted != null) {
                    selectedDate = LocalDateTime.parse(timePosted, DateTimeFormatter.ofPattern(DBConstants.DATE_FORMAT));
                }
            }
            /* If failed, ignore this error and set selected date to default */
            catch(SQLiteException | DateTimeParseException ignore) {}
            finally {
                if(selectedDate == null)
                    selectedDate = LocalDateTime.now();
            }

            /*
             Dismiss current dialog if one is currently open
             Prevents multiple dialogs from opening
            */
            dismissCurrentDialog();

            this.mDialog = new AccomplishmentDialog(this.getContext())
                    .setText(itemContent)
                    .setDialogType(AccomplishmentDialog.DialogType.EDIT)
                    .setSelectedTime(selectedDate)
                    .setConfirmClickListener((dialogView -> {
                        EditText input = dialogView.getRootView().findViewById(R.id.editTextAccomplishmentManage);
                        TextView timeLabel = dialogView.getRootView().findViewById(R.id.textViewSelectedTime);

                        if (input != null) {

                            LocalDateTime newSelectedDate;

                            if(timeLabel != null)
                                newSelectedDate = parseDate(timeLabel.getText().toString());
                            else
                                newSelectedDate = LocalDateTime.of(mSelectedDate, LocalTime.now());

                            /* Create Accomplishment object from new values */
                            Accomplishment accomplishment = new Accomplishment(newSelectedDate, input.getText().toString());

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
                    TextView timeLabel = dialogView.getRootView().findViewById(R.id.textViewSelectedTime);

                    if (input != null) {

                        LocalDateTime selectedDate;

                        if(timeLabel != null)
                            selectedDate = parseDate(timeLabel.getText().toString());
                        else
                            selectedDate = LocalDateTime.of(mSelectedDate, LocalTime.now());


                        /* Create Accomplishment object from new values */
                        Accomplishment accomplishment = new Accomplishment(selectedDate, input.getText().toString());

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

    /* Parse time posted response from dialog */
    private LocalDateTime parseDate(String timeString) {
        try {
            LocalTime time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern(DBConstants.TIME_FORMAT));

            return LocalDateTime.of(mSelectedDate, time);
        }
        catch(DateTimeParseException e) {
            Log.w(CLASS_LOG_TAG, e.getMessage(), e);
            return null;
        }
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

            /* Format current date to database format with wildcard to pattern match */
            String dateFormatted = mTableHelper.getDateQueryWildcardFormat(mSelectedDate);

            if(db.isOpen())
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
    public void onDateChanged(LocalDate date) {
        this.mSelectedDate = date;
        refreshCursor();
    }
}
