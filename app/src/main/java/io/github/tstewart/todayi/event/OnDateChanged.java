package io.github.tstewart.todayi.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnDateChanged {
    public static List<OnDateChangedListener> listeners = new ArrayList<>();

    public static void addListener(OnDateChangedListener listener) {
        listeners.add(listener);
    }

    public static void notifyDatabaseInteracted(Date date) {
        listeners.forEach(listener -> listener.onDateChanged(date));
    }
}
