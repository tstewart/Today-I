package io.github.tstewart.todayi.ui.decorators;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.List;

/*
Decorates day views from MaterialCalendarView
Sets day background to the provided Drawable
 */
public class DayRatedDecorator implements DayViewDecorator {

    final List<CalendarDay> mDates;
    final Drawable mDrawable;

    public DayRatedDecorator(List<CalendarDay> dates, Drawable drawable) {
        this.mDates = dates;
        this.mDrawable = drawable;
    }

    /* In what cases should this decoration be drawn */
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return mDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(mDrawable);
        view.addSpan(new ForegroundColorSpan(Color.BLACK));
    }
}
