package com.azure.mixedreality.remoterendering.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionUpdateOptions {
    /*
     * Update to the time the session will run after it reached the 'Ready'
     * state. It has to be bigger than the current value of
     * maxLeaseTimeMinutes.
     */
    private final int maxLeaseTimeMinutes;

    /**
     * Creates an instance of UpdateSessionSettings class.
     *
     * @param maxLeaseTimeMinutes the maxLeaseTimeMinutes value to set.
     */
    public SessionUpdateOptions(int maxLeaseTimeMinutes) {
        this.maxLeaseTimeMinutes = maxLeaseTimeMinutes;
    }

    /**
     * Get the maxLeaseTimeMinutes property: Update to the time the session will run after it reached the 'Ready' state.
     * It has to be bigger than the current value of maxLeaseTimeMinutes.
     *
     * @return the maxLeaseTimeMinutes value.
     */
    public int getMaxLeaseTimeMinutes() {
        return this.maxLeaseTimeMinutes;
    }
}
