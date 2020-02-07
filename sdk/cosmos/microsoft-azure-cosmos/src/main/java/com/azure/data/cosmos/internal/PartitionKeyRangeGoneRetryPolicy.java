// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.internal.caches.IPartitionKeyRangeCache;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import reactor.core.publisher.Mono;

import java.time.Duration;

// TODO: this need testing
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PartitionKeyRangeGoneRetryPolicy implements IDocumentClientRetryPolicy {

    private final RxCollectionCache collectionCache;
    private final IDocumentClientRetryPolicy nextRetryPolicy;
    private final IPartitionKeyRangeCache partitionKeyRangeCache;
    private final String collectionLink;
    private final FeedOptions feedOptions;
    private volatile boolean retried;

    public PartitionKeyRangeGoneRetryPolicy(
            RxCollectionCache collectionCache,
            IPartitionKeyRangeCache partitionKeyRangeCache,
            String collectionLink,
            IDocumentClientRetryPolicy nextRetryPolicy,
            FeedOptions feedOptions) {
        this.collectionCache = collectionCache;
        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.collectionLink = collectionLink;
        this.nextRetryPolicy = nextRetryPolicy;
        this.feedOptions = feedOptions;
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
                request.properties = this.feedOptions.properties();
            }
            Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(request);

            return collectionObs.flatMap(collectionValueHolder -> {

                Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = this.partitionKeyRangeCache.tryLookupAsync(collectionValueHolder.v.resourceId(),
                    null, request.properties);

                Mono<Utils.ValueHolder<CollectionRoutingMap>> refreshedRoutingMapObs = routingMapObs.flatMap(routingMapValueHolder -> {
                    if (routingMapValueHolder.v != null) {
                        // Force refresh.
                        return this.partitionKeyRangeCache.tryLookupAsync(
                            collectionValueHolder.v.resourceId(),
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
        this.nextRetryPolicy.onBeforeSendRequest(request);        
    }

}
