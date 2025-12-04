// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class StaleResourceRetryPolicy extends DocumentClientRetryPolicy {

    private final static Logger logger  = LoggerFactory.getLogger(StaleResourceRetryPolicy.class);

    private final RxCollectionCache clientCollectionCache;
    private final DocumentClientRetryPolicy nextPolicy;
    private final String collectionLink;
    private final Map<String, Object> requestOptionProperties;
    private final AtomicBoolean shouldSuppressRetry;
    private final ISessionContainer sessionContainer;
    private RxDocumentServiceRequest request;
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final AtomicReference<CosmosDiagnostics> cosmosDiagnosticsHolder;

    private volatile boolean retried = false;

    public StaleResourceRetryPolicy(
        RxCollectionCache collectionCache,
        DocumentClientRetryPolicy nextPolicy,
        String resourceFullName,
        Map<String, Object> requestOptionProperties,
        Map<String, String> requestCustomHeaders,
        ISessionContainer sessionContainer,
        DiagnosticsClientContext diagnosticsClientContext) {

        this.clientCollectionCache = collectionCache;
        this.nextPolicy = nextPolicy;

        // TODO the resource address should be inferred from exception
        this.collectionLink = Utils.getCollectionName(resourceFullName);
        this.requestOptionProperties = requestOptionProperties;
        this.shouldSuppressRetry = new AtomicBoolean(this.shouldSuppressRetry(requestCustomHeaders));
        this.sessionContainer = sessionContainer;

        this.diagnosticsClientContext = diagnosticsClientContext;
        this.cosmosDiagnosticsHolder = new AtomicReference<>(null); // will only create one if no request is bound to the retry policy
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
                // 1. refresh the collection cache if needed
                // 2. If the collection rid has changed, then also clean up session container for old containerRid
                AtomicReference<String> oldCollectionRid = new AtomicReference<>();
                return this.clientCollectionCache
                    .resolveByNameAsync(this.getMetadataDiagnosticsContext(), collectionLink, requestOptionProperties)
                    .flatMap(collectionInCache -> {
                        oldCollectionRid.set(collectionInCache.getResourceId());

                        if (this.request == null
                            || this.request.requestContext == null
                            || StringUtils.equals(collectionInCache.getResourceId(), this.request.requestContext.resolvedCollectionRid)) {

                            this.clientCollectionCache.refresh(
                                this.getMetadataDiagnosticsContext(),
                                collectionLink,
                                requestOptionProperties
                            );

                            return this.clientCollectionCache
                                .resolveByNameAsync(
                                    this.getMetadataDiagnosticsContext(),
                                    collectionLink,
                                    requestOptionProperties
                                )
                                .map(DocumentCollection :: getResourceId);
                        }

                        return Mono.just(oldCollectionRid.get());
                    })
                    .flatMap(refreshedCollectionRid -> {
                        if (!StringUtils.equals(refreshedCollectionRid, oldCollectionRid.get())) {
                            this.sessionContainer.clearTokenByResourceId(oldCollectionRid.get());
                        }

                        this.retried = true;
                        if (this.shouldSuppressRetry.get()) {
                            return Mono.just(ShouldRetryResult.error(e));
                        }

                        return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
                    })
                    .onErrorMap(throwable -> {

                        if (throwable instanceof CosmosException) {

                            CosmosException cosmosException = Utils.as(throwable, CosmosException.class);

                            if (!ResourceType.DocumentCollection.equals(this.request.getResourceType()) && Exceptions.isNotFound(cosmosException)) {
                                BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS);
                            }

                            return cosmosException;
                        }

                        return throwable;
                    });

            } else {
                logger.warn(
                    "Received second {} after backoff/retry. Will fail the request. {}",
                    clientException.getClass().getSimpleName(),
                    clientException.toString());

                // for server returned staled cache exception(410/1000). if still failing after retry, rewrap to service unavailable exception
                if (isServerNameCacheStaledException(clientException)) {
                    return Mono.just(
                        ShouldRetryResult.error(
                            BridgeInternal.createServiceUnavailableException(
                                clientException,
                                HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE_EXCEEDED_RETRY_LIMIT)
                    ));
                }

                return Mono.just(ShouldRetryResult.error(e));
            }
        }

        if (this.nextPolicy != null) {
            return this.nextPolicy.shouldRetry(e);
        }
        return Mono.just(ShouldRetryResult.error(e));
    }

    private MetadataDiagnosticsContext getMetadataDiagnosticsContext() {
        if (this.request != null) {
            return BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosDiagnostics);
        }

        if (this.cosmosDiagnosticsHolder.get() == null) {
            this.cosmosDiagnosticsHolder.set(this.diagnosticsClientContext.createDiagnostics());
        }

        return BridgeInternal.getMetaDataDiagnosticContext(this.cosmosDiagnosticsHolder.get());
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

    private boolean shouldSuppressRetry(Map<String, String> requestCustomHeaders) {
        if (requestCustomHeaders == null)
        {
            return false;
        }

        // Refresh the sdk collection cache and throw the exception if intendedCollectionRid was passed by outside sdk,
        // so caller will refresh their own collection cache if they have one
        // Cosmos encryption is one use case
        return
            StringUtils.isNotEmpty(
                requestCustomHeaders.get(HttpConstants.HttpHeaders.INTENDED_COLLECTION_RID_HEADER));
    }
}
