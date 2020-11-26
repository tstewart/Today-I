package io.github.tstewart.todayi.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnDateChanged {
    public static final List<OnDateChangedListener> sListeners = new ArrayList<>();

    public static void addListener(OnDateChangedListener listener) {
        sListeners.add(listener);
    }

    public static void notifyDatabaseInteracted(Date date) {
        sListeners.forEach(listener -> listener.onDateChanged(date));
    }
}
