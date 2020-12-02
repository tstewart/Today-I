package io.github.tstewart.todayi.events;

import java.util.ArrayList;
import java.util.List;

import io.github.tstewart.todayi.interfaces.OnDatabaseInteractionListener;

/*
Event class. To be called when an interaction is made on the database (e.g. adding/deleting entries)
 */
public class OnDatabaseInteracted {
    /* List of listeners registered to this event */
    private static final List<OnDatabaseInteractionListener> sListeners = new ArrayList<>();

    /* Private constructor prevents initialisation of event class */
    private OnDatabaseInteracted() {
    }

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
