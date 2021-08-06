// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;
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
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);
    private final ClientLogger logger = new ClientLogger(ExponentialBackoff.class);

    private final int maxRetries;
    private final Duration baseDelay;
    private final Duration maxDelay;

    /**
     * Creates an instance of {@link ExponentialBackoff} with a maximum of three retry attempts. This strategy starts
     * with a delay of 800 milliseconds and exponentially increases with each additional retry attempt.
     */
    public ExponentialBackoff() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * Creates an instance of {@link ExponentialBackoff}.
     *
     * @param maxRetries The max retry attempts that can be made.
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0 or {@code baseDelay} is less than or
     * equal to 0 or {@code maxDelay} is less than {@code baseDelay}.
     */
    public ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        if (maxRetries < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");

        if (baseDelay.isZero() || baseDelay.isNegative()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be negative or 0."));
        }

        if (baseDelay.compareTo(maxDelay) > 0) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
        }
        this.maxRetries = maxRetries;
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (baseDelay.toNanos() * (1 - JITTER_FACTOR)),
                (long) (baseDelay.toNanos() * (1 + JITTER_FACTOR)));
        Duration delay = Duration.ofNanos(Math.min((1 << retryAttempts) * delayWithJitterInNanos,
            maxDelay.toNanos()));
        return delay;
    }
}
