// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.eventhubs.implementation.ClientConstants;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An abstract representation of a policy to govern retrying of messaging operations.
 */
public abstract class Retry {
    /**
     * Default for the minimum time between retry attempts.
     */
    public static final Duration DEFAULT_RETRY_MIN_BACKOFF = Duration.ofSeconds(0);
    /**
     * Default for the maximum time between retry attempts.
     */
    public static final Duration DEFAULT_RETRY_MAX_BACKOFF = Duration.ofSeconds(30);
    /**
     * Default for the maximum number of retry attempts.
     */
    public static final int DEFAULT_MAX_RETRY_COUNT = 10;

    private final AtomicInteger retryCount = new AtomicInteger();
    private final int maxRetryCount;

    /**
     * Creates a new instance of Retry with the maximum retry count of {@code maxRetryCount}
     *
     * @param maxRetryCount The maximum number of retries allowed.
     */
    public Retry(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * Check if the existing exception is a retriable exception.
     *
     * @param exception An exception that was observed for the operation to be retried.
     * @return true if the exception is a retriable exception, otherwise false.
     */
    public static boolean isRetriableException(Exception exception) {
        return (exception instanceof AmqpException) && ((AmqpException) exception).isTransient();
    }

    /**
     * Get default configured Retry.
     *
     * @return Retry which has all default property set up.
     */
    public static Retry getNoRetry() {
        return new ExponentialRetry(Duration.ZERO, Duration.ZERO, 0);
    }

    /**
     * Get default configured Retry.
     *
     * @return Retry which has all default property set up.
     */
    public static Retry getDefaultRetry() {
        return new ExponentialRetry(DEFAULT_RETRY_MIN_BACKOFF, DEFAULT_RETRY_MAX_BACKOFF, DEFAULT_MAX_RETRY_COUNT);
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
     * Reset AtomicInteger to value zero.
     */
    public void resetRetryInterval() {
        retryCount.set(0);
    }

    /**
     * Get the maximum allowed retry count.
     *
     * @return maximum allowed retry count value.
     */
    public int maxRetryCount() {
        return maxRetryCount;
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @return The amount of time to delay before retrying the associated operation; if {@code null},
     * then the operation is no longer eligible to be retried.
     */
    public Duration getNextRetryInterval(Exception lastException, Duration remainingTime) {
        int baseWaitTime = 0;

        if (!isRetriableException(lastException) || retryCount.get() >= maxRetryCount) {
            return null;
        }

        if (!(lastException instanceof AmqpException)) {
            return null;
        }

        if (((AmqpException) lastException).getErrorCondition() == ErrorCondition.SERVER_BUSY_ERROR) {
            baseWaitTime += ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS;
        }

        return this.calculateNextRetryInterval(lastException, remainingTime, baseWaitTime, this.getRetryCount());
    }

    /**
     * Allows a concrete retry policy implementation to offer a base retry interval to be used in
     * the calculations performed by 'Retry.GetNextRetryInterval'.
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @param baseWaitSeconds The number of seconds to base the suggested retry interval on;
     * this should be used as the minimum interval returned under normal circumstances.
     * @param retryCount The number of retries that have already been attempted.
     * @return The amount of time to delay before retrying the associated operation; if {@code null},
     * then the operation is no longer eligible to be retried.
     */
    protected abstract Duration calculateNextRetryInterval(Exception lastException, Duration remainingTime,
                                                           int baseWaitSeconds, int retryCount);
}
