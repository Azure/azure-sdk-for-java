// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * The configuration for a fixed-delay retry that has a fixed delay duration between each retry attempt.
 */
public class FixedDelayOptions {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelayOptions.class);
    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates an instance of {@link FixedDelayOptions}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param delay The fixed delay duration between retry attempts.
     * @throws IllegalArgumentException If {@code maxRetries} is negative.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelayOptions(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public Duration getDelay() {
        return delay;
    }
}
