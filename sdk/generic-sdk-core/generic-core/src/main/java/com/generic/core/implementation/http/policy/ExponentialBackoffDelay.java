// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.policy.RetryPolicy;
import com.generic.core.util.configuration.Configuration;
import com.generic.core.util.ClientLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A truncated exponential backoff implementation of {@link RetryPolicy.RetryStrategy} that has a delay duration that exponentially
 * increases with each retry attempt until an upper bound is reached after which every retry attempt is delayed by the
 * provided max delay duration.
 */
public class ExponentialBackoffDelay implements RetryPolicy.RetryStrategy {
    private static final double JITTER_FACTOR = 0.05;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);
    private static final ClientLogger LOGGER = new ClientLogger(ExponentialBackoffDelay.class);
    private final long baseDelayNanos;
    private final long maxDelayNanos;

    /**
     * Creates an instance of {@link ExponentialBackoffDelay} with a maximum number of retry attempts configured by the
     * environment property {@link Configuration#PROPERTY_REQUEST_RETRY_COUNT}, or three if it isn't configured or
     * is less than or equal to 0. This strategy starts with a delay of 800 milliseconds and exponentially increases
     * with each additional retry attempt to a maximum of 8 seconds.
     */
    public ExponentialBackoffDelay() {
        this(DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY);
    }

//    /**
//     * Creates an instance of {@link ExponentialBackoff}.
//     *
//     * @param options The {@link RetryPolicy.ExponentialBackoffOptions}.
//     * @throws NullPointerException if {@code options} is {@code null}.
//     */
//    public ExponentialBackoff(RetryPolicy.ExponentialBackoffOptions options) {
//        this(
//            ObjectsUtil.requireNonNullElse(
//                Objects.requireNonNull(options, "'options' cannot be null.").getBaseDelay(),
//                DEFAULT_BASE_DELAY),
//            ObjectsUtil.requireNonNullElse(
//                Objects.requireNonNull(options, "'options' cannot be null.").getMaxDelay(),
//                DEFAULT_MAX_DELAY)
//        );
//    }

    /**
     * Creates an instance of {@link ExponentialBackoffDelay}.
     *
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0 or {@code baseDelay} is less than or equal
     * to 0 or {@code maxDelay} is less than {@code baseDelay}.
     */
    public ExponentialBackoffDelay(Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");

        if (baseDelay.isZero() || baseDelay.isNegative()) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'baseDelay' cannot be negative or 0."));
        }

        if (baseDelay.compareTo(maxDelay) > 0) {
            throw LOGGER
                .logThrowableAsError(new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
        }
        this.baseDelayNanos = baseDelay.toNanos();
        this.maxDelayNanos = maxDelay.toNanos();
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (baseDelayNanos * (1 - JITTER_FACTOR)), (long) (baseDelayNanos * (1 + JITTER_FACTOR)));
        return Duration.ofNanos(Math.min((1L << retryAttempts) * delayWithJitterInNanos, maxDelayNanos));
    }
}
