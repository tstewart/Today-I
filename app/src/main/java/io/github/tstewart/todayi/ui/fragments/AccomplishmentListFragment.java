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

import org.threeten.bp.LocalDate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.adapters.AccomplishmentAdapter;
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
public class AccomplishmentListFragment extends Fragment implements OnDatabaseInteractionListener, OnDateChangedListener {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    private RecyclerView mRecyclerView;

    /* Cursor adapter, loads posts to ListView with provided Cursor */
    private AccomplishmentAdapter mAdapter;

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

        mRecyclerView = view.findViewById(R.id.recyclerList);


        /* Get indicator imageView */
        ImageView indicator = view.findViewById(R.id.imageViewListDownIndicator);
        if(indicator != null) {
            /* Add listener for scroll events on RecyclerView
             * Show or hide the indicator that tells the user if the RecyclerView overflows off screen */
            mRecyclerView.setOnScrollListener(toggleIndicatorOnScroll(indicator));
            /* Add onClickListener to indicator to auto scroll to bottom of ListView */
            indicator.setOnClickListener(v -> mRecyclerView.scrollToPosition(mRecyclerView.getChildCount()));
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

            this.mAdapter = new AccomplishmentAdapter(this, mTableHelper, getNewCursor());

            ItemTouchHelper touchHelper = new ItemTouchHelper(new AccomplishmentAdapter.AccomplishmentCardTouchListener(mAdapter));
            touchHelper.attachToRecyclerView(mRecyclerView);

            this.mAdapter.setTouchHelper(touchHelper);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

            /* Add listener to notify fragment of database updates */
            OnDatabaseInteracted.addListener(this);
            /* Add listener to notify fragment of date changes */
            OnDateChanged.addListener(this);
        }


    @Override
    public void onPause() {
        super.onPause();
        mAdapter.persistPositions();
    }

    /**
     * Toggle indicator on or off if ListView can be scrolled
     * @param indicator Indicator image to toggle
     */
    private RecyclerView.OnScrollListener toggleIndicatorOnScroll(ImageView indicator) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                /* If can scroll, animate indicator to an alpha of 1 (Visible) */
                if (recyclerView.canScrollVertically(1)) {
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
        mAdapter.setCursor(cursor);
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
