package io.github.tstewart.todayi.ui.views;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.github.tstewart.todayi.R;

/*
Day rating selector view, as a list of ratings
 */
public class DayRatingListSelector extends DayRatingSelector {

    Button mRateButton;

    public DayRatingListSelector(Context context, LinearLayout parent, OnRatingChangedListener listener) {
        super(context, parent, listener);

        /* Create rating button, which will open an AlertDialog to select rating */
        mRateButton = new Button(new ContextThemeWrapper(context, R.style.AppTheme_DayRatingButton), null, R.style.Widget_AppCompat_Button_Borderless);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1);

        mRateButton.setText(context.getString(R.string.rate_day));

        mRateButton.setOnClickListener(this::onRateButtonClicked);

        parent.addView(mRateButton, layoutParams);
    }

    private void onRateButtonClicked(View view) {

        String[] items = new String[mMaxRating];

        for (int i = 0; i < mMaxRating; i++) {
            items[i] = String.valueOf(i + 1);
        }

        new MaterialAlertDialogBuilder(mContext)
                .setItems(items, (dialog, which) -> updateRating(which + 1))
                .create()
                .show();
    }

    public void updateRating(int rating) {
        setRating(rating);
        mListener.ratingChanged(rating);
    }

    @Override
    public void setRating(int rating) {
        if (rating > 0) {
            int color = getColorForRating(rating);
            setButtonBackground(mRateButton, color);

            mRateButton.setText(String.format(mContext.getString(R.string.rate_day) + " (%s)", rating));
        } else resetSelected();
    }

    @Override
    public void resetSelected() {
        setButtonBackground(mRateButton, mContext.getColor(R.color.colorTransparent));
        mRateButton.setText(mContext.getString(R.string.rate_day));
    }
}
