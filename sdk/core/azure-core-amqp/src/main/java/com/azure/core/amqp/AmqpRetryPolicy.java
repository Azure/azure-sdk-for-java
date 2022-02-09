// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import static com.azure.core.amqp.exception.AmqpErrorCondition.SERVER_BUSY_ERROR;
import static com.azure.core.amqp.implementation.ClientConstants.SERVER_BUSY_WAIT_TIME;

/**
 * An abstract representation of a policy to govern retrying of messaging operations.
 */
public abstract class AmqpRetryPolicy {
    static final long NANOS_PER_SECOND = 1000_000_000L;

    private static final double JITTER_FACTOR = 0.08;

    private final AmqpRetryOptions retryOptions;
    private final Duration baseJitter;

    /**
     * Creates an instance with the given retry options. If {@link AmqpRetryOptions#getMaxDelay()}, {@link
     * AmqpRetryOptions#getDelay()}, or {@link AmqpRetryOptions#getMaxRetries()} is equal to {@link Duration#ZERO} or
     * zero, requests failing with a retriable exception will not be retried.
     *
     * @param retryOptions The options to set on this retry policy.
     * @throws NullPointerException if {@code retryOptions} is {@code null}.
     */
    protected AmqpRetryPolicy(AmqpRetryOptions retryOptions) {
        Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");

        this.retryOptions = retryOptions;

        // 1 second = 1.0 * 10^9 nanoseconds.
        final double jitterInNanos = retryOptions.getDelay().getSeconds() * JITTER_FACTOR * NANOS_PER_SECOND;
        baseJitter = Duration.ofNanos((long) jitterInNanos);
    }

    /**
     * Gets the set of options used to configure this retry policy.
     *
     * @return The set of options used to configure this retry policy.
     */
    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * Gets the maximum number of retry attempts.
     *
     * @return The maximum number of retry attempts.
     */
    public int getMaxRetries() {
        return retryOptions.getMaxRetries();
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt.
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param retryCount The number of attempts that have been made, including the initial attempt before any retries.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation
     * is no longer eligible to be retried.
     */
    public Duration calculateRetryDelay(Throwable lastException, int retryCount) {
        if (retryOptions.getDelay() == Duration.ZERO
            || retryOptions.getMaxDelay() == Duration.ZERO
            || retryCount > retryOptions.getMaxRetries()) {
            return null;
        }

        final Duration baseDelay;
        if (lastException instanceof AmqpException && isRetriableException(lastException)) {
            baseDelay = ((AmqpException) lastException).getErrorCondition() == SERVER_BUSY_ERROR
                ? retryOptions.getDelay().plus(SERVER_BUSY_WAIT_TIME)
                : retryOptions.getDelay();
        } else if (lastException instanceof TimeoutException) {
            baseDelay = retryOptions.getDelay();
        } else {
            baseDelay = null;
        }

        if (baseDelay == null) {
            return null;
        }

        final Duration delay = calculateRetryDelay(retryCount, baseDelay, baseJitter, ThreadLocalRandom.current());

        // If delay is smaller or equal to the maximum delay, return the maximum delay.
        return delay.compareTo(retryOptions.getMaxDelay()) <= 0
            ? delay
            : retryOptions.getMaxDelay();
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt based on the {@code retryCount},
     * {@code baseDelay}, and {@code baseJitter}.
     *
     * @param retryCount The number of attempts that have been made, including the initial attempt before any retries.
     * @param baseDelay The base delay for a retry attempt.
     * @param baseJitter The base jitter delay.
     * @param random The random number generator. Can be utilised to calculate a random jitter value for the retry.
     * @return The amount of time to delay before retrying to associated operation; or {@code null} if the it cannot be
     * retried.
     */
    protected abstract Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter,
        ThreadLocalRandom random);

    @Override
    public int hashCode() {
        return Objects.hash(retryOptions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AmqpRetryPolicy)) {
            return false;
        }

        final AmqpRetryPolicy other = (AmqpRetryPolicy) obj;
        return retryOptions.equals(other.retryOptions);
    }

    /**
     * Check if the existing exception is a retriable exception.
     *
     * @param exception An exception that was observed for the operation to be retried.
     * @return true if the exception is a retriable exception, otherwise false.
     */
    private static boolean isRetriableException(Throwable exception) {
        return (exception instanceof AmqpException) && ((AmqpException) exception).isTransient();
    }
}
