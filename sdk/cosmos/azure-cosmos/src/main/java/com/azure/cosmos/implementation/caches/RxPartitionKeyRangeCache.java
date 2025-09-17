// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
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
import reactor.util.concurrent.Queues;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 **/
public class RxPartitionKeyRangeCache implements IPartitionKeyRangeCache {
    private final Logger logger = LoggerFactory.getLogger(RxPartitionKeyRangeCache.class);

    private final AsyncCacheNonBlocking<String, CollectionRoutingMap> routingMapCache;
    private final RxDocumentClientImpl client;
    private final RxCollectionCache collectionCache;
    private final DiagnosticsClientContext clientContext;
    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor qryOptAccessor =
        ImplementationBridgeHelpers
            .CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor();

    public RxPartitionKeyRangeCache(RxDocumentClientImpl client, RxCollectionCache collectionCache) {
        this.routingMapCache = new AsyncCacheNonBlocking<>();
        this.client = client;
        this.collectionCache = collectionCache;
        this.clientContext = client;
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryLookupAsync(java.lang.STRING, com.azure.cosmos.internal.routing.CollectionRoutingMap)
     */
    @Override
    public Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, CollectionRoutingMap previousValue, Map<String, Object> properties, StringBuilder sb) {

        if (sb != null) {
            sb.append("RxPartitionKeyRangeCache.TryLookupAsync:").append(",");
        }

        String callIdentifier = UUID.randomUUID().toString();

        return routingMapCache.getAsync(
                collectionRid,
                routingMap -> getRoutingMapForCollectionAsync(metaDataDiagnosticsContext, collectionRid, previousValue,
                    properties, sb, callIdentifier), currentValue -> shouldForceRefresh(previousValue, currentValue), callIdentifier)
            .map(Utils.ValueHolder::new)
            .onErrorResume(err -> {
                logger.debug("tryLookupAsync on collectionRid {} encountered failure", collectionRid, err);
                CosmosException dce = Utils.as(err, CosmosException.class);

                // bubble up in case a 404:1002 is seen to force retries as a part of document retries
                // todo: revert change when fault injection excludes 404:1002 for master resources
                if (dce != null && Exceptions.isNotFound(dce) && !Exceptions.isSubStatusCode(dce, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)) {
                    return Mono.just(new Utils.ValueHolder<>(null));
                }

                return Mono.error(err);
            });
    }

    private boolean shouldForceRefresh(CollectionRoutingMap previousValue, CollectionRoutingMap currentValue) {
        // Previous is null then no need to force a refresh
        // The request didn't access the cache before
        if (previousValue == null) {
            return false;
        }
        // currentValue is null then the value just got initialized so
        // is not possible for it to be stale
        if (currentValue == null) {
            return false;
        }

        return previousValue == currentValue;
    }

    @Override
    public Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                                        String collectionRid,
                                                                        CollectionRoutingMap previousValue,
                                                                        boolean forceRefreshCollectionRoutingMap,
                                                                        Map<String, Object> properties,
                                                                        StringBuilder sb) {
        return tryLookupAsync(metaDataDiagnosticsContext, collectionRid, previousValue, properties, sb);
    }

    /* (non-Javadoc)
     * @see IPartitionKeyRangeCache#tryGetOverlappingRangesAsync(java.lang.STRING, com.azure.cosmos.internal.routing.RANGE, boolean)
     */
    @Override
    public Mono<Utils.ValueHolder<List<PartitionKeyRange>>> tryGetOverlappingRangesAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                                                         String collectionRid,
                                                                                         Range<String> range,
                                                                                         boolean forceRefresh,
                                                                                         Map<String, Object> properties,
                                                                                         StringBuilder sb) {

        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = tryLookupAsync(metaDataDiagnosticsContext, collectionRid, null, properties, sb);

        return routingMapObs.flatMap(routingMapValueHolder -> {
            if (forceRefresh && routingMapValueHolder.v != null) {
                logger.debug("tryGetOverlappingRangesAsync with forceRefresh on collectionRid {}", collectionRid);
                return tryLookupAsync(metaDataDiagnosticsContext, collectionRid, routingMapValueHolder.v, properties, sb);
            }

            return Mono.just(routingMapValueHolder);
        }).map(routingMapValueHolder -> {
            if (routingMapValueHolder.v != null) {
                // TODO: the routingMap.getOverlappingRanges(range) returns Collection
                // maybe we should consider changing to ArrayList to avoid conversion
                return new Utils.ValueHolder<>(new ArrayList<>(routingMapValueHolder.v.getOverlappingRanges(range)));
            } else {
                logger.warn("Routing Map Null for collection: {} for range: {}, forceRefresh:{}", collectionRid, range, forceRefresh);
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
                                                                                       Map<String, Object> properties,
                                                                                       StringBuilder sb) {

        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = tryLookupAsync(metaDataDiagnosticsContext, collectionResourceId, null, properties, sb);

        return routingMapObs.flatMap(routingMapValueHolder -> {
            if (forceRefresh && routingMapValueHolder.v != null) {
                return tryLookupAsync(metaDataDiagnosticsContext, collectionResourceId, routingMapValueHolder.v, properties, sb);
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
    public Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetRangeByPartitionKeyRangeId(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, String partitionKeyRangeId, Map<String, Object> properties, StringBuilder sb) {

        if (sb != null) {
            sb.append("RxPartitionKeyRangeCache.TryGetRangeByPartitionKeyRangeId:").append(",");
        }

        String callIdentifier = UUID.randomUUID().toString();

        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs = routingMapCache.getAsync(
            collectionRid,
            routingMap -> getRoutingMapForCollectionAsync(metaDataDiagnosticsContext, collectionRid, null, properties, sb, callIdentifier), forceRefresh -> false, callIdentifier).map(Utils.ValueHolder::new);

        return routingMapObs.map(routingMapValueHolder -> new Utils.ValueHolder<>(routingMapValueHolder.v.getRangeByPartitionKeyRangeId(partitionKeyRangeId)))
                .onErrorResume(err -> {
                    CosmosException dce = Utils.as(err, CosmosException.class);
                    logger.debug(
                            "tryGetRangeByPartitionKeyRangeId on collectionRid {} and partitionKeyRangeId {} encountered failure",
                            collectionRid,
                            partitionKeyRangeId,
                            err);

                    // bubble up in case a 404:1002 is seen to force retries as a part of document retries
                    // todo: revert change when fault injection excludes 404:1002 for master resources
                    if (dce != null && Exceptions.isNotFound(dce) && !Exceptions.isSubStatusCode(dce, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)) {
                        return Mono.just(new Utils.ValueHolder<>(null));
                    }

                    return dce != null ? Mono.error(dce) : Mono.error(err);
                });
    }

    public Mono<Utils.ValueHolder<CollectionRoutingMap>> refreshAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, StringBuilder sb) {
        return this.tryLookupAsync(
            metaDataDiagnosticsContext,
            collectionRid,
            null,
            null,
            sb
        ).flatMap(collectionRoutingMapValueHolder -> tryLookupAsync(metaDataDiagnosticsContext, collectionRid,
            collectionRoutingMapValueHolder.v, null, sb));
    }

    private Mono<CollectionRoutingMap> getRoutingMapForCollectionAsync(
        MetadataDiagnosticsContext metaDataDiagnosticsContext,
        String collectionRid,
        CollectionRoutingMap previousRoutingMap,
        Map<String, Object> properties,
        StringBuilder sb,
        String callIdentifier) {

        // TODO: NOTE: main java code doesn't do anything in regard to the previous routing map
        // .Net code instead of using DocumentClient controls sending request and receiving requests here

        // here we stick to what main java sdk does, investigate later.

        if (sb != null) {
            sb.append("RxPartitionKeyRangeCache.GetRoutingMapForCollectionAsync:").append(",");
        }

        Mono<List<PartitionKeyRange>> rangesObs = getPartitionKeyRange(metaDataDiagnosticsContext, collectionRid , false, properties, sb, callIdentifier);

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

    private Mono<List<PartitionKeyRange>> getPartitionKeyRange(
        MetadataDiagnosticsContext metaDataDiagnosticsContext,
        String collectionRid,
        boolean forceRefresh,
        Map<String, Object> properties,
        StringBuilder sb,
        String callIdentifier) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(this.clientContext,
            OperationType.ReadFeed,
            collectionRid,
            ResourceType.PartitionKeyRange,
            null
        ); //this request doesn't actually go to server

        if (sb != null) {
            sb.append("RxPartitionKeyRangeCache.GetPartitionKeyRange:").append(collectionRid).append(",");
        }

        request.requestContext.resolvedCollectionRid = collectionRid;
        request.setResourceId(collectionRid);
        Mono<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(metaDataDiagnosticsContext, request)
            .map(collectionValueHolder -> collectionValueHolder.v);

        return collectionObs.flatMap(coll -> {

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
            if (properties != null) {
                ModelBridgeInternal.setQueryRequestOptionsProperties(cosmosQueryRequestOptions, properties);
            }

            qryOptAccessor.setOperationId(cosmosQueryRequestOptions, callIdentifier);
            Instant addressCallStartTime = Instant.now();

            int prefetch = Configs.isPartitionKeyRangePrefetchingEnabled() ? Queues.XS_BUFFER_SIZE : 1;

            logger.warn("Reading PartitionKeyRanges for collection : [{}] with rid : [{}] by UserAgent : [{}] by CallPath : [{}] by CallIdentifier [{}] with Prefetch [{}]", coll.getId(), collectionRid, this.client.getUserAgent(), sb != null ? sb.toString() : "N/A", callIdentifier, prefetch);

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
                }, 1, prefetch).collectList();
        });
    }
}
