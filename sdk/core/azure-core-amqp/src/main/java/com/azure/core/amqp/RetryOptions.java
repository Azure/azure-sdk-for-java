// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.time.Duration;
import java.util.Objects;

/**
 * A set of options that can be specified to influence how retry attempts are made.
 */
public class RetryOptions implements Cloneable {
    private int maxRetries;
    private Duration delay;
    private Duration maxDelay;
    private Duration tryTimeout;
    private RetryMode retryMode;

    /**
     * Creates an instance with the default retry options set.
     */
    public RetryOptions() {
        maxRetries = 3;
        delay = Duration.ofMillis(800);
        maxDelay = Duration.ofMinutes(1);
        tryTimeout = Duration.ofMinutes(1);
        retryMode = RetryMode.EXPONENTIAL;
    }

    /**
     * Sets the approach to use for calculating retry delays.
     *
     * @param retryMode The retry approach to use for calculating delays.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions retryMode(RetryMode retryMode) {
        this.retryMode = retryMode;
        return this;
    }

    /**
     * Sets the maximum number of retry attempts before considering the associated operation to have failed.
     *
     * @param numberOfRetries The maximum number of retry attempts.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions maxRetries(int numberOfRetries) {
        this.maxRetries = numberOfRetries;
        return this;
    }

    /**
     * Gets the delay between retry attempts for a fixed approach or the delay on which to base calculations for a
     * backoff-approach.
     *
     * @param delay The delay between retry attempts.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions delay(Duration delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Sets the maximum permissible delay between retry attempts.
     *
     * @param maximumDelay The maximum permissible delay between retry attempts.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions maxDelay(Duration maximumDelay) {
        this.maxDelay = maximumDelay;
        return this;
    }

    /**
     * Sets the maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.
     *
     * @param tryTimeout The maximum duration to wait for completion.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions tryTimeout(Duration tryTimeout) {
        this.tryTimeout = tryTimeout;
        return this;
    }

    /**
     * Gets the approach to use for calculating retry delays.
     *
     * @return The approach to use for calculating retry delays.
     */
    public RetryMode retryMode() {
        return retryMode;
    }

    /**
     * The maximum number of retry attempts before considering the associated operation to have failed.
     *
     * @return The maximum number of retry attempts before considering the associated operation to have failed.
     */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Gets the delay between retry attempts for a fixed approach or the delay on which to base calculations for a
     * backoff-approach.
     *
     * @return The delay between retry attempts.
     */
    public Duration delay() {
        return delay;
    }

    /**
     * Gets the maximum permissible delay between retry attempts.
     *
     * @return The maximum permissible delay between retry attempts.
     */
    public Duration maxDelay() {
        return maxDelay;
    }

    /**
     * Gets the maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.
     *
     * @return The maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.
     */
    public Duration tryTimeout() {
        return tryTimeout;
    }

    /**
     * Creates a new copy of the current instance, cloning its attributes into a new instance.
     *
     * @return A new copy of {@link RetryOptions}.
     */
    @Override
    public RetryOptions clone() {

        RetryOptions clone;
        try {
            clone = (RetryOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new RetryOptions();
        }

        return clone.delay(delay)
            .maxDelay(maxDelay)
            .maxRetries(maxRetries)
            .tryTimeout(tryTimeout)
            .retryMode(retryMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RetryOptions)) {
            return false;
        }

        final RetryOptions other = (RetryOptions) obj;

        return this.maxRetries() == other.maxRetries()
            && this.retryMode() == other.retryMode()
            && Objects.equals(this.maxDelay(), other.maxDelay())
            && Objects.equals(this.delay(), other.delay())
            && Objects.equals(this.tryTimeout(), other.tryTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(maxRetries, retryMode, maxDelay, delay, tryTimeout);
    }
}
