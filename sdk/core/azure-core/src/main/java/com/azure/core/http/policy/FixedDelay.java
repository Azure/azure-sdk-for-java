// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelay.class);;
    private final static ConfigurationProperty<Integer> MAX_RETRIES_CONFIG = ConfigurationProperty.integerPropertyBuilder("http-retry.fixed.max-retries")
        .global(true)
        .required(true)
        .build();
    private final static ConfigurationProperty<Duration> RETRY_DELAY_CONFIG = ConfigurationProperty.durationPropertyBuilder("http-retry.fixed.delay")
        .global(true)
        .required(true)
        .build();

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

    static RetryStrategy fromConfiguration(Configuration configuration ) {
        return new FixedDelay(configuration.get(MAX_RETRIES_CONFIG), configuration.get(RETRY_DELAY_CONFIG));
    }
}
