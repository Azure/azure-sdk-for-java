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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.documentdb.internal.RetryPolicy;
import com.microsoft.azure.documentdb.internal.RetryPolicyBridgeInternal;
import com.microsoft.azure.documentdb.internal.routing.ClientCollectionCache;

import rx.Observable;

class CreateDocumentRetryHandler implements RxRetryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDocumentRetryHandler.class);
    private final RetryPolicy keyMismatchRetryPolicy;

    public CreateDocumentRetryHandler(ClientCollectionCache clientCollectionCache,
            String resourcePath) {
        
        this.keyMismatchRetryPolicy = RetryPolicyBridgeInternal
                .createPartitionKeyMismatchRetryPolicy(resourcePath, clientCollectionCache);
    }

    @Override
    public Observable<Long> handleRetryAttempt(Throwable t, int attemptNumber) {
        
        if (t instanceof DocumentClientException) {
            try {
                return handleRetryAttemptInternal((DocumentClientException) t, attemptNumber);
            } catch (DocumentClientException e) {
                return Observable.error(e);
            }
        } else {
            return Observable.error(t);
        }
    }

    private Observable<Long> handleRetryAttemptInternal(DocumentClientException e, int attemptNumber) throws DocumentClientException {

        RetryPolicy retryPolicy = null;
        if (e.getStatusCode() == HttpConstants.StatusCodes.BADREQUEST && e.getSubStatusCode() != null
                && e.getSubStatusCode() == HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH) {
            // If HttpStatusCode is 404 (NotFound) and SubStatusCode is
            // 1001 (PartitionKeyMismatch), invoke the partition key mismatch retry policy
            retryPolicy = keyMismatchRetryPolicy;
        }

        if (retryPolicy == null || !retryPolicy.shouldRetry(e)) {
            LOGGER.trace("Execution encontured exception: {}, status code {} sub status code {}. Won't retry!", 
                    e.getMessage(), e.getStatusCode(), e.getSubStatusCode());
            return Observable.error(e);
        }
        LOGGER.trace("Execution encontured exception: {}, status code {} sub status code {}. Will retry in {}ms", 
                e.getMessage(), e.getStatusCode(), e.getSubStatusCode(), retryPolicy.getRetryAfterInMilliseconds());
       
        long waitTime = retryPolicy.getRetryAfterInMilliseconds();
        return Observable.just(waitTime);
    }
}
