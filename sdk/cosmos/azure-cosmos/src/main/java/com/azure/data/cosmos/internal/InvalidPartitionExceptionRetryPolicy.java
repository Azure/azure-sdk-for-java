// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class InvalidPartitionExceptionRetryPolicy implements IDocumentClientRetryPolicy {

    private final RxCollectionCache clientCollectionCache;
    private final IDocumentClientRetryPolicy nextPolicy;
    private final String collectionLink;
    private final FeedOptions feedOptions;

    private volatile boolean retried = false;

    public InvalidPartitionExceptionRetryPolicy(RxCollectionCache collectionCache,
            IDocumentClientRetryPolicy nextPolicy,
            String resourceFullName,
            FeedOptions feedOptions) {

        this.clientCollectionCache = collectionCache;
        this.nextPolicy = nextPolicy;

        // TODO the resource address should be inferred from exception
        this.collectionLink = com.azure.data.cosmos.internal.Utils.getCollectionName(resourceFullName);
        this.feedOptions = feedOptions;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.nextPolicy.onBeforeSendRequest(request);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        CosmosClientException clientException = Utils.as(e, CosmosClientException.class);
        if (clientException != null && 
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.GONE) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE)) {
            if (!this.retried) {
                // TODO: resource address should be accessible from the exception
                //this.clientCollectionCache.Refresh(clientException.ResourceAddress);
                // TODO: this is blocking. is that fine?
                if(this.feedOptions != null) {
                    this.clientCollectionCache.refresh(collectionLink,this.feedOptions.properties());
                } else {
                    this.clientCollectionCache.refresh(collectionLink,null);
                }

                this.retried = true;
                return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
            } else {
                return Mono.just(ShouldRetryResult.error(e));
            }
        }

        return this.nextPolicy.shouldRetry(e);
    }
}
