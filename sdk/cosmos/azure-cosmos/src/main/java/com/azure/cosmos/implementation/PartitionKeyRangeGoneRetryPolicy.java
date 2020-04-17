// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.caches.IPartitionKeyRangeCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.models.FeedOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;

// TODO: this need testing
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PartitionKeyRangeGoneRetryPolicy extends DocumentClientRetryPolicy {

    private final RxCollectionCache collectionCache;
    private final DocumentClientRetryPolicy nextRetryPolicy;
    private final IPartitionKeyRangeCache partitionKeyRangeCache;
    private final String collectionLink;
    private final FeedOptions feedOptions;
    private volatile boolean retried;
    private RxDocumentServiceRequest request;

    public PartitionKeyRangeGoneRetryPolicy(
            RxCollectionCache collectionCache,
            IPartitionKeyRangeCache partitionKeyRangeCache,
            String collectionLink,
            DocumentClientRetryPolicy nextRetryPolicy,
            FeedOptions feedOptions) {
        this.collectionCache = collectionCache;
        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.collectionLink = collectionLink;
        this.nextRetryPolicy = nextRetryPolicy;
        this.feedOptions = feedOptions;
        this.request = null;
    }

    /// <summary>
    /// Should the caller retry the operation.
    /// </summary>
    /// <param name="exception">Exception that occured when the operation was tried</param>
    /// <param name="cancellationToken"></param>
    /// <returns>True indicates caller should retry, False otherwise</returns>
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        CosmosClientException clientException = Utils.as(exception, CosmosClientException.class);
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.GONE) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE)) {

            if (this.retried){
                return Mono.just(ShouldRetryResult.error(clientException));
            }

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    this.collectionLink,
                    null
                    // AuthorizationTokenType.PrimaryMasterKey)
                    );
            if (this.feedOptions != null) {
                request.properties = this.feedOptions.getProperties();
            }
            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(
                BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosResponseDiagnostics),
                request);

            return collectionObs.flatMap(collectionValueHolder -> {

                Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = this.partitionKeyRangeCache.tryLookupAsync(
                    BridgeInternal.getMetaDataDiagnosticContext(this.request.requestContext.cosmosResponseDiagnostics),
                    collectionValueHolder.v.getResourceId(),
                    null,
                    request.properties);

                Mono<Utils.ValueHolder<CollectionRoutingMap>> refreshedRoutingMapObs = routingMapObs.flatMap(routingMapValueHolder -> {
                    if (routingMapValueHolder.v != null) {
                        // Force refresh.
                        return this.partitionKeyRangeCache.tryLookupAsync(
                            null,
                            collectionValueHolder.v.getResourceId(),
                            routingMapValueHolder.v,
                            request.properties);
                    } else {
                        return Mono.just(new Utils.ValueHolder<>(null));
                    }
                });

                //  TODO: Check if this behavior can be replaced by doOnSubscribe
                return refreshedRoutingMapObs.flatMap(rm -> {
                    this.retried = true;
                    return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
                });

            });

        } else {
            return this.nextRetryPolicy.shouldRetry(exception);
        }
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.nextRetryPolicy.onBeforeSendRequest(request);
    }

}
