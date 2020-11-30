package io.github.tstewart.todayi.ui.decorators;

import android.graphics.Color;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.List;

/*
Decorates day views from MaterialCalendarView
Decorates all days with a black dot under the day view
 */
public class DayPostedDecorator implements DayViewDecorator {

    final List<CalendarDay> mDates;

    public DayPostedDecorator(List<CalendarDay> dates) {
        this.mDates = dates;
    }

    /* In what cases should this decoration be drawn */
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return mDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(10, Color.BLACK));
    }
}
