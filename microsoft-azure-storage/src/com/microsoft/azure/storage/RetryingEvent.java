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
 * Represents an event that is fired when a request is retried.
 */
public final class RetryingEvent extends BaseEvent {

    /**
     * Represents the context for a retry of a request made against the storage services. Includes current retry count,
     * location mode, and next location.
     */
    private final RetryContext retryContext;

    /**
     * Creates an instance of the <code>BaseEvent</code> class which is fired when a request is retried.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param connectionObject
     *            Represents a connection object. Currently only <code>java.net.HttpURLConnection</code> is supported as
     *            a connection object.
     * @param requestResult
     *            A {@link RequestResult} object that represents the current request result.
     * @param retryContext
     *            A {@link RetryContext} object which contains the number of retries done for this request (including 
     *            the pending retry) and other retry information.
     */
    public RetryingEvent(OperationContext opContext, Object connectionObject, RequestResult requestResult,
            RetryContext retryContext) {
        super(opContext, connectionObject, requestResult);
        this.retryContext = retryContext;
    }

    /**
     * Gets the context for a retry of a request made against the storage services. Includes current retry count,
     * location mode, and next location.
     * 
     * @return the retryCount
     */
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

}
