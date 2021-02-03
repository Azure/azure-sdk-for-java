// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * A set of options that can be specified to influence how retry attempts are made.
 */
@Fluent
public class AmqpRetryOptions {
    private final ClientLogger logger = new ClientLogger(AmqpRetryOptions.class);

    private int maxRetries;
    private Duration delay;
    private Duration maxDelay;
    private Duration tryTimeout;
    private AmqpRetryMode retryMode;

    /**
     * Creates an instance with the default retry options set.
     */
    public AmqpRetryOptions() {
        this.maxRetries = 3;
        this.delay = Duration.ofMillis(800);
        this.maxDelay = Duration.ofMinutes(1);
        this.tryTimeout = Duration.ofMinutes(1);
        this.retryMode = AmqpRetryMode.EXPONENTIAL;
    }

    /**
     * Creates an instance configured with {@code retryOptions}. This is not thread-safe.
     *
     * @param retryOptions Retry options to configure new instance with.
     * @throws NullPointerException if {@code retryOptions} is null.
     */
    public AmqpRetryOptions(AmqpRetryOptions retryOptions) {
        Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");

        this.maxDelay = retryOptions.getMaxDelay();
        this.delay = retryOptions.getDelay();
        this.maxRetries = retryOptions.getMaxRetries();
        this.retryMode = retryOptions.getMode();
        this.tryTimeout = retryOptions.getTryTimeout();
    }

    /**
     * Sets the approach to use for calculating retry delays.
     *
     * @param retryMode The retry approach to use for calculating delays.
     * @return The updated {@link AmqpRetryOptions} object.
     */
    public AmqpRetryOptions setMode(AmqpRetryMode retryMode) {
        this.retryMode = retryMode;
        return this;
    }

    /**
     * Sets the maximum number of retry attempts before considering the associated operation to have failed.
     *
     * @param numberOfRetries The maximum number of retry attempts.
     * @return The updated {@link AmqpRetryOptions} object.
     * @throws IllegalArgumentException When {@code numberOfRetries} is negative.
     */
    public AmqpRetryOptions setMaxRetries(int numberOfRetries) {
        if (numberOfRetries < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'numberOfRetries' cannot be negative."));
        }

        this.maxRetries = numberOfRetries;
        return this;
    }

    /**
     * Gets the delay between retry attempts for a fixed approach or the delay on which to base calculations for a
     * backoff-approach.
     *
     * @param delay The delay between retry attempts.
     * @return The updated {@link AmqpRetryOptions} object.
     * @throws NullPointerException When {@code delay} is null.
     * @throws IllegalArgumentException When {@code delay} is negative or zero.
     */
    public AmqpRetryOptions setDelay(Duration delay) {
        Objects.requireNonNull(delay, "'delay' cannot be null.");
        if (delay.isNegative() || delay.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'delay' must be positive."));
        }

        this.delay = delay;
        return this;
    }

    /**
     * Sets the maximum permissible delay between retry attempts.
     *
     * @param maximumDelay The maximum permissible delay between retry attempts.
     * @return The updated {@link AmqpRetryOptions} object.
     *
     * @throws NullPointerException When {@code maximumDelay} is null.
     * @throws IllegalArgumentException When {@code maximumDelay} is negative or zero.
     */
    public AmqpRetryOptions setMaxDelay(Duration maximumDelay) {
        Objects.requireNonNull(maximumDelay, "'maximumDelay' cannot be null.");
        if (maximumDelay.isNegative() || maximumDelay.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'maximumDelay' must be positive."));
        }

        this.maxDelay = maximumDelay;
        return this;
    }

    /**
     * Sets the maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.
     *
     * @param tryTimeout The maximum duration to wait for completion.
     * @return The updated {@link AmqpRetryOptions} object.
     *
     * @throws NullPointerException When {@code tryTimeout} is null.
     * @throws IllegalArgumentException When {@code tryTimeout} is negative or zero.
     */
    public AmqpRetryOptions setTryTimeout(Duration tryTimeout) {
        Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null");
        if (tryTimeout.isNegative() || tryTimeout.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'tryTimeout' must be positive."));
        }

        this.tryTimeout = tryTimeout;
        return this;
    }

    /**
     * Gets the approach to use for calculating retry delays.
     *
     * @return The approach to use for calculating retry delays.
     */
    public AmqpRetryMode getMode() {
        return retryMode;
    }

    /**
     * The maximum number of retry attempts before considering the associated operation to have failed.
     *
     * @return The maximum number of retry attempts before considering the associated operation to have failed.
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Gets the delay between retry attempts for a fixed approach or the delay on which to base calculations for a
     * backoff-approach.
     *
     * @return The delay between retry attempts.
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Gets the maximum permissible delay between retry attempts.
     *
     * @return The maximum permissible delay between retry attempts.
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Gets the maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.
     *
     * @return The maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.
     */
    public Duration getTryTimeout() {
        return tryTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AmqpRetryOptions)) {
            return false;
        }

        final AmqpRetryOptions other = (AmqpRetryOptions) obj;

        return this.getMaxRetries() == other.getMaxRetries()
            && this.getMode() == other.getMode()
            && Objects.equals(this.getMaxDelay(), other.getMaxDelay())
            && Objects.equals(this.getDelay(), other.getDelay())
            && Objects.equals(this.getTryTimeout(), other.getTryTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(maxRetries, retryMode, maxDelay, delay, tryTimeout);
    }
}
