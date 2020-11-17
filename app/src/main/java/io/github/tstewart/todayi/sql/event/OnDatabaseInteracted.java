package io.github.tstewart.todayi.sql.event;

import java.util.ArrayList;
import java.util.List;

public class OnDatabaseInteracted {
    private static List<OnDatabaseInteractionListener> listeners = new ArrayList<>();

    public static void addListener(OnDatabaseInteractionListener listener) {
        listeners.add(listener);
    }

    public static void notifyDatabaseInteracted() {
        listeners.forEach(OnDatabaseInteractionListener::onDatabaseInteracted);
    }
}
