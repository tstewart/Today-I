package io.github.tstewart.todayi.ui.decorators;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;

/*
 Splits a HashMap of CalendarDays and Integers into a list of DayRatedDecorators
 A DayRatedDecorator is created for every rating

 There is probably a much simpler way to do this, however the MaterialCalendarView api is restrictive.
 The only sensible way to decorate 5 different colors is to have 5 different decorators. Why? I don't know.
*/
public class DayRatingSplitter {

    /*
     Maximum selectable rating
     */
    final int MAX_RATING = UserPreferences.getMaxDayRating();
    final Context mContext;
    /* Array of colors to assign to rating */
    int[] mColors;


    public DayRatingSplitter(@NonNull Context context) {
        this.mContext = context;

        mColors = new ColorBlendHelper(MAX_RATING).blendColors();
    }

    public List<DayRatedDecorator> getDayRatingDecorators(Map<CalendarDay, Integer> ratings) {

        List<DayRatedDecorator> decorators = new ArrayList<>();

        for (int i = 1; i <= MAX_RATING; i++) {
            /* List of days that match the rating we are currently looking for */
            List<CalendarDay> daysMatchingRating = new ArrayList<>();

            /* Iterate over entries in ratings */
            for (Map.Entry<CalendarDay, Integer> entry : ratings.entrySet()) {
                /* If key's value equals index, add it to the list of CalendarDays that match this rating */
                if (entry.getValue() == i) {
                    daysMatchingRating.add(entry.getKey());
                }
            }

            /* Get color correlated to this current index */
            int color = getColorAtIndex(i - 1);

            /* Get drawable to add to DayRatedDecorator with the provided color */
            Drawable ratingDrawable = getRatingDrawable(color);

            decorators.add(new DayRatedDecorator(daysMatchingRating, ratingDrawable));
        }

        return decorators;
    }

    /*
    Creates a circle drawable with the provided color
    This will be used in the DayRatedDecorator to set the Calendar day view's background
     */
    private Drawable getRatingDrawable(int color) {
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.calendar_rated_circle);

        if (drawable != null) {
            /* Make a clone, to ensure updating the color doesn't break every other instance */
            drawable = (GradientDrawable) drawable.mutate();
            drawable.setColor(color);
        }

        return drawable;
    }

    /**
     * Get color associated with the current index
     *
     * @param index Index to check color for
     * @return Color that matches this index
     */
    public int getColorAtIndex(int index) {
        if (index < 0 || index >= mColors.length) {
            return mContext.getColor(R.color.colorTransparent);
        }
        return mColors[index];
    }
}
