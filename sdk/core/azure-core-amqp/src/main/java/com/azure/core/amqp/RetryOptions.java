// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.time.Duration;

/**
 * A set of options that can be specified to influence how retry attempts are made.
 */
public class RetryOptions implements Cloneable {
    private int maximumNumberOfRetries;
    private Duration delay;
    private Duration maximumDelay;
    private Duration tryTimeout;
    private RetryMode retryMode;

    /**
     * Creates an instance with the default retry options set.
     */
    public RetryOptions() {
        maximumNumberOfRetries = 3;
        delay = Duration.ofMillis(800);
        maximumDelay = Duration.ofMinutes(1);
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
    public RetryOptions maximumNumberOfRetries(int numberOfRetries) {
        this.maximumNumberOfRetries = numberOfRetries;
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
     * @param maximumDelay the maximum permissible delay between retry attempts.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions maximumDelay(Duration maximumDelay) {
        this.maximumDelay = maximumDelay;
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
    public int maximumNumberOfRetries() {
        return maximumNumberOfRetries;
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
    public Duration maximumDelay() {
        return maximumDelay;
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
    public Object clone() {
        RetryOptions clone;
        try {
            clone = (RetryOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new RetryOptions();
        }

        clone.delay(delay);
        clone.maximumDelay(maximumDelay);
        clone.maximumNumberOfRetries(maximumNumberOfRetries);
        clone.tryTimeout(tryTimeout);
        clone.retryMode(retryMode);

        return clone;
    }
}
