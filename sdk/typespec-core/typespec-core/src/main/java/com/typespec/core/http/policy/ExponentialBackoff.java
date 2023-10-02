// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.implementation.util.ObjectsUtil;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.typespec.core.util.Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT;

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
        String envDefaultMaxRetries = Configuration.getGlobalConfiguration().get(PROPERTY_AZURE_REQUEST_RETRY_COUNT);

        int defaultMaxRetries = 3;
        if (!CoreUtils.isNullOrEmpty(envDefaultMaxRetries)) {
            try {
                defaultMaxRetries = Integer.parseInt(envDefaultMaxRetries);
                if (defaultMaxRetries < 0) {
                    defaultMaxRetries = 3;
                }
            } catch (NumberFormatException ignored) {
                LOGGER.verbose("{} was loaded but is an invalid number. Using 3 retries as the maximum.",
                    PROPERTY_AZURE_REQUEST_RETRY_COUNT);
            }
        }

        DEFAULT_MAX_RETRIES = defaultMaxRetries;
    }

    private final int maxRetries;
    private final long baseDelayNanos;
    private final long maxDelayNanos;

    /**
     * Creates an instance of {@link ExponentialBackoff} with a maximum number of retry attempts configured by the
     * environment property {@link Configuration#PROPERTY_AZURE_REQUEST_RETRY_COUNT}, or three if it isn't configured or
     * is less than or equal to 0. This strategy starts with a delay of 800 milliseconds and exponentially increases
     * with each additional retry attempt to a maximum of 8 seconds.
     */
    public ExponentialBackoff() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * Creates an instance of {@link ExponentialBackoff}.
     *
     * @param options The {@link ExponentialBackoffOptions}.
     * @throws NullPointerException if {@code options} is {@code null}.
     */
    public ExponentialBackoff(ExponentialBackoffOptions options) {
        this(
            ObjectsUtil.requireNonNullElse(
                Objects.requireNonNull(options, "'options' cannot be null.").getMaxRetries(),
                DEFAULT_MAX_RETRIES),
            ObjectsUtil.requireNonNullElse(
                Objects.requireNonNull(options, "'options' cannot be null.").getBaseDelay(),
                DEFAULT_BASE_DELAY),
            ObjectsUtil.requireNonNullElse(
                Objects.requireNonNull(options, "'options' cannot be null.").getMaxDelay(),
                DEFAULT_MAX_DELAY)
        );
    }

    /**
     * Creates an instance of {@link ExponentialBackoff}.
     *
     * @param maxRetries The max retry attempts that can be made.
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0 or {@code baseDelay} is less than or equal
     * to 0 or {@code maxDelay} is less than {@code baseDelay}.
     */
    public ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        if (maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");

        if (baseDelay.isZero() || baseDelay.isNegative()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be negative or 0."));
        }

        if (baseDelay.compareTo(maxDelay) > 0) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
        }
        this.maxRetries = maxRetries;
        this.baseDelayNanos = baseDelay.toNanos();
        this.maxDelayNanos = maxDelay.toNanos();
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
