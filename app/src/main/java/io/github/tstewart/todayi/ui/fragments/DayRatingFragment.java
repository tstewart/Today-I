package io.github.tstewart.todayi.ui.fragments;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.helpers.DayRatingTableHelper;

public class DayRatingFragment extends Fragment implements OnDateChangedListener {

    final int[] mColors = new int[]{R.color.colorRatingRed, R.color.colorRatingOrange, R.color.colorRatingYellow, R.color.colorRatingLightGreen, R.color.colorRatingGreen};
    Button[] mButtons;
    Date mSelectedDate;

    DayRatingTableHelper mTableHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_rater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButtons = new Button[5];
        LinearLayout ll = view.findViewById(R.id.linearLayoutDayRating);

        for (int i = 0; i < 5; i++) {

            mButtons[i] = new Button(new ContextThemeWrapper(getContext(), R.style.AppTheme_DayRatingButton), null, R.style.Widget_AppCompat_Button_Borderless);
            mButtons[i].setText(String.valueOf(i + 1));
            mButtons[i].setOnClickListener(this::onRatingButtonClicked);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            ll.addView(mButtons[i], layoutParams);
        }

        int index = getIndexOfRating(new Date());

        if (index >= 0) setSelectedButton(index);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTableHelper = new DayRatingTableHelper(getContext());
        OnDateChanged.addListener(this);
    }

    private void onRatingButtonClicked(View v) {
        Context context = getContext();

        if (mButtons != null && context != null) {

            resetAllButtonBackgrounds();

            if (v instanceof Button) {
                Button buttonClicked = (Button) v;

                int index = Arrays.asList(mButtons).indexOf(buttonClicked);

                if (index >= 0 && index < mButtons.length) {
                    int color = mColors[index];
                    setButtonBackground(buttonClicked, color);

                    setSelectedButton(index);

                    if (mTableHelper != null) {
                        mTableHelper.setRating(mSelectedDate, index + 1);
                    }
                }
            }
        }
    }

    private void setSelectedButton(int index) {
        resetAllButtonBackgrounds();

        if (index < mColors.length) {

            Button button = mButtons[index];
            int color = mColors[index];

            setButtonBackground(button, color);
        }
    }

    private void resetAllButtonBackgrounds() {
        if (mButtons != null) Arrays.asList(mButtons).forEach(this::resetButtonBackground);
    }

    private void resetButtonBackground(Button button) {
        setButtonBackground(button, R.color.colorTransparent);
    }

    private void setButtonBackground(Button button, int color) {
        if (getContext() != null && button != null) {
            GradientDrawable drawable = (GradientDrawable) button.getBackground();
            drawable.setColor(ContextCompat.getColor(getContext(), color));
        }
    }

    private int getIndexOfRating(Date date) {
        Context context = getContext();

        if (context != null) {
            int index = new DayRatingTableHelper(context).getRating(date, -1);
            return index - 1;
        }

        return -1;
    }

    @Override
    public void onDateChanged(Date date) {
        int index = getIndexOfRating(date);

        if (index >= 0) setSelectedButton(index);
        else resetAllButtonBackgrounds();

        this.mSelectedDate = date;
    }
}
