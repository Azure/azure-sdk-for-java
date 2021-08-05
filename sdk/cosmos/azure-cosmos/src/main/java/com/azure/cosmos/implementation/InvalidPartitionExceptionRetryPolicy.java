// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.CosmosException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class InvalidPartitionExceptionRetryPolicy extends DocumentClientRetryPolicy {

    private final RxCollectionCache clientCollectionCache;
    private final DocumentClientRetryPolicy nextPolicy;
    private final String collectionLink;
    private final Map<String, Object> requestOptionProperties;
    private RxDocumentServiceRequest request;

    private volatile boolean retried = false;

    public InvalidPartitionExceptionRetryPolicy(RxCollectionCache collectionCache,
            DocumentClientRetryPolicy nextPolicy,
            String resourceFullName,
            Map<String, Object> requestOptionProperties) {

        this.clientCollectionCache = collectionCache;
        this.nextPolicy = nextPolicy;

        // TODO the resource address should be inferred from exception
        this.collectionLink = Utils.getCollectionName(resourceFullName);
        this.requestOptionProperties = requestOptionProperties;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        if (this.nextPolicy != null) {
            this.nextPolicy.onBeforeSendRequest(request);
        }
    }

    @Override
    public RetryContext getRetryContext() {
        if (this.nextPolicy != null) {
            return this.nextPolicy.getRetryContext();
        } else {
            return null;
        }
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        CosmosException clientException = Utils.as(e, CosmosException.class);
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.GONE) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE)) {
            if (!this.retried) {
                // TODO: resource address should be accessible from the exception
                //this.clientCollectionCache.Refresh(clientException.ResourceAddress);
                // TODO: this is blocking. is that fine?
                this.clientCollectionCache.refresh(
                    this.request != null ? BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosDiagnostics) : null,
                    collectionLink,
                    requestOptionProperties);

                this.retried = true;
                return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
            } else {
                return Mono.just(ShouldRetryResult.error(e));
            }
        }

        if (this.nextPolicy != null) {
            return this.nextPolicy.shouldRetry(e);
        }
        return Mono.just(ShouldRetryResult.error(e));
    }
}
