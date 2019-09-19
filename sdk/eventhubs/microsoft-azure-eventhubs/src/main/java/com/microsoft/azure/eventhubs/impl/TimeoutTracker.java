// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.time.Duration;
import java.time.Instant;

public class TimeoutTracker {
    private final Duration originalTimeout;
    private boolean isTimerStarted;
    private Instant startTime;
    private final String context;

    /**
     * @param timeout              original operationTimeout
     * @param startTrackingTimeout whether/not to start the timeout tracking - right now. if not started now, timer tracking will start upon the first call to {@link TimeoutTracker#elapsed()}/{@link TimeoutTracker#remaining()}
     */
    public TimeoutTracker(Duration timeout, boolean startTrackingTimeout) {
        this(timeout, startTrackingTimeout, null);
    }

    public TimeoutTracker(Duration timeout, boolean startTrackingTimeout, String context)
    {
        this.context = context;

        if (timeout.compareTo(Duration.ZERO) < 0) {
            throw new IllegalArgumentException("timeout should be non-negative");
        }

        this.originalTimeout = timeout;

        if (startTrackingTimeout) {
            this.startTime = Instant.now();
        }

        this.isTimerStarted = startTrackingTimeout;
    }

    public static TimeoutTracker create(Duration timeout) {
        return create(timeout, null);
    }

    public static TimeoutTracker create(Duration timeout, String context) {
        return new TimeoutTracker(timeout, true, context);
    }

    public Duration remaining() {
        return this.originalTimeout.minus(this.elapsed());
    }

    public Duration elapsed() {
        if (!this.isTimerStarted) {
            this.startTime = Instant.now();
            this.isTimerStarted = true;
        }

        return Duration.between(this.startTime, Instant.now());
    }

    public String getContext()
    {
        return this.context;
    }
}
