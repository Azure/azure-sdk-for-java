// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An abstract representation of a policy to govern retrying of messaging operations.
 */
public abstract class RetryPolicy implements Cloneable {
    protected final RetryOptions retryOptions;

    protected RetryPolicy(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
     * @return The amount of time to delay before retrying the associated operation; if {@code null},
     * then the operation is no longer eligible to be retried.
     */
    public abstract Duration calculateRetryDelay(Exception lastException, Duration remainingTime, int retryCount);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
