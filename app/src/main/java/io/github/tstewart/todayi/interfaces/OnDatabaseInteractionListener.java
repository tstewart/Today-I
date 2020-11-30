package io.github.tstewart.todayi.interfaces;

/**
 * Event interface. Called on Database interaction (when registered with OnDatabaseInteracted)
 */
public interface OnDatabaseInteractionListener {
    /**
     * Perform function on database interacted
     */
    void onDatabaseInteracted();
}
