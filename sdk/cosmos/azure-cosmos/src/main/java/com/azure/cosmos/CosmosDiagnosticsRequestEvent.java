// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import java.time.Duration;
import java.time.Instant;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class CosmosDiagnosticsRequestEvent {

    private final Instant startTime;
    private final Duration duration;
    private final String name;

    CosmosDiagnosticsRequestEvent(
        Instant startTime,
        Duration duration,
        String name) {

        checkNotNull(startTime, "Argument 'startTime' must not be null.");
        checkNotNull(name, "Argument 'name' must not be null.");
        this.startTime = startTime;
        this.duration = duration;
        this.name = name;
    }

    /**
     * Gets the start time of the request pipeline event
     * @return the start time of the request pipeline event
     */
    public Instant getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the duration for the request pipeline event - or null when the pipeline event hasn't finished (yet).
     * @return the duration for the request pipeline event or null when the pipeline event hasn't finished (yet).
     */
    public Duration getDuration() {
        return this.duration;
    }

    /**
     * Gets the name of the request pipeline event.
     * @return the name of the request pipeline event.
     */
    public String getEventName() {
        return this.name;
    }

}
