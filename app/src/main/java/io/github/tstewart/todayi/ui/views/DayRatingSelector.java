package io.github.tstewart.todayi.ui.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.widget.Button;
import android.widget.LinearLayout;

import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;

/*
Generic class for day rating selector views.
 */
public abstract class DayRatingSelector {

    final int[] mColors;
    final Context mContext;
    int mMaxRating;
    LinearLayout mParent;
    OnRatingChangedListener mListener;

    protected DayRatingSelector(Context context, LinearLayout parent, OnRatingChangedListener listener) {
        this.mMaxRating = UserPreferences.getMaxDayRating();
        this.mColors = new ColorBlendHelper(mMaxRating).blendColors();
        this.mContext = context;
        this.mParent = parent;
        this.mListener = listener;
    }

    public abstract void setRating(int rating);
    public abstract void resetSelected();

    void setButtonBackground(Button button, int color) {
        if (button != null) {
            Drawable drawable = button.getBackground();

            if(drawable instanceof ColorDrawable) ((ColorDrawable)drawable).setColor(color);
            else if(drawable instanceof GradientDrawable) ((GradientDrawable)drawable).setColor(color);
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
