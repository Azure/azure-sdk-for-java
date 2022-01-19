// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ImmutableConfiguration;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

import static com.azure.core.util.Configuration.NONE;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {
    private static final String CONFIG_PREFIX = "http-retry.fixed";
    private final static ConfigurationProperty<Integer> MAX_RETRIES_CONFIG = ConfigurationProperty.integerProperty(CONFIG_PREFIX, "max-retries",  null, null);
    private final static ConfigurationProperty<Duration> RETRY_DELAY_CONFIG = ConfigurationProperty.durationProperty(CONFIG_PREFIX, "delay", null, null);

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

    static RetryStrategy fromConfiguration(ImmutableConfiguration configuration, RetryStrategy defaultStrategy) {
        if (configuration == null || configuration == NONE) {
            return defaultStrategy;
        }

        if(!configuration.contains(MAX_RETRIES_CONFIG) || !configuration.contains(RETRY_DELAY_CONFIG)) {
            // TODO(configuration) log error and fail
        }

        return new FixedDelay(configuration.get(MAX_RETRIES_CONFIG), configuration.get(RETRY_DELAY_CONFIG));
    }
}
