// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.eventhubs.implementation.ClientConstants;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Retry {

    public static final Retry NO_RETRY = new RetryExponential(Duration.ofSeconds(0), Duration.ofSeconds(0), 0);

    private AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * Check if the existing exception is a retryable exception.
     *
     * @param exception A exception that was observed for the operation to be retried.
     * @return true if the exception is a retryable exception, otherwise false.
     * @throws IllegalArgumentException when the exception is null.
     */
    public static boolean isRetryableException(Exception exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception cannot be null");
        }

        if (exception instanceof AmqpException) {
            return ((AmqpException) exception).isTransient();
        }
        return false;
    }

    /**
     * Get 'NO_RETRY' of current.
     *
     * @return Retry 'NO_RETRY'.
     */
    public static Retry getNoRetry() {
        return Retry.NO_RETRY;
    }

    /**
     * Get default configured Retry.
     *
     * @return Retry which has all default property set up.
     */
    public static Retry getDefault() {
        return new RetryExponential(
            ClientConstants.DEFAULT_RETRY_MIN_BACKOFF,
            ClientConstants.DEFAULT_RETRY_MAX_BACKOFF,
            ClientConstants.DEFAULT_MAX_RETRY_COUNT);
    }

    /**
     * Increase one count to current count value.
     *
     * @return current AtomicInteger value.
     */
    public int incrementRetryCount() {
        return retryCount.incrementAndGet();
    }

    /**
     * Get the current retried count.
     *
     * @return current AtomicInteger value.
     */
    public int getRetryCount() {
        return retryCount.get();
    }

    /**
     * reset AtomicInteger to value zero.
     */
    public void resetRetryInterval() {
        retryCount.set(0);
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation is no longer eligible to be retried.
     */
    public Duration getNextRetryInterval(Exception lastException, Duration remainingTime) {
        int baseWaitTime = 0;

        if (lastException == null || !(lastException instanceof AmqpException)) {
            return this.onGetNextRetryInterval(lastException, remainingTime, baseWaitTime, this.getRetryCount());
        }

        if (((AmqpException) lastException).getErrorCondition() == ErrorCondition.SERVER_BUSY_ERROR) {
            baseWaitTime += ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS;
        }
        return this.onGetNextRetryInterval(lastException, remainingTime, baseWaitTime, this.getRetryCount());
    }

    /**
     * Allows a concrete retry policy implementation to offer a base retry interval to be used in
     * the calculations performed by 'Retry.GetNextRetryInterval'.
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @param baseWaitSeconds The number of seconds to base the suggested retry interval on;
     *   this should be used as the minimum interval returned under normal circumstances.
     * @param retryCount The number of retries that have already been attempted.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation is no longer eligible to be retried.
     */
    protected abstract Duration onGetNextRetryInterval(Exception lastException,
                                                       Duration remainingTime,
                                                       int baseWaitSeconds,
                                                       int retryCount);
}
