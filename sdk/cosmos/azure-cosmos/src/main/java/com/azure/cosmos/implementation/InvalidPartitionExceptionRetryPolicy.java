// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.QueryRequestOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class InvalidPartitionExceptionRetryPolicy extends DocumentClientRetryPolicy {

    private final RxCollectionCache clientCollectionCache;
    private final DocumentClientRetryPolicy nextPolicy;
    private final String collectionLink;
    private final QueryRequestOptions queryRequestOptions;
    private RxDocumentServiceRequest request;

    private volatile boolean retried = false;

    public InvalidPartitionExceptionRetryPolicy(RxCollectionCache collectionCache,
            DocumentClientRetryPolicy nextPolicy,
            String resourceFullName,
            QueryRequestOptions queryRequestOptions) {

        this.clientCollectionCache = collectionCache;
        this.nextPolicy = nextPolicy;

        // TODO the resource address should be inferred from exception
        this.collectionLink = Utils.getCollectionName(resourceFullName);
        this.queryRequestOptions = queryRequestOptions;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.nextPolicy.onBeforeSendRequest(request);
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
                if(this.queryRequestOptions != null) {
                    this.clientCollectionCache.refresh(
                        BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosDiagnostics),
                        collectionLink,
                        ModelBridgeInternal.getPropertiesFromQueryRequestOptions(this.queryRequestOptions));
                } else {
                    this.clientCollectionCache.refresh(
                        BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosDiagnostics),
                        collectionLink,
                        null);
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
