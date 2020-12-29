package io.github.tstewart.todayi.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.helpers.db.DayRatingTableHelper;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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

    /* Colors for individual day rating */
    int[] mColors;
    /* List of day rating buttons */
    Button[] mButtons;
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
        mButtons = new Button[MAX_RATING];

        LinearLayout ll = view.findViewById(R.id.linearLayoutDayRating);

        /* Create 5 buttons to make up the selectable day ratings */
        for (int i = 0; i < MAX_RATING; i++) {

            /* Set button theme */
            mButtons[i] = new Button(new ContextThemeWrapper(getContext(), R.style.AppTheme_DayRatingButton), null, R.style.Widget_AppCompat_Button_Borderless);
            /* Set text to current index */
            mButtons[i].setText(String.valueOf(i + 1));
            mButtons[i].setOnClickListener(this::onRatingButtonClicked);

            /* Set button layout */
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1);

            /* Add button to layout */
            ll.addView(mButtons[i], layoutParams);
        }
        /* Set weight of individual buttons in the fragment
        * E.g. A weight of 5 means each button takes up 20% of the parent layout */
        ll.setWeightSum(MAX_RATING);

        mColors = new ColorBlendHelper(mColors.length).blendColors();

        /* Get rating for current date */
        int index = getIndexOfRating(LocalDate.now());

        /* Set currently selected button to rating of current day (if exists) */
        if (index >= 0) setSelectedButton(index);

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

        /* Register OnDateChanged to set current day rating */
        OnDateChanged.addListener(this);
    }

    private void onRatingButtonClicked(View v) {
        Context context = getContext();

        /* If list of buttons initialised and the item clicked was a Button */
        if (mButtons != null && context != null
                && v instanceof Button) {

            /* Set all button backgrounds to transparent */
            resetAllButtonBackgrounds();

            Button buttonClicked = (Button) v;

            /* Index of selected button in List of buttons */
            int index = Arrays.asList(mButtons).indexOf(buttonClicked);

            /* Get current rating for this date */
            int currentRating = mTableHelper.getRating(mSelectedDate, -1);

            /* If the current rating for this day is the same as the rating just selected, cancel the rating for this date */
            if(index+1 == currentRating) {
                String currentDateString = mSelectedDate.format(DateTimeFormatter.ofPattern(DBConstants.DATE_FORMAT_NO_TIME));
                mTableHelper.delete(DBConstants.COLUMN_DATE +"=?", new String[]{currentDateString});
            }
            /* If the rating selection was not the same as the previous rating, change the rating in the db
            If the selected button is within the bounds of buttons (0 - buttons array length) */
            else if (index >= 0 && index < mButtons.length) {

                /* Set background color for selected button */
                setSelectedButton(index);

                /* Set rating for this day in Database */
                if (mTableHelper != null) {
                    try {
                        mTableHelper.setRating(mSelectedDate, index + 1);
                    } catch (ValidationFailedException e) {
                        Log.w(CLASS_LOG_TAG,e.getMessage(), e);
                        Toast.makeText(getContext(),"Failed to set rating, validation of rating value failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    /**
     * Set selected button for the button at index
     * @param index Index of button
     */
    private void setSelectedButton(int index) {
        resetAllButtonBackgrounds();

        /* If index is not out of range of colors array */
        if (index < mColors.length) {

            Button button = mButtons[index];
            int color = mColors[index];

            /* Set background color of this button */
            setButtonBackground(button, color);
        }
    }

    /*
    Remove color from all button backgrounds
     */
    private void resetAllButtonBackgrounds() {
        for (Button button:
             mButtons) {
            resetButtonBackground(button);
        }
    }

    /*
    Reset background color for provided button
     */
    private void resetButtonBackground(Button button) {
        Context context = getContext();

        if(context != null) {
            int color = ContextCompat.getColor(context,R.color.colorTransparent);
            setButtonBackground(button, color);
        }
    }

    /*
    Set button background color to provided color
     */
    private void setButtonBackground(Button button, int color) {
        if (button != null) {
            GradientDrawable drawable = (GradientDrawable) button.getBackground();
            drawable.setColor(color);
        }
    }

    /*
    Get day rating from Database for Date, adjusted to match Array index
     */
    private int getIndexOfRating(LocalDate date) {
        Context context = getContext();

        if (context != null) {
            /* Get rating from Database for provided date */
            int index = new DayRatingTableHelper(context).getRating(date, -1);
            return index - 1;
        }

        return -1;
    }

    @Override
    public void onDateChanged(LocalDate date) {
        /* Get Array index of rating for current date */
        int index = getIndexOfRating(date);

        /* If index is valid, set background color for Button */
        if (index >= 0) setSelectedButton(index);
            /* If index is invalid, reset background color for all buttons */
        else resetAllButtonBackgrounds();

        this.mSelectedDate = date;
    }
}
