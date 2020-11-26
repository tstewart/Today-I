package io.github.tstewart.todayi.event;

import java.util.ArrayList;
import java.util.List;

public class OnDatabaseInteracted {
    private static final List<OnDatabaseInteractionListener> sListeners = new ArrayList<>();

    public static void addListener(OnDatabaseInteractionListener listener) {
        sListeners.add(listener);
    }

    public static void notifyDatabaseInteracted() {
        sListeners.forEach(OnDatabaseInteractionListener::onDatabaseInteracted);
    }
}
