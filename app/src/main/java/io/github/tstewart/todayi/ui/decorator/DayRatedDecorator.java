package io.github.tstewart.todayi.ui.decorator;

import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.List;

public class DayRatedDecorator implements DayViewDecorator {

    final List<CalendarDay> dates;
    final Drawable drawable;

    public DayRatedDecorator(List<CalendarDay> dates, Drawable drawable) {
        this.dates = dates;
        this.drawable = drawable;
    }
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
    }
}
