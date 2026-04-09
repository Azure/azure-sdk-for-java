// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Checkpoint strategy that bounds the replay window by a maximum time interval.
 */
public final class TimeIntervalCheckpointStrategy extends ChangeFeedCheckpointStrategy {
    private final Duration maxCheckpointDelay;

    /**
     * Creates a new time-interval checkpoint strategy.
     *
     * @param maxCheckpointDelay the maximum allowed replay window duration.
     */
    public TimeIntervalCheckpointStrategy(Duration maxCheckpointDelay) {
        checkNotNull(maxCheckpointDelay, "Argument 'maxCheckpointDelay' can not be null");
        if (maxCheckpointDelay.isZero() || maxCheckpointDelay.isNegative()) {
            throw new IllegalArgumentException("Argument 'maxCheckpointDelay' must be positive");
        }

        this.maxCheckpointDelay = maxCheckpointDelay;
    }

    /**
     * Gets the maximum replay window.
     *
     * @return the maximum replay window.
     */
    public Duration getMaxCheckpointDelay() {
        return this.maxCheckpointDelay;
    }
}

