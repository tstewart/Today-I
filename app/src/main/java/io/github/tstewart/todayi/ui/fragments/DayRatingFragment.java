package io.github.tstewart.todayi.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.helpers.db.DayRatingTableHelper;
import io.github.tstewart.todayi.ui.views.DayRatingButtonSelector;
import io.github.tstewart.todayi.ui.views.DayRatingListSelector;
import io.github.tstewart.todayi.ui.views.DayRatingSelector;

/*
 * Fragment for editing day ratings
 */
public class DayRatingFragment extends Fragment implements OnDateChangedListener {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private static final String CLASS_LOG_TAG = DayRatingFragment.class.getSimpleName();

    /*
    Maximum selectable rating
     */
    private int mCurrentMaxRating = UserPreferences.getMaxDayRating();

    /* Main Layout */
    LinearLayout mMainLayout;

    /* Current rating selector */
    private DayRatingSelector mRatingSelector;

    /* Colors for individual day rating */
    int[] mColors;
    /* Current date (Application-wide) */
    LocalDate mSelectedDate;
    /* Database table helper, assists with Database interaction */
    DayRatingTableHelper mTableHelper;

    /* Checks if onResume has been run at least once.
    * This prevents onResume from being called as soon as the Activity is created */
    boolean mHasCalledResume = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_rater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mColors = new int[mCurrentMaxRating];
        mColors = new ColorBlendHelper(mColors.length).blendColors();

        mMainLayout = view.findViewById(R.id.linearLayoutDayRating);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context context = getContext();

        /* If this fragment's context is valid */
        if(context != null) {
            mTableHelper = new DayRatingTableHelper(getContext());
        }
        else {
            /* Try and establish a database connection with the parent activity, if exists */
            Activity parent = getActivity();
            if(parent != null) {
                mTableHelper = new DayRatingTableHelper(parent.getApplicationContext());
            }
        }

        /* Setup rating selector */
        setRatingSelector();

        /* Register OnDateChanged to set current day rating */
        OnDateChanged.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mHasCalledResume) {

            int newMaxRating = UserPreferences.getMaxDayRating();

            /* If max rating has changed, refresh selector */
            if(newMaxRating != mCurrentMaxRating) {
                mCurrentMaxRating = UserPreferences.getMaxDayRating();
                /* Refresh the fragment's rating selector. */
                updateRatingSelector();
                /* Update selector with selected rating for this date */
                setSelectedRating();
            }
        }
        /* Resume has now been called at least once, and rating selector can be properly adjusted */
        else mHasCalledResume = true;
    }

    private void updateRatingSelector() {
        if(mMainLayout != null) {
            /* Delete existing selector from layout */
            mMainLayout.removeAllViews();
            /* Set new rating selector */
            setRatingSelector();
        }
    }

    /* Setup the rating selector for this fragment */
    private void setRatingSelector() {
        if(mMainLayout != null) {
            /* If max rating is greater than 10, use list-based selector */
            if (mCurrentMaxRating > 10 ) {
                mRatingSelector = new DayRatingListSelector(getContext(), mMainLayout, this::updateRating);
            }
            /* If max rating is less than or equal to 5, use button-based selector */
            else {
                mRatingSelector = new DayRatingButtonSelector(getContext(), mMainLayout, this::updateRating);
            }
        }
    }

    /* Set selected rating on rating selector for the selected date */
    private void setSelectedRating() {
        /* Get rating for current date */
        int rating = mTableHelper.getRating(mSelectedDate, -1);

        /* If index is valid, set selected rating in rating selector */
        if (rating >= 0) mRatingSelector.setRating(rating);
            /* If index is invalid, reset selected rating in rating selector */
        else mRatingSelector.resetSelected();
    }

    public void updateRating(int rating) {
        if(mTableHelper != null) {

            /* Check existing rating, if it is the same as this rating, assume the rating is being cancelled */
            int existingRating = mTableHelper.getRating(mSelectedDate, -1);
            if(existingRating>0 && existingRating==rating) {
                String currentDateString = mSelectedDate.format(DateTimeFormatter.ofPattern(DBConstants.DATE_FORMAT_NO_TIME));
                mTableHelper.delete(DBConstants.COLUMN_DATE+"=?", new String[]{currentDateString});
                mRatingSelector.setRating(-1);
            }
            /* If the same rating doesn't exist, update the rating for this day */
            else {
                try {
                    mTableHelper.setRating(mSelectedDate, rating);
                } catch (ValidationFailedException e) {
                    Log.w(CLASS_LOG_TAG, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void onDateChanged(LocalDate date) {
        this.mSelectedDate = date;
        setSelectedRating();
    }
}
