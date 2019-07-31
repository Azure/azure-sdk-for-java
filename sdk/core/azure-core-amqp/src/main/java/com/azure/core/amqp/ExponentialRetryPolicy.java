// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A policy to govern retrying of messaging operations in which the delay between retries will grow in an exponential
 * manner, allowing more time to recover as the number of retries increases.
 */
public final class ExponentialRetryPolicy extends RetryPolicy {
    private final double retryFactor;

    /**
     * Creates a new instance with a minimum and maximum retry period in addition to maximum number of retry attempts.
     *
     * @param retryOptions The options to apply to this retry policy.
     * @throws NullPointerException if {@code retryOptions} is {@code null}.
     */
    public ExponentialRetryPolicy(RetryOptions retryOptions) {
        super(retryOptions);

        this.retryFactor = computeRetryFactor();
    }

    /**
     * Calculates the retry delay using exponential backoff.
     *
     * @param retryCount The number of attempts that have been made, including the initial attempt before any
     *         retries.
     * @param baseDelay The delay to use for the basis of the exponential backoff.
     * @param baseJitter The duration to use for the basis of the random jitter value.
     * @param random The random number generator used to calculate the jitter.
     * @return The duration to delay before retrying a request.
     */
    @Override
    protected Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter,
                                           ThreadLocalRandom random) {
        final double jitterSeconds = random.nextDouble() * baseJitter.getSeconds();
        final double nextRetrySeconds = Math.pow(retryFactor, (double) retryCount);
        final Double nextRetryNanos = (jitterSeconds + nextRetrySeconds) * NANOS_PER_SECOND;

        return baseDelay.plus(Duration.ofNanos(nextRetryNanos.longValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof ExponentialRetryPolicy
            && super.equals(obj);
    }

    /**
     * Creates a clone of this instance.
     *
     * @return A clone of the {@link ExponentialRetryPolicy} instance.
     */
    @Override
    public RetryPolicy clone() {
        final RetryOptions cloned = getRetryOptions().clone();
        return new ExponentialRetryPolicy(cloned);
    }

    private double computeRetryFactor() {
        final RetryOptions options = getRetryOptions();
        final Duration maxBackoff = options.maxDelay();
        final Duration minBackoff = options.delay();
        final int maximumRetries = options.maxRetries();
        final long deltaBackoff = maxBackoff.minus(minBackoff).getSeconds();

        if (deltaBackoff <= 0 || maximumRetries <= 0) {
            return 0;
        }

        return Math.log(deltaBackoff) / Math.log(maximumRetries);
    }
}
