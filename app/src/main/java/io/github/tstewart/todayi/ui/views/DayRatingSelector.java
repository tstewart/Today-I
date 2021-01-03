package io.github.tstewart.todayi.ui.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.Button;
import android.widget.LinearLayout;

import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;

public abstract class DayRatingSelector {

    final int mMaxRating = UserPreferences.getMaxDayRating();
    final int[] mColors;
    final Context mContext;
    LinearLayout mParent;
    OnRatingChangedListener mListener;

    public DayRatingSelector(Context context, LinearLayout parent, OnRatingChangedListener listener) {
        this.mColors = new ColorBlendHelper(mMaxRating).blendColors();
        this.mContext = context;
        this.mParent = parent;
        this.mListener = listener;
    }

    public abstract void setRating(int rating);
    public abstract void resetSelected();

    void setButtonBackground(Button button, int color) {
        if (button != null) {
            ColorDrawable drawable = (ColorDrawable) button.getBackground();
            drawable.setColor(color);
        }
    }

    public int getColorForRating(int rating) {
        int colorIndex = rating-1;

        /* If index is not out of range of colors array */
        if (colorIndex < mColors.length) {
            return mColors[colorIndex];
        }
        return -1;
    }

    public interface OnRatingChangedListener {
        void ratingChanged(int rating);
    }
}
