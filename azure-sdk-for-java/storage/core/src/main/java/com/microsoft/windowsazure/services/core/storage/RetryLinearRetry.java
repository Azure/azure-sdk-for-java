/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.storage;

import java.net.HttpURLConnection;

/**
 * Represents a retry policy that performs a specified number of retries, using a specified fixed time interval between
 * retries.
 * 
 * This class extends the {@link RetryPolicy} class and implements the {@link RetryPolicyFactory} interface.
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
    @Override
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
    public RetryResult shouldRetry(final int currentRetryCount, final int statusCode, final Exception lastException,
            final OperationContext opContext) {
        if (statusCode >= 400 && statusCode < 500 || statusCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED
                || statusCode == HttpURLConnection.HTTP_VERSION) {
            return new RetryResult(0, false);
        }

        final int backoff = Math.max(Math.min(this.deltaBackoffIntervalInMs, RetryPolicy.DEFAULT_MAX_BACKOFF),
                RetryPolicy.DEFAULT_MIN_BACKOFF);

        return new RetryResult(backoff, currentRetryCount < this.maximumAttempts);
    }
}
