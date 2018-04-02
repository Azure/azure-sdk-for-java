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

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;

import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class InvalidPartitionExceptionRetryPolicy implements IDocumentClientRetryPolicy {

    private final RxCollectionCache clientCollectionCache;
    private final IDocumentClientRetryPolicy nextPolicy;
    private final String collectionLink;

    private volatile boolean retried = false;

    public InvalidPartitionExceptionRetryPolicy(RxCollectionCache collectionCache,
            IDocumentClientRetryPolicy nextPolicy,
            String resourceFullName) {

        this.clientCollectionCache = collectionCache;
        this.nextPolicy = nextPolicy;

        // TODO the resource address should be inferred from exception
        this.collectionLink = com.microsoft.azure.cosmosdb.internal.Utils.getCollectionName(resourceFullName);
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.nextPolicy.onBeforeSendRequest(request);
    }

    @Override
    public Single<ShouldRetryResult> shouldRetry(Exception e) {
        DocumentClientException clientException = Utils.as(e, DocumentClientException.class);
        if (clientException != null && 
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.GONE) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE)) {
            if (!this.retried) {
                // TODO: resource address should be accessible from the exception
                //this.clientCollectionCache.Refresh(clientException.ResourceAddress);
                // TODO: this is blocking. is that fine?
                this.clientCollectionCache.refresh(collectionLink);
                this.retried = true;
                return Single.just(ShouldRetryResult.retryAfter(Duration.ZERO));
            } else {
                return Single.just(ShouldRetryResult.error(e));
            }
        }

        return this.nextPolicy.shouldRetry(e);
    }
}
