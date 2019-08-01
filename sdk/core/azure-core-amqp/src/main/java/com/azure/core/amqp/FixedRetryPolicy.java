// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A policy to govern retrying of messaging operations in which the base delay between retries remains the same.
 */
public final class FixedRetryPolicy extends RetryPolicy {
    /**
     * Creates an instance with the given retry options.
     *
     * @param retryOptions The options to set on this retry policy.
     */
    public FixedRetryPolicy(RetryOptions retryOptions) {
        super(retryOptions);
    }

    /**
     * Calculates the delay for a fixed backoff.
     *
     * @param retryCount The number of attempts that have been made, including the initial attempt before any
     *         retries.
     * @param baseDelay The delay to use for the fixed backoff.
     * @param baseJitter The duration to use for the basis of the random jitter value.
     * @param random The random number generator used to calculate the jitter.
     * @return The duration to delay before retrying a request.
     */
    @Override
    protected Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter, ThreadLocalRandom random) {
        final Double jitterNanos = random.nextDouble() * baseJitter.getSeconds() * RetryPolicy.NANOS_PER_SECOND;
        final Duration jitter = Duration.ofNanos(jitterNanos.longValue());

        return baseDelay.plus(jitter);
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

        return obj instanceof FixedRetryPolicy
            && super.equals(obj);
    }

    /**
     * Creates a clone of this instance.
     *
     * @return A clone of the {@link FixedRetryPolicy} instance.
     */
    @Override
    public RetryPolicy clone() {
        final RetryOptions cloned = getRetryOptions().clone();
        return new FixedRetryPolicy(cloned);
    }
}
