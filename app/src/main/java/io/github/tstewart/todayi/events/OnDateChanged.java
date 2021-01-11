package io.github.tstewart.todayi.events;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import io.github.tstewart.todayi.interfaces.OnDateChangedListener;

/*
Event class. To be called when the current selected date is changed.
The current date selected is controlled by MainActivity.
 */
public class OnDateChanged {
    /* List of listeners registered to this event */
    private static final List<OnDateChangedListener> sListeners = new ArrayList<>();

    /* Private constructor prevents initialisation of event class */
    private OnDateChanged() {
    }

    /*
    Add listener to list of listeners waiting for event.
    */
    public static void addListener(OnDateChangedListener listener) {
        sListeners.add(listener);
    }

    /*
    Alert all listeners that the event has been called
    */
    public static void notifyDateChanged(LocalDate date) {
        for (OnDateChangedListener listener :
             sListeners) {
            listener.onDateChanged(date);
        }
    }
}
