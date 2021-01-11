package io.github.tstewart.todayi.ui.views;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import io.github.tstewart.todayi.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DayRatingButtonSelector extends DayRatingSelector {

    Button[] mButtons;

    public DayRatingButtonSelector(Context context, LinearLayout parent, OnRatingChangedListener listener) {
        super(context, parent, listener);

        int maxRating = this.mMaxRating;

        /* Define button theme */
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.AppTheme_DayRatingButton);

        /* Create buttons to make up the selectable day ratings */
        mButtons = new Button[maxRating];

        for (int i = 0; i < maxRating; i++) {

            /* Set button theme */
            mButtons[i] = new Button(themeWrapper, null, 0);
            /* Set text to current index */
            mButtons[i].setText(String.valueOf(i + 1));
            /* Set button tag to rating (to ensure this button can have it's associated rating checked later) */
            mButtons[i].setTag(i+1);
            mButtons[i].setOnClickListener(this::onRatingButtonClicked);

            /* Set button layout */
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1);

            /* Add button to layout */
            parent.addView(mButtons[i], layoutParams);
        }

        /* Set weight of individual buttons in the fragment
         * E.g. A weight of 5 means each button takes up 20% of the parent layout */
        parent.setWeightSum(maxRating);
    }

    private void onRatingButtonClicked(View view) {
        if(view instanceof Button) {
            Button button = (Button)view;

            if(button.getTag() instanceof Integer) {
                int rating = (int)button.getTag();
                updateRating(rating);
            }
        }
    }

    private Button getButtonForRating(int rating) {
        for (Button button : mButtons) {
            if(button.getTag().equals(rating)) return button;
        }
        return null;
    }

    public void updateRating(int rating) {
        setRating(rating);
        this.mListener.ratingChanged(rating);
    }

    @Override
    public void setRating(int rating) {
        Button button = getButtonForRating(rating);

        resetSelected();

        if(rating>0 && button != null) {
            int color = getColorForRating(rating);
            setButtonBackground(button, color);
        }
    }

    @Override
    public void resetSelected() {
        int transparent = mContext.getColor(R.color.colorTransparent);
        for (Button button : mButtons) {
            setButtonBackground(button, transparent);
        }
    }
}
