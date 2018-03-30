/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
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
package com.microsoft.azure.cosmosdb.rx.internal;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxClientCollectionCache;

import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 * 
 * A RetryPolicy implementation that ensures the PartitionKeyDefinitionMap is up-to-date.
 * Entries in the PartitionKeyDefinitionMap can become stale if a collection is deleted
 * and then recreated with the same name but a different partition key definition, if
 * the request is made using name-based links.
 * 
 * TODO: verify with Sergii, other than collection deleted and recreated with the same name 
 *       is there any other scenario which this should be used?
 *       
 */
public class PartitionKeyMismatchRetryPolicy implements IDocumentClientRetryPolicy {
    private RxClientCollectionCache clientCollectionCache;
    private IDocumentClientRetryPolicy nextRetryPolicy;
    private AtomicInteger retriesAttempted = new AtomicInteger(0);
    private String collectionLink;
    private final static int MaxRetries = 1;


    public PartitionKeyMismatchRetryPolicy(
            RxClientCollectionCache clientCollectionCache, 
            IDocumentClientRetryPolicy nextRetryPolicy, 
            String resourceFullName) {
        this.clientCollectionCache = clientCollectionCache;
        this.nextRetryPolicy = nextRetryPolicy;

        // TODO: this should be retrievable from document client exception.
        collectionLink = com.microsoft.azure.cosmosdb.internal.Utils.getCollectionName(resourceFullName);
    }


    /// <summary> 
    /// Should the caller retry the operation.
    /// </summary>
    /// <param name="exception">Exception that occured when the operation was tried</param>
    /// <param name="cancellationToken"></param>
    /// <returns>True indicates caller should retry, False otherwise</returns>
    public Single<ShouldRetryResult> shouldRetry(Exception exception) {
        DocumentClientException clientException = Utils.as(exception, DocumentClientException.class) ;

        if (clientException != null && 
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.BADREQUEST) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH)                
                && this.retriesAttempted.get() < MaxRetries) {
            //Debug.Assert(clientException.ResourceAddress != null);

            // TODO:
            //this.clientCollectionCache.refresh(clientException.ResourceAddress);
            this.clientCollectionCache.refresh(collectionLink);

            this.retriesAttempted.incrementAndGet();

            return Single.just(ShouldRetryResult.retryAfter(Duration.ZERO));
        } 

        return this.nextRetryPolicy.shouldRetry(exception);
    }


    /* (non-Javadoc)
     * @see com.microsoft.azure.cosmosdb.rx.internal.query.IDocumentClientRetryPolicy#onBeforeSendRequest(rx.Observable)
     */
    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        // TODO Auto-generated method stub
        this.nextRetryPolicy.onBeforeSendRequest(request);
    }
}
