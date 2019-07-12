// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An abstract representation of a policy to govern retrying of messaging operations.
 */
public abstract class Retry implements Cloneable {
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
     * Creates a Retry policy that does not retry failed requests.
     *
     * @return A new Retry policy that does not retry failed requests.
     */
    public static Retry getNoRetry() {
        return new ExponentialRetry(Duration.ZERO, Duration.ZERO, 0);
    }

    /**
     * Creates a Retry policy that retries failed requests up to {@link #DEFAULT_MAX_RETRY_COUNT 10} times. As the
     * number of retry attempts increase, the period between retry attempts increases.
     *
     * @return A new instance with the default Retry values configured.
     */
    public static Retry getDefaultRetry() {
        return new ExponentialRetry(DEFAULT_RETRY_MIN_BACKOFF, DEFAULT_RETRY_MAX_BACKOFF, DEFAULT_MAX_RETRY_COUNT);
    }

    /**
     * Increments the number of retry attempts and returns the previous number of retry counts.
     *
     * @return The number of retry attempts before it was incremented.
     */
    public int incrementRetryCount() {
        return retryCount.getAndIncrement();
    }

    /**
     * Gets the current number of retry attempts for this instance.
     *
     * @return The current number of retry attempts.
     */
    public int getRetryCount() {
        return retryCount.get();
    }

    /**
     * Resets the number of retry attempts for this instance.
     */
    public void resetRetryInterval() {
        retryCount.set(0);
    }

    /**
     * Gets the maximum number of retry attempts that are allowed.
     *
     * @return The maximum number of retry attempts.
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt.
     *
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation
     *         is no longer eligible to be retried.
     */
    public Duration getNextRetryInterval(Duration baseWaitTime, Duration remainingTime) {
        if (retryCount.get() >= maxRetryCount) {
            return null;
        }

        return calculateNextRetryInterval(remainingTime, baseWaitTime, getRetryCount());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Allows a concrete retry policy implementation to offer a retry interval to be used in the calculations
     * performed by {@link Retry#getNextRetryInterval(Duration, Duration)}.
     *
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @param baseWaitTime The amount of time to base the suggested retry interval on. This should be used as
     *         the minimum interval returned under normal circumstances.
     * @param retryCount The number of retries that have already been attempted.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation
     *         is no longer eligible to be retried.
     */
    protected abstract Duration calculateNextRetryInterval(Duration remainingTime, Duration baseWaitTime, int retryCount);
}
