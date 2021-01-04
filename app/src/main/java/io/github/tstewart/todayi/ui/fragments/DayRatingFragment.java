package io.github.tstewart.todayi.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
    private static final int MAX_RATING = UserPreferences.getMaxDayRating();

    /* Current rating selector */
    private DayRatingSelector mRatingSelector;

    /* Colors for individual day rating */
    int[] mColors;
    /* Current date (Application-wide) */
    LocalDate mSelectedDate;
    /* Database table helper, assists with Database interaction */
    DayRatingTableHelper mTableHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_rater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mColors = new int[MAX_RATING];
        mColors = new ColorBlendHelper(mColors.length).blendColors();

        LinearLayout ll = view.findViewById(R.id.linearLayoutDayRating);

        if(MAX_RATING>5) {
            mRatingSelector = new DayRatingListSelector(getContext(), ll, this::updateRating);
        }
        else {
            mRatingSelector = new DayRatingButtonSelector(getContext(), ll, this::updateRating);
        }

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

        /* Get rating for current date */
        int rating = mTableHelper.getRating(LocalDate.now(), -1);

        /* Set currently selected button to rating of current day (if exists) */
        if (rating >= 0) mRatingSelector.setRating(rating);

        /* Register OnDateChanged to set current day rating */
        OnDateChanged.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        /* If this fragment is attached to a parent */
        if(isAdded()) {
            /* Refresh this fragment by detaching it and reattaching it
            * This will reset the max rating view, so that if settings change the max rating, this will instantly be reflected */
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().detach(this).attach(this).commit();
        }
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
        /* Get rating for current date */
        int rating = mTableHelper.getRating(date, -1);

        /* If index is valid, set background color for Button */
        if (rating >= 0) mRatingSelector.setRating(rating);
        /* If index is invalid, reset background color for all buttons */
        else mRatingSelector.resetSelected();

        this.mSelectedDate = date;
    }
}
