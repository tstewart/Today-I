package io.github.tstewart.todayi.events;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import io.github.tstewart.todayi.interfaces.OnDatabaseInteractionListener;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;

/*
Event class. To be called when an interaction is made on the database (e.g. adding/deleting entries)
 */
public class OnDatabaseInteracted {
    private static final List<OnDatabaseInteractionListener> sListeners = new ArrayList<>();

    /*
    Add listener to list of listeners waiting for event.
     */
    public static void addListener(OnDatabaseInteractionListener listener) {
        sListeners.add(listener);
    }

    /*
    Alert all listeners that the event has been called
     */
    public static void notifyDatabaseInteracted() {
        for (OnDatabaseInteractionListener listener :
                sListeners) {
            listener.onDatabaseInteracted();
        }
    }
}
