// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.time.Duration;
import java.time.Instant;

class TimeoutTracker {
    private final Duration originalTimeout;
    private boolean isTimerStarted;
    private Instant startTime;

    /**
     * Creates an instance to keep track of timed out sends.
     *
     * @param timeout original operationTimeout
     * @param startTrackingTimeout whether/not to start the timeout tracking - right now. If not started now, timer
     * tracking will start upon the first call to {@link TimeoutTracker#elapsed()}/{@link TimeoutTracker#remaining()}
     */
    TimeoutTracker(Duration timeout, boolean startTrackingTimeout) {
        if (timeout.compareTo(Duration.ZERO) < 0) {
            throw new IllegalArgumentException("timeout should be non-negative");
        }

        this.originalTimeout = timeout;

        if (startTrackingTimeout) {
            this.startTime = Instant.now();
        }

        this.isTimerStarted = startTrackingTimeout;
    }

    Duration remaining() {
        return this.originalTimeout.minus(elapsed());
    }

    Duration elapsed() {
        if (!this.isTimerStarted) {
            this.startTime = Instant.now();
            this.isTimerStarted = true;
        }

        return Duration.between(this.startTime, Instant.now());
    }
}
