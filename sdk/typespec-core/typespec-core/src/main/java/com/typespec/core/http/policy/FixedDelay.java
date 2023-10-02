// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.util.logging.ClientLogger;
import java.time.Duration;
import java.util.Objects;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelay.class);

    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param delay The fixed delay duration between retry attempts.
     * @throws IllegalArgumentException If {@code maxRetries} is negative.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelay(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param fixedDelayOptions The {@link FixedDelayOptions}.
     */
    public FixedDelay(FixedDelayOptions fixedDelayOptions) {
        this(
            Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getMaxRetries(),
            Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getDelay()
        );
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        return delay;
    }
}
