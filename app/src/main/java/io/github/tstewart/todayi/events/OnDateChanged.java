package io.github.tstewart.todayi.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.tstewart.todayi.interfaces.OnDateChangedListener;

/*
Event class. To be called when the current selected date is changed.
The current date selected is controlled by MainActivity.
 */
public class OnDateChanged {
    public static final List<OnDateChangedListener> sListeners = new ArrayList<>();

    /*
    Add listener to list of listeners waiting for event.
    */
    public static void addListener(OnDateChangedListener listener) {
        sListeners.add(listener);
    }

    /*
    Alert all listeners that the event has been called
    */
    public static void notifyDatabaseInteracted(Date date) {
        sListeners.forEach(listener -> listener.onDateChanged(date));
    }
}
