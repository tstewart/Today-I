package io.github.tstewart.todayi.ui.decorators;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.tstewart.todayi.R;

/*
 Splits a HashMap of CalendarDays and Integers into a list of DayRatedDecorators
 A DayRatedDecorator is created for every rating

 There is probably a much simpler way to do this, however the MaterialCalendarView api is restrictive.
 The only sensible way to decorate 5 different colors is to have 5 different decorators. Why? I don't know.
*/
/*
 TODO Remove restriction of this class (only accepts 1-5)
 TODO Ratings and their respective colors should be stored in a constant
*/
public class DayRatingSplitter {

    final Context mContext;

    public DayRatingSplitter(@NonNull Context context) {
        this.mContext = context;
    }

    public List<DayRatedDecorator> getDayRatingDecorators(HashMap<CalendarDay, Integer> ratings) {

        List<DayRatedDecorator> decorators = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            // variables inside lambda must be final, so the current for index is assigned again.
            final int index = i;
            // List of days that match the rating we are currently looking for
            List<CalendarDay> daysMatchingRating = new ArrayList<>();

            ratings.forEach((calendarDay, rating) -> {
                // If rating in the HashMap matches the current for index, add it to current list of CalendarDays
                if (rating == index) {
                    daysMatchingRating.add(calendarDay);
                }
            });

            // Get color correlated to this current index
            int color = getColorAtIndex(index);

            // Get drawable to add to DayRatedDecorator with the provided color
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
            // Make a clone, to ensure updating the color doesn't break every other instance
            drawable = (GradientDrawable) drawable.mutate();
            drawable.setColor(color);
        }

        return drawable;
    }

    /**
     * Get color associated with the current index
     * @param index Index to check color for
     * @return Color that matches this index
     */
    public int getColorAtIndex(int index) {
        int colorResourceId;
        switch (index) {
            case 1:
                colorResourceId = R.color.colorRatingRed;
                break;
            case 2:
                colorResourceId = R.color.colorRatingOrange;
                break;
            case 3:
                colorResourceId = R.color.colorRatingYellow;
                break;
            case 4:
                colorResourceId = R.color.colorRatingLightGreen;
                break;
            case 5:
                colorResourceId = R.color.colorRatingGreen;
                break;
            default:
                // Default to a transparent color
                colorResourceId = R.color.colorTransparent;
        }
        return ContextCompat.getColor(mContext, colorResourceId);
    }
}
