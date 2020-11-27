package io.github.tstewart.todayi.events;

import java.util.ArrayList;
import java.util.List;

import io.github.tstewart.todayi.interfaces.OnDatabaseInteractionListener;

public class OnDatabaseInteracted {
    private static final List<OnDatabaseInteractionListener> sListeners = new ArrayList<>();

    public static void addListener(OnDatabaseInteractionListener listener) {
        sListeners.add(listener);
    }

    public static void notifyDatabaseInteracted() {
        sListeners.forEach(OnDatabaseInteractionListener::onDatabaseInteracted);
    }
}
