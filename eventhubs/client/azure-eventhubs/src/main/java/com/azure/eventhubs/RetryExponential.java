// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ClientConstants;

import java.time.Duration;

public final class RetryExponential extends Retry {
    private final Duration minBackoff;
    private final Duration maxBackoff;
    private final int maxRetryCount;
    private final double retryFactor;

    /**
     * @param minBackoff The minimum time period permissible for backing off between retries.
     * @param maxBackoff The maximum time period permissible for backing off between retries.
     * @param maxRetryCount The maximum number of retries allowed.
     */
    public RetryExponential(Duration minBackoff, Duration maxBackoff, int maxRetryCount) {
        this.minBackoff = minBackoff;
        this.maxBackoff = maxBackoff;
        this.maxRetryCount = maxRetryCount;
        this.retryFactor = computeRetryFactor();
    }

    @Override
    protected Duration onGetNextRetryInterval(final Exception lastException,
                                              final Duration remainingTime,
                                              final int baseWaitSeconds,
                                              final int retryCount) {
        if ((!Retry.isRetryableException(lastException)) || retryCount >= maxRetryCount) {
            return null;
        }
        double nextRetryInterval = Math.pow(retryFactor, (double) retryCount);

        long nextRetryIntervalSeconds = (long) nextRetryInterval;
        long nextRetryIntervalNano = (long) ((nextRetryInterval - (double) nextRetryIntervalSeconds) * 1000000000);
        if (remainingTime.getSeconds() < Math.max(nextRetryInterval, ClientConstants.TIMER_TOLERANCE.getSeconds())) {
            return null;
        }

        Duration retryAfter = this.minBackoff.plus(Duration.ofSeconds(nextRetryIntervalSeconds, nextRetryIntervalNano));
        return retryAfter.plus(Duration.ofSeconds(baseWaitSeconds));
    }

    private double computeRetryFactor() {
        long deltaBackoff = this.maxBackoff.minus(this.minBackoff).getSeconds();
        if (deltaBackoff <= 0 || this.maxRetryCount <= 0) {
            return 0;
        }
        return Math.log(deltaBackoff) / Math.log(this.maxRetryCount);
    }
}
