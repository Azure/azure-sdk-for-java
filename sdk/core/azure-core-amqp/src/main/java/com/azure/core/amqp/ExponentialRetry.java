// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.time.Duration;
import java.util.Objects;

/**
 * A policy to govern retrying of messaging operations in which the delay between retries will grow in an exponential
 * manner, allowing more time to recover as the number of retries increases.
 */
public final class ExponentialRetry extends Retry {
    private static final Duration TIMER_TOLERANCE = Duration.ofSeconds(1);

    private final Duration minBackoff;
    private final Duration maxBackoff;
    private final double retryFactor;

    /**
     * Creates a new instance with a minimum and maximum retry period in addition to maximum number of retry attempts.
     *
     * @param minBackoff The minimum time period permissible for backing off between retries.
     * @param maxBackoff The maximum time period permissible for backing off between retries.
     * @param maxRetryCount The maximum number of retries allowed.
     * @throws NullPointerException if {@code minBackoff} or {@code maxBackoff} is {@code null}.
     */
    public ExponentialRetry(Duration minBackoff, Duration maxBackoff, int maxRetryCount) {
        super(maxRetryCount);
        Objects.requireNonNull(minBackoff);
        Objects.requireNonNull(maxBackoff);

        this.minBackoff = minBackoff;
        this.maxBackoff = maxBackoff;

        this.retryFactor = computeRetryFactor();
    }

    /**
     * {@inheritDoc}
     */
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
        if (deltaBackoff <= 0 || super.getMaxRetryCount() <= 0) {
            return 0;
        }
        return Math.log(deltaBackoff) / Math.log(super.getMaxRetryCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxBackoff, minBackoff, getMaxRetryCount(), getRetryCount());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ExponentialRetry)) {
            return false;
        }

        ExponentialRetry other = (ExponentialRetry) obj;

        return this.maxBackoff.equals(other.maxBackoff)
            && this.minBackoff.equals(other.minBackoff)
            && this.getMaxRetryCount() == other.getMaxRetryCount()
            && this.getRetryCount() == other.getRetryCount();
    }

    /**
     * Creates a clone of this instance.
     *
     * The {@code minBackoff}, {@code maxBackoff}, and {@code maxRetryCount} are not cloned, but these objects are
     * immutable and not subject to change.
     *
     * @return A clone of the {@link ExponentialRetry} instance.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() {
        return new ExponentialRetry(minBackoff, maxBackoff, getMaxRetryCount());
    }
}
