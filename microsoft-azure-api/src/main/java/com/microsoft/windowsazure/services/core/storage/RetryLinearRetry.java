package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents a retry policy that performs a specified number of retries, using a specified fixed time interval between
 * retries.
 * 
 * This class extends the {@link RetryPolicy} class and implements the {@link RetryPolicyFactory} interface.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class RetryLinearRetry extends RetryPolicy implements RetryPolicyFactory {

    /**
     * Creates an instance of the <code>RetryLinearRetry</code> class.
     */
    public RetryLinearRetry() {
        this(RetryPolicy.DEFAULT_CLIENT_BACKOFF, RetryPolicy.DEFAULT_CLIENT_RETRY_COUNT);
    }

    /**
     * Creates an instance of the <code>RetryLinearRetry</code> class using the specified delta backoff and maximum
     * retry attempts.
     * 
     * @param deltaBackoff
     *            The backoff interval, in milliseconds, between retries.
     * @param maxAttempts
     *            The maximum number of retry attempts.
     */
    public RetryLinearRetry(final int deltaBackoff, final int maxAttempts) {
        super(deltaBackoff, maxAttempts);
    }

    /**
     * Generates a new retry policy for the current request attempt.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link RetryPolicy} object that represents the retry policy for the current request attempt.
     */
    public RetryPolicy createInstance(final OperationContext opContext) {
        return new RetryLinearRetry(this.deltaBackoffIntervalInMs, this.maximumAttempts);
    }

    /**
     * Determines if the operation should be retried and how long to wait until the next retry.
     * 
     * @param currentRetryCount
     *            The number of retries for the given operation. A value of zero signifies this is the first error
     *            encountered.
     * @param statusCode
     *            The status code for the last operation.
     * @param lastException
     *            A <code>Exception</code> object that represents the last exception encountered.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link RetryResult} object that represents the retry result, indicating whether the operation should be
     *         retried and how long to backoff.
     */
    @Override
    public RetryResult shouldRetry(
            final int currentRetryCount, final int statusCode, final Exception lastException,
            final OperationContext opContext) {
        if (statusCode >= 400 && statusCode < 500) {
            return new RetryResult(0, false);
        }

        final int backoff =
                Math.max(Math.min(this.deltaBackoffIntervalInMs, RetryPolicy.DEFAULT_MAX_BACKOFF),
                        RetryPolicy.DEFAULT_MIN_BACKOFF);

        return new RetryResult(backoff, currentRetryCount < this.maximumAttempts);
    }
}
