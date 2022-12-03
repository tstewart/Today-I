package io.github.tstewart.todayi.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import org.threeten.bp.LocalDate;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.adapters.AccomplishmentCursorAdapter;
import io.github.tstewart.todayi.controllers.AccomplishmentDragSortController;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.events.OnSwipePerformedListener;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.interfaces.OnDatabaseInteractionListener;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentNewDialog;

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
    private AccomplishmentDialog mDialog;

    /* Currently selected date (Application-wide) */
    private LocalDate mSelectedDate = LocalDate.now();

    /* Database table helper, assists with Database interaction */
    private AccomplishmentTableHelper mTableHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_accomplishment_list, container, false);

        /* Add gesture support, allowing user to change dates by swiping the ListView */
        DragSortListView listView = view.findViewById(android.R.id.list);
        LinearLayout accomplishmentListLayout = view.findViewById(R.id.listLayout);

        if(accomplishmentListLayout != null) {
            accomplishmentListLayout.setOnTouchListener(changeDayOnSwipe());
        }

        if(listView != null) {
            /* Get indicator imageView */
            ImageView indicator = view.findViewById(R.id.imageViewListDownIndicator);

            if(indicator != null) {
                /* Add listener for scroll events on ListView
                 * Show or hide the indicator that tells the user if the ListView overflows off screen */
                listView.setOnScrollListener(toggleIndicatorOnScroll(indicator));
                /* Add onClickListener to indicator to auto scroll to bottom of ListView */
                indicator.setOnClickListener(v -> listView.setSelection(listView.getCount()-1));
            }

            AccomplishmentDragSortController controller = new AccomplishmentDragSortController(getContext(), listView);
            controller.setDragHandleId(R.id.drag_handle);
            controller.setDragInitMode(1);
            controller.setBackgroundColor(R.color.colorTransparent);

            listView.setFloatViewManager(controller);
            listView.setOnTouchListener(controller);
            listView.setDragEnabled(true);
        }

        /* Get "+" button, to add a new Accomplishment on click */
        FloatingActionButton newAccomplishmentButton = view.findViewById(R.id.buttonNewAccomplishment);
        newAccomplishmentButton.setOnClickListener(this::onNewItemButtonPressed);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context context = getContext();

        if(context != null) {
            this.mTableHelper = new AccomplishmentTableHelper(context);

            this.mCursorAdapter = new AccomplishmentCursorAdapter(this, context, null);

            setListAdapter(mCursorAdapter);
        }

        /* Add listener to notify fragment of database updates */
        OnDatabaseInteracted.addListener(this);
        /* Add listener to notify fragment of date changes */
        OnDateChanged.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCursorAdapter.persistPositions();
    }

    /* Return listener action to change current day when view is swiped */
    private OnSwipePerformedListener changeDayOnSwipe() {
        return new OnSwipePerformedListener(getContext()) {
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
        };
    }

    /**
     * Toggle indicator on or off if ListView can be scrolled
     * @param indicator Indicator image to toggle
     */
    private AbsListView.OnScrollListener toggleIndicatorOnScroll(ImageView indicator) {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { /* Not required */}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                /* If can scroll, animate indicator to an alpha of 1 (Visible) */
                if (view.canScrollVertically(1)) {
                    indicator.animate().alpha(1).setDuration(200);
                    indicator.setClickable(true);
                }
                /* If cant scroll, animate indicator to an alpha of 0 (Invisible) */
                else {
                    indicator.animate().alpha(0).setDuration(200);
                    indicator.setClickable(false);
                }
            }
        };
    }

    private void onNewItemButtonPressed(View view) {

        /*
         Dismiss current dialog if one is currently open
         Prevents multiple dialogs from opening
        */
        dismissCurrentDialog();

        this.mDialog = new AccomplishmentNewDialog(mSelectedDate);

        mDialog.display(getParentFragmentManager());
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
    public Cursor getNewCursor() {
        Context context = getContext();
        if(context != null) {
            SQLiteDatabase db = Database.getInstance(getContext()).getWritableDatabase();

            /* Format current date to database format with wildcard to pattern match */
            String dateFormatted = mTableHelper.getDateQuery(mSelectedDate);

            if(db.isOpen())
                return db.rawQuery(DBConstants.ACCOMPLISHMENT_QUERY,new String[]{dateFormatted});
        }
        return null;
    }

    /*
       Set cursor to new cursor, closing current cursor
     */
    private void setCursor(Cursor cursor) {
        //Cursor currentCursor = mCursorAdapter.getCursor();
        //if (currentCursor != null) currentCursor.close();

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
        if(mCursorAdapter.getCursor() != null) mCursorAdapter.persistPositions();
        refreshCursor();
    }
}
