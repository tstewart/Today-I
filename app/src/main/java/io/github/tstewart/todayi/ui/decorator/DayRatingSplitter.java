package io.github.tstewart.todayi.ui.decorator;

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

// There is probably a much simpler way to do this, however the MaterialCalendarView api is pretty bad.
// The only sensible way to decorate 5 different colors is to have 5 different decorators. Why? I don't know.
public class DayRatingSplitter {

    final Context context;

    public DayRatingSplitter(@NonNull Context context) {
        this.context = context;
    }

    public List<DayRatedDecorator> getDayRatingDecorators(HashMap<CalendarDay, Integer> ratings) {

        List<DayRatedDecorator> decorators = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            // variables inside lambda must be final
            final int index = i;
            List<CalendarDay> daysMatchingRating = new ArrayList<>();

            ratings.forEach((calendarDay, rating) -> {
                if(rating == index) {
                    daysMatchingRating.add(calendarDay);
                }
            });

            int color = getColorAtIndex(index);

            Drawable ratingDrawable = getRatingDrawable(color);

            decorators.add(new DayRatedDecorator(daysMatchingRating,ratingDrawable));
        }

        //May god have mercy on me
        return decorators;
    }

    private Drawable getRatingDrawable(int color) {
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.calendar_rated_circle);

        if (drawable != null) {
            // Make a clone, to ensure updating the color doesn't break every other instance
            drawable = (GradientDrawable) drawable.mutate();
            drawable.setColor(color);
        }

        return drawable;
    }

    public int getColorAtIndex(int index) {
        switch(index) {
            case 1:
                return ContextCompat.getColor(context, R.color.colorRatingRed);
            case 2:
                return ContextCompat.getColor(context, R.color.colorRatingOrange);
            case 3:
                return ContextCompat.getColor(context, R.color.colorRatingYellow);
            case 4:
                return ContextCompat.getColor(context, R.color.colorRatingLightGreen);
            case 5:
                return ContextCompat.getColor(context, R.color.colorRatingGreen);
            default:
                return ContextCompat.getColor(context, R.color.colorTransparent);
        }
    }
}
