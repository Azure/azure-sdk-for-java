// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import static com.azure.core.amqp.exception.ErrorCondition.SERVER_BUSY_ERROR;

/**
 * An abstract representation of a policy to govern retrying of messaging operations.
 */
public abstract class RetryPolicy implements Cloneable {
    static final long NANOS_PER_SECOND = 1000_000_000L;

    private static final double JITTER_FACTOR = 0.08;
    // Base sleep wait time.
    private static final Duration SERVER_BUSY_WAIT_TIME = Duration.ofSeconds(4);

    private final RetryOptions retryOptions;
    private final Duration baseJitter;

    /**
     * Creates an instance with the given retry options. If {@link RetryOptions#maxDelay()}, {@link
     * RetryOptions#delay()}, or {@link RetryOptions#maxRetries()} is equal to {@link Duration#ZERO} or zero, requests
     * failing with a retriable exception will not be retried.
     *
     * @param retryOptions The options to set on this retry policy.
     * @throws NullPointerException if {@code retryOptions} is {@code null}.
     */
    protected RetryPolicy(RetryOptions retryOptions) {
        Objects.requireNonNull(retryOptions);

        this.retryOptions = retryOptions;

        // 1 second = 1.0 * 10^9 nanoseconds.
        final Double jitterInNanos = retryOptions.delay().getSeconds() * JITTER_FACTOR * NANOS_PER_SECOND;
        baseJitter = Duration.ofNanos(jitterInNanos.longValue());
    }

    /**
     * Gets the set of options used to configure this retry policy.
     *
     * @return The set of options used to configure this retry policy.
     */
    protected RetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * Gets the maximum number of retry attempts.
     *
     * @return The maximum number of retry attempts.
     */
    public int getMaxRetries() {
        return retryOptions.maxRetries();
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt.
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param retryCount The number of attempts that have been made, including the initial attempt before any
     *         retries.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation
     *         is no longer eligible to be retried.
     */
    public Duration calculateRetryDelay(Exception lastException, int retryCount) {
        if (retryOptions.delay() == Duration.ZERO
            || retryOptions.maxDelay() == Duration.ZERO
            || retryCount > retryOptions.maxRetries()) {
            return null;
        }

        final Duration baseDelay;
        if (lastException instanceof AmqpException && isRetriableException(lastException)) {
            baseDelay = ((AmqpException) lastException).getErrorCondition() == SERVER_BUSY_ERROR
                ? retryOptions.delay().plus(SERVER_BUSY_WAIT_TIME)
                : retryOptions.delay();
        } else if (lastException instanceof TimeoutException) {
            baseDelay = retryOptions.delay();
        } else {
            baseDelay = null;
        }

        if (baseDelay == null) {
            return null;
        }

        final Duration delay = calculateRetryDelay(retryCount, baseDelay, baseJitter, ThreadLocalRandom.current());

        // If delay is smaller or equal to the maximum delay, return the maximum delay.
        return delay.compareTo(retryOptions.maxDelay()) <= 0
            ? delay
            : retryOptions.maxDelay();
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt based on the {@code retryCound}, {@code
     * baseDelay}, and {@code baseJitter}.
     *
     * @param retryCount The number of attempts that have been made, including the initial attempt before any
     *         retries.
     * @param baseDelay The base delay for a retry attempt.
     * @param baseJitter The base jitter delay.
     * @param random The random number generator. Can be utilised to calculate a random jitter value for the
     *         retry.
     * @return The amount of time to delay before retrying to associated operation; or {@code null} if the it cannot be
     *         retried.
     */
    protected abstract Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter,
                                                    ThreadLocalRandom random);

    /**
     * Creates a clone of the retry policy.
     *
     * @return A new clone of the retry policy.
     */
    @Override
    public RetryPolicy clone() throws CloneNotSupportedException {
        return (RetryPolicy) super.clone();
    }

    @Override
    public int hashCode() {
        return Objects.hash(retryOptions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RetryPolicy)) {
            return false;
        }

        final RetryPolicy other = (RetryPolicy) obj;
        return retryOptions.equals(other.retryOptions);
    }

    /**
     * Check if the existing exception is a retriable exception.
     *
     * @param exception An exception that was observed for the operation to be retried.
     * @return true if the exception is a retriable exception, otherwise false.
     */
    private static boolean isRetriableException(Exception exception) {
        return (exception instanceof AmqpException) && ((AmqpException) exception).isTransient();
    }
}
