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
package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents a retry policy that performs no retries.
 * 
 * This class extends the {@link com.microsoft.windowsazure.services.core.storage.RetryPolicy} class and implements the
 * {@link com.microsoft.windowsazure.services.core.storage.RetryPolicyFactory} interface.
 */
public final class RetryNoRetry extends RetryPolicy implements RetryPolicyFactory {

    /**
     * Holds the static instance of the no retry policy.
     */
    private static RetryNoRetry instance = new RetryNoRetry();

    /**
     * Returns the static instance of a no retry policy.
     * 
     * @return A <code>RetryNoRetry</code> object that represents a no retry policy.
     */
    public static RetryNoRetry getInstance() {
        return instance;
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
        return getInstance();
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
     * 
     */
    @Override
    public RetryResult shouldRetry(final int currentRetryCount, final int statusCode, final Exception lastException,
            final OperationContext opContext) {
        return new RetryResult(0, false);
    }
}
