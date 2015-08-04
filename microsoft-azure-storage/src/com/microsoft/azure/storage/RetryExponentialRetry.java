/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

import java.net.HttpURLConnection;
import java.util.Random;

/**
 * Represents a retry policy that performs a specified number of retries, using a randomized exponential backoff scheme
 * to determine the interval between retries.
 * 
 * This class extends the {@link com.microsoft.azure.storage.RetryPolicy} class and implements the
 * {@link com.microsoft.azure.storage.RetryPolicyFactory} interface.
 */
public final class RetryExponentialRetry extends RetryPolicy implements RetryPolicyFactory {

    /**
     * Holds the random number generator used to calculate randomized backoff intervals.
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
     * Determines whether the operation should be retried and specifies the interval until the next retry.
     * 
     * @param retryContext
     *            A {@link RetryContext} object that indicates the number of retries, last request's results, whether
     *            the next retry should happen in the primary or secondary location, and specifies the location mode.
     * @param operationContext
     *            An {@link OperationContext} object for tracking the current operation.
     * @return
     *         A {@link RetryInfo} object that indicates whether the next retry will happen in the primary or secondary
     *         location, and specifies the location mode. If <code>null</code>, the operation will not be retried.
     */
    @Override
    public RetryInfo evaluate(RetryContext retryContext, OperationContext operationContext) {

        boolean secondaryNotFound = this.evaluateLastAttemptAndSecondaryNotFound(retryContext);

        if (retryContext.getCurrentRetryCount() < this.maximumAttempts) {
            int statusCode = retryContext.getLastRequestResult().getStatusCode();
            if ((!secondaryNotFound && statusCode >= 400 && statusCode < 500)
                    || statusCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED
                    || statusCode == HttpURLConnection.HTTP_VERSION
                    || statusCode == Constants.HeaderConstants.HTTP_UNUSED_306) {
                return null;
            }

            // Calculate backoff Interval between 80% and 120% of the desired
            // backoff, multiply by 2^n -1 for exponential
            double incrementDelta = (Math.pow(2, retryContext.getCurrentRetryCount()) - 1);
            final int boundedRandDelta = (int) (this.deltaBackoffIntervalInMs * 0.8)
                    + this.randRef.nextInt((int) (this.deltaBackoffIntervalInMs * 1.2)
                            - (int) (this.deltaBackoffIntervalInMs * 0.8));
            incrementDelta *= boundedRandDelta;

            final long retryInterval = (int) Math.round(Math.min(this.resolvedMinBackoff + incrementDelta,
                    this.resolvedMaxBackoff));

            return this.evaluateRetryInfo(retryContext, secondaryNotFound, retryInterval);
        }

        return null;
    }
}
