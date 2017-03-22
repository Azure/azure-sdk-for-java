/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.documentdb.rx.internal;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.internal.RetryPolicyBridgeInternal;
import com.microsoft.azure.documentdb.internal.EndpointManager;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.documentdb.internal.RetryPolicy;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;

import rx.Observable;

/**
 * Provides a Retry handler for executing the code block and retry if needed.
 */
class ExecuteDocumentClientRequestRetryHandler implements RxRetryHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExecuteDocumentClientRequestRetryHandler.class);
    
    private final RetryPolicy discoveryRetryPolicy;
    private final RetryPolicy throttleRetryPolicy;
    private final RetryPolicy sessionReadRetryPolicy;
    
    public ExecuteDocumentClientRequestRetryHandler(RxDocumentServiceRequest request,
            EndpointManager globalEndpointManager, AsyncDocumentClient client) {    

        this.discoveryRetryPolicy = RetryPolicyBridgeInternal.createEndpointDiscoveryRetryPolicy(
                client.getConnectionPolicy(),
                globalEndpointManager);

        this.throttleRetryPolicy = RetryPolicyBridgeInternal.createResourceThrottleRetryPolicy(
                client.getConnectionPolicy().getRetryOptions().getMaxRetryAttemptsOnThrottledRequests(),
                client.getConnectionPolicy().getRetryOptions().getMaxRetryWaitTimeInSeconds());

        this.sessionReadRetryPolicy = RetryPolicyBridgeInternal.createSessionReadRetryPolicy(
                globalEndpointManager, request);
    }

    @Override
    public Observable<Long> handleRetryAttempt(Throwable t, int attemptNumber) {

        if (t instanceof DocumentClientException) {
            try {
                return handleRetryAttemptInternal((DocumentClientException) t, attemptNumber);
            } catch (Exception e) {
                return Observable.error(e);
            }
        } else {
            return Observable.error(t);
        }
    }

    public Observable<Long> handleRetryAttemptInternal(DocumentClientException e, int attemptNumber) throws DocumentClientException {

        LOGGER.trace("Executing DocumentClientRequest");
        
        RetryPolicy retryPolicy = null;
        if (e.getStatusCode() == HttpConstants.StatusCodes.FORBIDDEN && e.getSubStatusCode() != null
                && e.getSubStatusCode() == HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN) {
            // If HttpStatusCode is 403 (Forbidden) and SubStatusCode is
            // 3 (WriteForbidden),
            // invoke the endpoint discovery retry policy
            retryPolicy = discoveryRetryPolicy;
        } else if (e.getStatusCode() == HttpConstants.StatusCodes.TOO_MANY_REQUESTS) {
            // If HttpStatusCode is 429 (Too Many Requests), invoke the
            // throttle retry policy
            retryPolicy = throttleRetryPolicy;
        } else if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND && e.getSubStatusCode() != null
                && e.getSubStatusCode() == HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE) {
            // If HttpStatusCode is 404 (NotFound) and SubStatusCode is
            // 1002 (ReadSessionNotAvailable), invoke the session read retry policy
            retryPolicy = sessionReadRetryPolicy;
        }

        if (retryPolicy == null || !retryPolicy.shouldRetry(e)) {
            LOGGER.trace("Execution encontured exception: {}, status code {} sub status code {}. Won't retry!", 
                    e.getMessage(), e.getStatusCode(), e.getSubStatusCode());
            return Observable.error(e);
        }
        LOGGER.trace("Execution encontured exception: {}, status code {} sub status code {}. Will retry in {}ms", 
                e.getMessage(), e.getStatusCode(), e.getSubStatusCode(), retryPolicy.getRetryAfterInMilliseconds());
        
        return Observable.timer(retryPolicy.getRetryAfterInMilliseconds(), TimeUnit.MILLISECONDS);
    }
}
