/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage;

import java.util.Random;

/**
 * Represents a retry policy that performs a specified number of retries, using a randomized exponential backoff scheme
 * to determine the interval between retries.
 * 
 * This class extends the {@link com.microsoft.windowsazure.services.core.storage.RetryPolicy} class and implements the
 * {@link com.microsoft.windowsazure.services.core.storage.RetryPolicyFactory} interface.
 */
public final class RetryExponentialRetry extends RetryPolicy implements RetryPolicyFactory {

    /**
     * Holds the random number generator used to calculate randomized backoff interavals.
     */
    private final Random randRef = new Random();

    /**
     * Holds the actual maximum backoff interval to enforce.
     */
    private int resolvedMaxBackoff = RetryPolicy.DEFAULT_MAX_BACKOFF;

    /**
     * Holds the actual minimum backoff interval to enforce.
     */
    private int resolvedMinBackoff = RetryPolicy.DEFAULT_MIN_BACKOFF;

    /**
     * Creates an instance of the <code>RetryExponentialRetry</code> class.
     */
    public RetryExponentialRetry() {
        this(RetryPolicy.DEFAULT_CLIENT_BACKOFF, RetryPolicy.DEFAULT_CLIENT_RETRY_COUNT);
    }

    /**
     * Creates an instance of the <code>RetryExponentialRetry</code> class using the specified delta backoff and maximum
     * retry attempts.
     * 
     * @param deltaBackoff
     *            The backoff interval, in milliseconds, between retries.
     * @param maxAttempts
     *            The maximum number of retry attempts.
     */
    public RetryExponentialRetry(final int deltaBackoff, final int maxAttempts) {
        super(deltaBackoff, maxAttempts);
    }

    /**
     * Creates an instance of the <code>RetryExponentialRetry</code> class using the specified minimum, maximum, and
     * delta backoff amounts, and maximum number of retry attempts.
     * 
     * @param minBackoff
     *            The minimum backoff interval, in milliseconds, between retries.
     * @param deltaBackoff
     *            The backoff interval, in milliseconds, between retries.
     * @param maxBackOff
     *            The maximum backoff interval, in milliseconds, between retries.
     * @param maxAttempts
     *            The maximum retry attempts, in milliseconds, between retries.
     */
    public RetryExponentialRetry(final int minBackoff, final int deltaBackoff, final int maxBackOff,
            final int maxAttempts) {
        super(deltaBackoff, maxAttempts);
        this.resolvedMinBackoff = minBackoff;
        this.resolvedMaxBackoff = maxBackOff;
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
    @Override
    public RetryPolicy createInstance(final OperationContext opContext) {
        return new RetryExponentialRetry(this.resolvedMinBackoff, this.deltaBackoffIntervalInMs,
                this.resolvedMaxBackoff, this.maximumAttempts);
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
    public RetryResult shouldRetry(final int currentRetryCount, final int statusCode, final Exception lastException,
            final OperationContext opContext) {

        if (statusCode >= 400 && statusCode < 500) {
            return new RetryResult(0, false);
        }

        if (currentRetryCount < this.maximumAttempts) {

            // Calculate backoff Interval between 80% and 120% of the desired
            // backoff, multiply by 2^n -1 for
            // exponential
            int incrementDelta = (int) (Math.pow(2, currentRetryCount) - 1);
            final int boundedRandDelta = (int) (this.deltaBackoffIntervalInMs * 0.8)
                    + this.randRef.nextInt((int) (this.deltaBackoffIntervalInMs * 1.2)
                            - (int) (this.deltaBackoffIntervalInMs * 0.8));
            incrementDelta *= boundedRandDelta;

            // Enforce max / min backoffs
            return new RetryResult(Math.min(this.resolvedMinBackoff + incrementDelta, this.resolvedMaxBackoff), true);
        }
        else {
            return new RetryResult(-1, false);
        }
    }
}
