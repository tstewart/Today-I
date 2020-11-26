package io.github.tstewart.todayi.ui.decorator;

import android.graphics.Color;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.List;

public class DayPostedDecorator implements DayViewDecorator {

    final List<CalendarDay> mDates;

    public DayPostedDecorator(List<CalendarDay> dates) {
        this.mDates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return mDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(10, Color.BLACK));
    }
}
