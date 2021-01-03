package io.github.tstewart.todayi.ui.views;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import io.github.tstewart.todayi.R;

public class DayRatingListSelector extends DayRatingSelector {

    Button mRateButton;

    public DayRatingListSelector(Context context, LinearLayout parent, OnRatingChangedListener listener) {
        super(context, parent, listener);

        /* Create rating button, which will open an AlertDialog to select rating */
        mRateButton = new Button(new ContextThemeWrapper(context, R.style.AppTheme_DayRatingButton), null, R.style.Widget_AppCompat_Button_Borderless);

        /* Set button text TODO use resource string */
        mRateButton.setText("Rate your day...");

        mRateButton.setOnClickListener(this::onRateButtonClicked);

        parent.addView(mRateButton);
    }

    private void onRateButtonClicked(View view) {

        String[] items = new String[mMaxRating];

        for (int i = 0; i < mMaxRating; i++) {
            items[i] = String.valueOf(i+1);
        }

        new AlertDialog.Builder(mContext)
                .setItems(items, (dialog, which) -> updateRating(which+1))
                .create()
                .show();
    }

    public void updateRating(int rating) {
        setRating(rating);
        mListener.ratingChanged(rating);
    }

    @Override
    public void setRating(int rating) {
        int color = getColorForRating(rating);
        setButtonBackground(mRateButton, color);
    }

    @Override
    public void resetSelected() {

    }
}
