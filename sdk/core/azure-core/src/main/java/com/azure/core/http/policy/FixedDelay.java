// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;
import java.time.Duration;
import java.util.Objects;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {

    private final ClientLogger logger = new ClientLogger(FixedDelay.class);
    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param delay The fixed delay duration between retry attempts.
     */
    public FixedDelay(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
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
