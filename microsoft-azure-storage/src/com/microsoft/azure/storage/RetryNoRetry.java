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

/**
 * Represents a retry policy that performs no retries.
 * 
 * This class extends the {@link com.microsoft.azure.storage.RetryPolicy} class and implements the
 * {@link com.microsoft.azure.storage.RetryPolicyFactory} interface.
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
        return null;
    }
}
