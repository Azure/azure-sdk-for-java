// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class InvalidPartitionExceptionRetryPolicy extends DocumentClientRetryPolicy {

    private static final Logger logger = LoggerFactory.getLogger(InvalidPartitionExceptionRetryPolicy.class);
    private final RxCollectionCache clientCollectionCache;
    private final DocumentClientRetryPolicy nextPolicy;
    private final String collectionLink;
    private final Map<String, Object> requestOptionProperties;
    private final ISessionContainer sessionContainer;
    private RxDocumentServiceRequest request;

    private volatile boolean retried = false;

    public InvalidPartitionExceptionRetryPolicy(
        RxCollectionCache collectionCache,
        DocumentClientRetryPolicy nextPolicy,
        String resourceFullName,
        Map<String, Object> requestOptionProperties,
        ISessionContainer sessionContainer) {

        this.clientCollectionCache = collectionCache;
        this.nextPolicy = nextPolicy;

        // TODO the resource address should be inferred from exception
        this.collectionLink = Utils.getCollectionName(resourceFullName);
        this.requestOptionProperties = requestOptionProperties;
        this.sessionContainer = sessionContainer;
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
        if (isServerNameCacheStaledException(clientException) || isGatewayStaledContainerException(clientException)) {
            if (!this.retried) {
                // refresh the collection cache
                // also if collectionRid has changed, then also clean up session container for old containerRid
                AtomicReference<String> originalCollectionRid = new AtomicReference<>();
                return this.clientCollectionCache
                    .resolveByNameAsync(this.getMetadataDiagnosticsContext(), collectionLink, requestOptionProperties)
                    .flatMap(documentCollection -> {
                        originalCollectionRid.set(documentCollection.getResourceId());
                        this.clientCollectionCache.refresh(
                            this.getMetadataDiagnosticsContext(),
                            collectionLink,
                            requestOptionProperties
                        );

                        return this.clientCollectionCache
                            .resolveByNameAsync(this.getMetadataDiagnosticsContext(), collectionLink, requestOptionProperties);
                    })
                    .flatMap(refreshedDocumentCollection -> {
                        if (!originalCollectionRid.get().equals(refreshedDocumentCollection.getResourceId())) {
                            logger.info(
                                "Container recreate, going to clean up session container for original containerRid {}",
                                originalCollectionRid.get());

                          //  this.sessionContainer.clearTokenByResourceId(originalCollectionRid.get());
                        }

                        this.retried = true;
                        return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
                    });

            } else {
                return Mono.just(ShouldRetryResult.error(e));
            }
        }

        if (this.nextPolicy != null) {
            return this.nextPolicy.shouldRetry(e);
        }
        return Mono.just(ShouldRetryResult.error(e));
    }

    private MetadataDiagnosticsContext getMetadataDiagnosticsContext() {
        return request != null
            ? BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosDiagnostics) : null;
    }

    private boolean isServerNameCacheStaledException(CosmosException cosmosException) {
        return cosmosException != null &&
            Exceptions.isStatusCode(cosmosException, HttpConstants.StatusCodes.GONE) &&
            Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
    }

    private boolean isGatewayStaledContainerException(CosmosException cosmosException) {
        return cosmosException != null &&
            Exceptions.isStatusCode(cosmosException, HttpConstants.StatusCodes.BADREQUEST) &&
            Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS);
    }
}
