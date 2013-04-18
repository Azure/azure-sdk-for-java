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
/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

/**
 * Abstract class that represents a retry policy.
 */
public abstract class RetryPolicy {

    /**
     * Represents the default client backoff interval, in milliseconds.
     */
    public static final int DEFAULT_CLIENT_BACKOFF = 1000 * 30;

    /**
     * Represents the default client retry count.
     */
    public static final int DEFAULT_CLIENT_RETRY_COUNT = 3;

    /**
     * Represents the default maximum backoff interval, in milliseconds.
     */
    public static final int DEFAULT_MAX_BACKOFF = 1000 * 90;

    /**
     * Represents the default minimum backoff interval, in milliseconds.
     */
    public static final int DEFAULT_MIN_BACKOFF = 1000 * 3;

    /**
     * Represents the realized backoff interval, in milliseconds.
     */
    protected int deltaBackoffIntervalInMs;

    /**
     * Represents the maximum retries that the retry policy should attempt.
     */
    protected int maximumAttempts;

    /**
     * Creates an instance of the <code>RetryPolicy</code> class.
     */
    public RetryPolicy() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>RetryPolicy</code> class using the specified delta backoff and maximum retry
     * attempts.
     * 
     * @param deltaBackoff
     *            The backoff interval, in milliseconds, between retries.
     * @param maxAttempts
     *            The maximum number of retry attempts.
     */
    public RetryPolicy(final int deltaBackoff, final int maxAttempts) {
        this.deltaBackoffIntervalInMs = deltaBackoff;
        this.maximumAttempts = maxAttempts;
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
     * @return A {@link RetryResult} object that indicates whether the operation should be retried and how long to
     *         backoff.
     */
    public abstract RetryResult shouldRetry(int currentRetryCount, int statusCode, Exception lastException,
            OperationContext opContext);
}
