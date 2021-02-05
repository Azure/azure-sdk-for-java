package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;

public final class UpdateSessionOptions {
    /*
     * Update to the time the session will run after it reached the 'Ready'
     * state. It has to be bigger than the current value of
     * maxLeaseTimeMinutes.
     */
    private Duration maxLeaseTime = Duration.ofMinutes(10);

    /**
     * Set the maxLeaseTime property: Update to the time the session will run after it reached the 'Ready' state.
     * It has to be bigger than the current value of maxLeaseTime.
     *
     * @return the maxLeaseTime value.
     */
    public UpdateSessionOptions maxLeaseTime(Duration maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
        return this;
    }

    /**
     * Get the maxLeaseTimeMinutes property: Update to the time the session will run after it reached the 'Ready' state.
     * It has to be bigger than the current value of maxLeaseTime.
     *
     * @return the maxLeaseTimeMinutes value.
     */
    public Duration getMaxLeaseTime() {
        return this.maxLeaseTime;
    }
}
