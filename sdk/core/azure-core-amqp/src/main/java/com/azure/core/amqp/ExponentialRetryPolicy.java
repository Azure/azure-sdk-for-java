// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;

import java.time.Duration;
import java.util.Objects;

/**
 * A policy to govern retrying of messaging operations in which the delay between retries will grow in an exponential
 * manner, allowing more time to recover as the number of retries increases.
 */
public final class ExponentialRetryPolicy extends RetryPolicy {
    // Base sleep wait time.
    private static final Duration SERVER_BUSY_WAIT_TIME = Duration.ofSeconds(4);
    private static final Duration TIMER_TOLERANCE = Duration.ofSeconds(1);

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
     * {@inheritDoc}
     */
    @Override
    public Duration calculateRetryDelay(Exception lastException, Duration remainingTime, int retryCount) {
        if (!isRetriableException(lastException) || retryCount >= retryOptions.maxRetries()) {
            return null;
        }

        if (!(lastException instanceof AmqpException)) {
            return null;
        }

        final Duration baseWaitTime = ((AmqpException) lastException).getErrorCondition() == ErrorCondition.SERVER_BUSY_ERROR
            ? SERVER_BUSY_WAIT_TIME
            : Duration.ZERO;

        final double nextRetryInterval = Math.pow(retryFactor, (double) retryCount);
        final long nextRetryIntervalSeconds = (long) nextRetryInterval;
        final long nextRetryIntervalNano = (long) ((nextRetryInterval - (double) nextRetryIntervalSeconds) * 1000000000);

        if (remainingTime.getSeconds() < Math.max(nextRetryInterval, TIMER_TOLERANCE.getSeconds())) {
            return null;
        }

        final Duration interval = Duration.ofSeconds(nextRetryIntervalSeconds, nextRetryIntervalNano);
        final Duration retryAfter = retryOptions.delay().plus(interval);

        return retryAfter.plus(baseWaitTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(retryFactor, retryOptions);
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
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() {
        final RetryOptions cloned = (RetryOptions) retryOptions.clone();
        return new ExponentialRetryPolicy(cloned);
    }

    private double computeRetryFactor() {
        final Duration maxBackoff = retryOptions.maxDelay();
        final Duration minBackoff = retryOptions.delay();
        final int maximumRetries = retryOptions.maxRetries();
        final long deltaBackoff = maxBackoff.minus(minBackoff).getSeconds();

        if (deltaBackoff <= 0 || maximumRetries <= 0) {
            return 0;
        }

        return Math.log(deltaBackoff) / Math.log(maximumRetries);
    }

}
