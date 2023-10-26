// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy.retry;

import com.generic.core.http.policy.retry.ExponentialBackoffOptions;
import com.generic.core.implementation.util.CoreUtils;
import com.generic.core.util.configuration.Configuration;
import com.generic.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A truncated exponential backoff implementation of {@link RetryStrategy} that has a delay duration that exponentially
 * increases with each retry attempt until an upper bound is reached after which every retry attempt is delayed by the
 * provided max delay duration.
 */
public class ExponentialBackoff implements RetryStrategy {
    private static final double JITTER_FACTOR = 0.05;
    private static final int DEFAULT_MAX_RETRIES;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);
    private static final ClientLogger LOGGER = new ClientLogger(ExponentialBackoff.class);

    static {
        String envDefaultMaxRetries = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_REQUEST_RETRY_COUNT);

        int defaultMaxRetries = 3;
        if (!CoreUtils.isNullOrEmpty(envDefaultMaxRetries)) {
            try {
                defaultMaxRetries = Integer.parseInt(envDefaultMaxRetries);
                if (defaultMaxRetries < 0) {
                    defaultMaxRetries = 3;
                }
            } catch (NumberFormatException ignored) {
//                LOGGER.verbose("{} was loaded but is an invalid number. Using 3 retries as the maximum.",
//                    Configuration.PROPERTY_REQUEST_RETRY_COUNT);
            }
        }

        DEFAULT_MAX_RETRIES = defaultMaxRetries;
    }

    private final int maxRetries;
    private final long baseDelayNanos;
    private final long maxDelayNanos;

    /**
     * Creates an instance of {@link ExponentialBackoff} with a maximum number of retry attempts configured by the
     * environment property {@link Configuration#PROPERTY_REQUEST_RETRY_COUNT}, or three if it isn't configured or
     * is less than or equal to 0. This strategy starts with a delay of 800 milliseconds and exponentially increases
     * with each additional retry attempt to a maximum of 8 seconds.
     */
    public ExponentialBackoff() {
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.baseDelayNanos = DEFAULT_BASE_DELAY.toNanos();
        this.maxDelayNanos = DEFAULT_MAX_DELAY.toNanos();
    }

    /**
     * Creates an instance of {@link ExponentialBackoff}.
     *
     * @param options The {@link ExponentialBackoffOptions}.
     * @throws NullPointerException if {@code options} is {@code null}.
     */
    public ExponentialBackoff(ExponentialBackoffOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.maxRetries = options.getMaxRetries();
        this.baseDelayNanos = options.getBaseDelay().toNanos();
        this.maxDelayNanos = options.getMaxDelay().toNanos();
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (baseDelayNanos * (1 - JITTER_FACTOR)), (long) (baseDelayNanos * (1 + JITTER_FACTOR)));
        return Duration.ofNanos(Math.min((1L << retryAttempts) * delayWithJitterInNanos, maxDelayNanos));
    }
}
