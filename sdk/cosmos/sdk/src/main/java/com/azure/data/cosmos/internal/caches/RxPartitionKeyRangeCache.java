// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.caches;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.internal.Exceptions;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import com.azure.data.cosmos.internal.routing.IServerIdentity;
import com.azure.data.cosmos.internal.routing.InMemoryCollectionRoutingMap;
import com.azure.data.cosmos.internal.routing.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 **/
public class RxPartitionKeyRangeCache implements IPartitionKeyRangeCache {
    private final Logger logger = LoggerFactory.getLogger(RxPartitionKeyRangeCache.class);

    private final AsyncCache<String, CollectionRoutingMap> routingMapCache;
    private final AsyncDocumentClient client;
    private final RxCollectionCache collectionCache;

    public RxPartitionKeyRangeCache(AsyncDocumentClient client, RxCollectionCache collectionCache) {
        this.routingMapCache = new AsyncCache<>();
        this.client = client;
        this.collectionCache = collectionCache;
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryLookupAsync(java.lang.STRING, com.azure.data.cosmos.internal.routing.CollectionRoutingMap)
     */
    @Override
    public Mono<CollectionRoutingMap> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue, Map<String, Object> properties) {
        return routingMapCache.getAsync(
                collectionRid,
                previousValue,
                () -> getRoutingMapForCollectionAsync(collectionRid, previousValue, properties))
                .onErrorResume(err -> {
                    logger.debug("tryLookupAsync on collectionRid {} encountered failure", collectionRid, err);
                    CosmosClientException dce = Utils.as(err, CosmosClientException.class);
                    if (dce != null && Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND)) {
                        return Mono.empty();
                    }

                    return Mono.error(err);
                });
    }

    @Override
    public Mono<CollectionRoutingMap> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue, boolean forceRefreshCollectionRoutingMap,
            Map<String, Object> properties) {
        return tryLookupAsync(collectionRid, previousValue, properties);
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetOverlappingRangesAsync(java.lang.STRING, com.azure.data.cosmos.internal.routing.RANGE, boolean)
     */
    @Override
    public Mono<List<PartitionKeyRange>> tryGetOverlappingRangesAsync(String collectionRid, Range<String> range, boolean forceRefresh,
            Map<String, Object> properties) {

        Mono<CollectionRoutingMap> routingMapObs = tryLookupAsync(collectionRid, null, properties);

        return routingMapObs.flatMap(routingMap -> {
            if (forceRefresh) {
                logger.debug("tryGetOverlappingRangesAsync with forceRefresh on collectionRid {}", collectionRid);
                return tryLookupAsync(collectionRid, routingMap, properties);
            }

            return Mono.just(routingMap);
        }).switchIfEmpty(Mono.empty()).map(routingMap -> routingMap.getOverlappingRanges(range)).switchIfEmpty(Mono.defer(() -> {
            logger.debug("Routing Map Null for collection: {} for range: {}, forceRefresh:{}", collectionRid, range.toString(), forceRefresh);
            return Mono.empty();
        }));
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetPartitionKeyRangeByIdAsync(java.lang.STRING, java.lang.STRING, boolean)
     */
    @Override
    public Mono<PartitionKeyRange> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId, String partitionKeyRangeId,
            boolean forceRefresh, Map<String, Object> properties) {

        Mono<CollectionRoutingMap> routingMapObs = tryLookupAsync(collectionResourceId, null, properties);

        return routingMapObs.flatMap(routingMap -> {
            if (forceRefresh && routingMap != null) {
                return tryLookupAsync(collectionResourceId, routingMap, properties);
            }
            return Mono.justOrEmpty(routingMap);

        }).switchIfEmpty(Mono.defer(Mono::empty)).map(routingMap -> routingMap.getRangeByPartitionKeyRangeId(partitionKeyRangeId)).switchIfEmpty(Mono.defer(() -> {
            logger.debug("Routing Map Null for collection: {}, PartitionKeyRangeId: {}, forceRefresh:{}", collectionResourceId, partitionKeyRangeId, forceRefresh);
            return null;
        }));
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetRangeByPartitionKeyRangeId(java.lang.STRING, java.lang.STRING)
     */
    @Override
    public Mono<PartitionKeyRange> tryGetRangeByPartitionKeyRangeId(String collectionRid, String partitionKeyRangeId, Map<String, Object> properties) {
        Mono<CollectionRoutingMap> routingMapObs = routingMapCache.getAsync(
                collectionRid,
                null,
                () -> getRoutingMapForCollectionAsync(collectionRid, null, properties));

        return routingMapObs.map(routingMap -> routingMap.getRangeByPartitionKeyRangeId(partitionKeyRangeId))
                .onErrorResume(err -> {
                    CosmosClientException dce = Utils.as(err, CosmosClientException.class);
                    logger.debug("tryGetRangeByPartitionKeyRangeId on collectionRid {} and partitionKeyRangeId {} encountered failure",
                            collectionRid, partitionKeyRangeId, err);

                    if (dce != null && Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND)) {
                        return Mono.empty();
                    }

                    return Mono.error(dce);
                });
    }

    private Mono<CollectionRoutingMap> getRoutingMapForCollectionAsync(
            String collectionRid,
            CollectionRoutingMap previousRoutingMap,
            Map<String, Object> properties) {

        // TODO: NOTE: main java code doesn't do anything in regard to the previous routing map
        // .Net code instead of using DocumentClient controls sending request and receiving requests here

        // here we stick to what main java sdk does, investigate later.

        Mono<List<PartitionKeyRange>> rangesObs = getPartitionKeyRange(collectionRid, false, properties);

        return rangesObs.flatMap(ranges -> {

            List<ImmutablePair<PartitionKeyRange, IServerIdentity>> rangesTuples =
                    ranges.stream().map(range -> new  ImmutablePair<>(range, (IServerIdentity) null)).collect(Collectors.toList());


            CollectionRoutingMap routingMap;
            if (previousRoutingMap == null)
            {
                // Splits could have happened during change feed query and we might have a mix of gone and new ranges.
                Set<String> goneRanges = new HashSet<>(ranges.stream().flatMap(range -> CollectionUtils.emptyIfNull(range.getParents()).stream()).collect(Collectors.toSet()));

                routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
                    rangesTuples.stream().filter(tuple -> !goneRanges.contains(tuple.left.id())).collect(Collectors.toList()),
                    collectionRid);
            }
            else
            {
                routingMap = previousRoutingMap.tryCombine(rangesTuples);
            }

            if (routingMap == null)
            {
                // RANGE information either doesn't exist or is not complete.
                return Mono.error(new NotFoundException(String.format("GetRoutingMapForCollectionAsync(collectionRid: {%s}), RANGE information either doesn't exist or is not complete.", collectionRid)));
            }

            return Mono.just(routingMap);
        });
    }

    private Mono<List<PartitionKeyRange>> getPartitionKeyRange(String collectionRid, boolean forceRefresh, Map<String, Object> properties) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.ReadFeed,
                collectionRid,
                ResourceType.PartitionKeyRange,
                null
                ); //this request doesn't actually go to server

        request.requestContext.resolvedCollectionRid = collectionRid;
        Mono<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(request);

        return collectionObs.flatMap(coll -> {

            FeedOptions feedOptions = new FeedOptions();
            if (properties != null) {
                feedOptions.properties(properties);
            }
            return client.readPartitionKeyRanges(coll.selfLink(), feedOptions)
                    // maxConcurrent = 1 to makes it in the right order
                    .flatMap(p -> Flux.fromIterable(p.results()), 1).collectList();
        });
    }
}

