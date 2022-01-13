// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import java.time.Duration;
import java.util.Objects;

import static com.azure.core.util.Configuration.NONE;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {
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
            ClientLogger logger = new ClientLogger(FixedDelay.class);
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

    static RetryStrategy fromConfiguration(Configuration configuration, RetryStrategy defaultStrategy) {
        if (configuration == null || configuration == NONE) {
            return defaultStrategy;
        }

        String maxRetriesStr = configuration.get("http.retry.strategy.fixed.max-retries");
        String delayStr = configuration.get( "http.retry.strategy.fixed.delay");

        if (maxRetriesStr == null || delayStr == null) {
            // TODO(configuration) log error and fail
        }

        int maxRetries = Integer.parseInt(maxRetriesStr);
        // TODO(configuration) fail on error

        Duration delay = Duration.parse(delayStr);
        // TODO(configuration) fail on error

        return new FixedDelay(maxRetries, delay);
    }
}
