// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.CollectionUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.IServerIdentity;
import com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
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
    private final RxDocumentClientImpl client;
    private final RxCollectionCache collectionCache;
    private final DiagnosticsClientContext clientContext;

    public RxPartitionKeyRangeCache(RxDocumentClientImpl client, RxCollectionCache collectionCache) {
        this.routingMapCache = new AsyncCache<>();
        this.client = client;
        this.collectionCache = collectionCache;
        this.clientContext = client;
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryLookupAsync(java.lang.STRING, com.azure.cosmos.internal.routing.CollectionRoutingMap)
     */
    @Override
    public Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, CollectionRoutingMap previousValue, Map<String, Object> properties) {
        return routingMapCache.getAsync(
                collectionRid,
                previousValue,
                () -> getRoutingMapForCollectionAsync(metaDataDiagnosticsContext, collectionRid, previousValue, properties))
                              .map(Utils.ValueHolder::new)
                              .onErrorResume(err -> {
                                  logger.debug("tryLookupAsync on collectionRid {} encountered failure", collectionRid, err);
                                  CosmosException dce = Utils.as(err, CosmosException.class);
                                  if (dce != null && Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND)) {
                                      return Mono.just(new Utils.ValueHolder<>(null));
                                  }

                                  return Mono.error(err);
                              });
    }

    @Override
    public Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                                        String collectionRid,
                                                                        CollectionRoutingMap previousValue,
                                                                        boolean forceRefreshCollectionRoutingMap,
                                                                        Map<String, Object> properties) {
        return tryLookupAsync(metaDataDiagnosticsContext, collectionRid, previousValue, properties);
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetOverlappingRangesAsync(java.lang.STRING, com.azure.cosmos.internal.routing.RANGE, boolean)
     */
    @Override
    public Mono<Utils.ValueHolder<List<PartitionKeyRange>>> tryGetOverlappingRangesAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                                                         String collectionRid,
                                                                                         Range<String> range,
                                                                                         boolean forceRefresh,
                                                                                         Map<String, Object> properties) {

        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = tryLookupAsync(metaDataDiagnosticsContext, collectionRid, null, properties);

        return routingMapObs.flatMap(routingMapValueHolder -> {
            if (forceRefresh && routingMapValueHolder.v != null) {
                logger.debug("tryGetOverlappingRangesAsync with forceRefresh on collectionRid {}", collectionRid);
                return tryLookupAsync(metaDataDiagnosticsContext, collectionRid, routingMapValueHolder.v, properties);
            }

            return Mono.just(routingMapValueHolder);
        }).map(routingMapValueHolder -> {
            if (routingMapValueHolder.v != null) {
                // TODO: the routingMap.getOverlappingRanges(range) returns Collection
                // maybe we should consider changing to ArrayList to avoid conversion
                return new Utils.ValueHolder<>(new ArrayList<>(routingMapValueHolder.v.getOverlappingRanges(range)));
            } else {
                logger.debug("Routing Map Null for collection: {} for range: {}, forceRefresh:{}", collectionRid, range, forceRefresh);
                return new Utils.ValueHolder<>(null);
            }
        });
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetPartitionKeyRangeByIdAsync(java.lang.STRING, java.lang.STRING, boolean)
     */
    @Override
    public Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetPartitionKeyRangeByIdAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                                                       String collectionResourceId,
                                                                                       String partitionKeyRangeId,
                                                                                       boolean forceRefresh,
                                                                                       Map<String, Object> properties) {

        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = tryLookupAsync(metaDataDiagnosticsContext, collectionResourceId, null, properties);

        return routingMapObs.flatMap(routingMapValueHolder -> {
            if (forceRefresh && routingMapValueHolder.v != null) {
                return tryLookupAsync(metaDataDiagnosticsContext, collectionResourceId, routingMapValueHolder.v, properties);
            }
            return Mono.just(routingMapValueHolder);

        }).map(routingMapValueHolder -> {
            if (routingMapValueHolder.v != null) {
                return new Utils.ValueHolder<>(routingMapValueHolder.v.getRangeByPartitionKeyRangeId(partitionKeyRangeId));
            } else {
                logger.debug("Routing Map Null for collection: {}, PartitionKeyRangeId: {}, forceRefresh:{}", collectionResourceId, partitionKeyRangeId, forceRefresh);
                return new Utils.ValueHolder<>(null);
            }
        });
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetRangeByPartitionKeyRangeId(java.lang.STRING, java.lang.STRING)
     */
    @Override
    public Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetRangeByPartitionKeyRangeId(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, String partitionKeyRangeId, Map<String, Object> properties) {
        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = routingMapCache.getAsync(
                collectionRid,
                null,
                () -> getRoutingMapForCollectionAsync(metaDataDiagnosticsContext, collectionRid, null, properties)).map(Utils.ValueHolder::new);

        return routingMapObs.map(routingMapValueHolder -> new Utils.ValueHolder<>(routingMapValueHolder.v.getRangeByPartitionKeyRangeId(partitionKeyRangeId)))
                .onErrorResume(err -> {
                    CosmosException dce = Utils.as(err, CosmosException.class);
                    logger.debug("tryGetRangeByPartitionKeyRangeId on collectionRid {} and partitionKeyRangeId {} encountered failure",
                            collectionRid, partitionKeyRangeId, err);

                    if (dce != null && Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND)) {
                        return Mono.just(new Utils.ValueHolder<>(null));
                    }

                    return Mono.error(dce);
                });
    }

    private Mono<CollectionRoutingMap> getRoutingMapForCollectionAsync(
        MetadataDiagnosticsContext metaDataDiagnosticsContext,
            String collectionRid,
            CollectionRoutingMap previousRoutingMap,
            Map<String, Object> properties) {

        // TODO: NOTE: main java code doesn't do anything in regard to the previous routing map
        // .Net code instead of using DocumentClient controls sending request and receiving requests here

        // here we stick to what main java sdk does, investigate later.

        Mono<List<PartitionKeyRange>> rangesObs = getPartitionKeyRange(metaDataDiagnosticsContext, collectionRid , false, properties);

        return rangesObs.flatMap(ranges -> {

            List<ImmutablePair<PartitionKeyRange, IServerIdentity>> rangesTuples =
                    ranges.stream().map(range -> new  ImmutablePair<>(range, (IServerIdentity) null)).collect(Collectors.toList());


            CollectionRoutingMap routingMap;
            if (previousRoutingMap == null)
            {
                // Splits could have happened during change feed query and we might have a mix of gone and new ranges.
                Set<String> goneRanges = new HashSet<>(ranges.stream().flatMap(range -> CollectionUtils.emptyIfNull(range.getParents()).stream()).collect(Collectors.toSet()));

                routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
                    rangesTuples.stream().filter(tuple -> !goneRanges.contains(tuple.left.getId())).collect(Collectors.toList()),
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

    private Mono<List<PartitionKeyRange>> getPartitionKeyRange(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, boolean forceRefresh, Map<String, Object> properties) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this.clientContext,
                OperationType.ReadFeed,
                collectionRid,
                ResourceType.PartitionKeyRange,
                null
                ); //this request doesn't actually go to server

        request.requestContext.resolvedCollectionRid = collectionRid;
        Mono<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(metaDataDiagnosticsContext, request)
                                                                .map(collectionValueHolder -> collectionValueHolder.v);

        return collectionObs.flatMap(coll -> {

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
            if (properties != null) {
                ModelBridgeInternal.setQueryRequestOptionsProperties(cosmosQueryRequestOptions, properties);
            }
            Instant addressCallStartTime = Instant.now();
            return client.readPartitionKeyRanges(coll.getSelfLink(), cosmosQueryRequestOptions)
                    // maxConcurrent = 1 to makes it in the right getOrder
                    .flatMap(p -> {
                        if(metaDataDiagnosticsContext != null) {
                            Instant addressCallEndTime = Instant.now();
                            MetadataDiagnosticsContext.MetadataDiagnostics metaDataDiagnostic  = new MetadataDiagnosticsContext.MetadataDiagnostics(addressCallStartTime,
                                addressCallEndTime,
                                MetadataDiagnosticsContext.MetadataType.PARTITION_KEY_RANGE_LOOK_UP);
                            metaDataDiagnosticsContext.addMetaDataDiagnostic(metaDataDiagnostic);
                        }

                        return Flux.fromIterable(p.getResults());
                    }, 1).collectList();
        });
    }
}
