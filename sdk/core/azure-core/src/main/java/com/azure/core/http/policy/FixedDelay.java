// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationHelpers;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

import static com.azure.core.util.Configuration.NONE;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelay.class);;
    private final static ConfigurationProperty<Integer> MAX_RETRIES_CONFIG = ConfigurationProperty.integerProperty("http-retry.fixed.max-retries",  null, null, LOGGER);
    private final static ConfigurationProperty<Duration> RETRY_DELAY_CONFIG = ConfigurationProperty.durationProperty("http-retry.fixed.delay", null, null, LOGGER);

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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
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

        if(!ConfigurationHelpers.containsAll(configuration, MAX_RETRIES_CONFIG, RETRY_DELAY_CONFIG)) {
            // TODO(configuration) log error and fail
        }

        return new FixedDelay(configuration.get(MAX_RETRIES_CONFIG), configuration.get(RETRY_DELAY_CONFIG));
    }
}
