package io.github.tstewart.todayi.interfaces;

import org.threeten.bp.LocalDate;

import java.util.Date;

/**
 * Event interface. Called application-wide selected date changed (when registered with OnDateChanged)
 */
public interface OnDateChangedListener {
    /**
     * Perform function on date changed
     */
    void onDateChanged(LocalDate date);
}
