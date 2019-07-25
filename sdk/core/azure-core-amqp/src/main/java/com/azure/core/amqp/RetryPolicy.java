// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;

import java.time.Duration;

/**
 * An abstract representation of a policy to govern retrying of messaging operations.
 */
public abstract class RetryPolicy implements Cloneable {
    protected final RetryOptions retryOptions;

    /**
     * Creates an instance with the given retry options.
     *
     * @param retryOptions The options to set on this retry policy.
     */
    protected RetryPolicy(RetryOptions retryOptions) {
        this.retryOptions = (RetryOptions) retryOptions.clone();
    }

    /**
     * Check if the existing exception is a retriable exception.
     *
     * @param exception An exception that was observed for the operation to be retried.
     * @return true if the exception is a retriable exception, otherwise false.
     */
    protected static boolean isRetriableException(Exception exception) {
        return (exception instanceof AmqpException) && ((AmqpException) exception).isTransient();
    }

    /**
     * Calculates the amount of time to delay before the next retry attempt.
     *
     * @param lastException The last exception that was observed for the operation to be retried.
     * @param remainingTime The amount of time remaining for the cumulative timeout across retry attempts.
     * @param retryCount The number of times the request has been retried.
     * @return The amount of time to delay before retrying the associated operation; if {@code null}, then the operation
     *         is no longer eligible to be retried.
     */
    public abstract Duration calculateRetryDelay(Exception lastException, Duration remainingTime, int retryCount);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int hashCode() {
        return retryOptions.hashCode();
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
}
