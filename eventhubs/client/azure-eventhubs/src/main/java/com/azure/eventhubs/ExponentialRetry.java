// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;

import java.time.Duration;

/**
 * A policy to govern retrying of messaging operations in which the delay between retries
 * will grow in an exponential manner, allowing more time to recover as the number of retries increases.
 */
public final class ExponentialRetry extends Retry {

    public static final Duration TIMER_TOLERANCE = Duration.ofSeconds(1);

    private final Duration minBackoff;
    private final Duration maxBackoff;
    private final double retryFactor;

    /**
     * Creates a new instance with a minimum and maximum retry period in addition to maximum number of retry attempts.
     *
     * @param minBackoff The minimum time period permissible for backing off between retries.
     * @param maxBackoff The maximum time period permissible for backing off between retries.
     * @param maxRetryCount The maximum number of retries allowed.
     */
    public ExponentialRetry(Duration minBackoff, Duration maxBackoff, int maxRetryCount) {
        super(maxRetryCount);
        this.minBackoff = minBackoff;
        this.maxBackoff = maxBackoff;

        this.retryFactor = computeRetryFactor();
    }

    @Override
    protected Duration calculateNextRetryInterval(final Exception lastException,
                                                  final Duration remainingTime,
                                                  final int baseWaitSeconds,
                                                  final int retryCount) {
        final double nextRetryInterval = Math.pow(retryFactor, (double) retryCount);
        final long nextRetryIntervalSeconds = (long) nextRetryInterval;
        final long nextRetryIntervalNano = (long) ((nextRetryInterval - (double) nextRetryIntervalSeconds) * 1000000000);

        if (remainingTime.getSeconds() < Math.max(nextRetryInterval, TIMER_TOLERANCE.getSeconds())) {
            return null;
        }

        final Duration retryAfter = minBackoff.plus(Duration.ofSeconds(nextRetryIntervalSeconds, nextRetryIntervalNano));
        return retryAfter.plus(Duration.ofSeconds(baseWaitSeconds));
    }

    private double computeRetryFactor() {
        final long deltaBackoff = maxBackoff.minus(minBackoff).getSeconds();
        if (deltaBackoff <= 0 || super.maxRetryCount() <= 0) {
            return 0;
        }
        return Math.log(deltaBackoff) / Math.log(super.maxRetryCount());
    }
}
