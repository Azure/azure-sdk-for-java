// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/** Options for updating an existing rendering session. */
@Fluent
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
     * @param maxLeaseTime the maxLeaseTime value
     * @return this UpdateSessionOptions object.
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
