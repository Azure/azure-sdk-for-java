/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal.caches;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.internal.routing.IServerIdentity;
import com.microsoft.azure.cosmosdb.rx.internal.NotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.routing.CollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.InMemoryCollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.internal.Exceptions;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.Single;

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
     * @see com.microsoft.azure.cosmosdb.rx.internal.caches.IPartitionKeyRangeCache#tryLookupAsync(java.lang.String, com.microsoft.azure.cosmosdb.internal.routing.CollectionRoutingMap)
     */
    @Override
    public Single<CollectionRoutingMap> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue, Map<String, Object> properties) {
        return routingMapCache.getAsync(
                collectionRid,
                previousValue,
                () -> getRoutingMapForCollectionAsync(collectionRid, previousValue, properties))
                .onErrorResumeNext(err -> {
                    logger.debug("tryLookupAsync on collectionRid {} encountered failure", collectionRid, err);
                    DocumentClientException dce = Utils.as(err, DocumentClientException.class);
                    if (dce != null && Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND)) {
                        return Single.just(null);
                    }

                    return Single.error(err);
                });
    }

    @Override
    public Single<CollectionRoutingMap> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue, boolean forceRefreshCollectionRoutingMap,
            Map<String, Object> properties) {
        return tryLookupAsync(collectionRid, previousValue, properties);
    }

    /* (non-Javadoc)
     * @see com.microsoft.azure.cosmosdb.rx.internal.caches.IPartitionKeyRangeCache#tryGetOverlappingRangesAsync(java.lang.String, com.microsoft.azure.cosmosdb.internal.routing.Range, boolean)
     */
    @Override
    public Single<List<PartitionKeyRange>> tryGetOverlappingRangesAsync(String collectionRid, Range<String> range, boolean forceRefresh,
            Map<String, Object> properties) {

        Single<CollectionRoutingMap> routingMapObs = tryLookupAsync(collectionRid, null, properties);

        return routingMapObs.flatMap(routingMap -> {
            if (forceRefresh && routingMap != null) {
                logger.debug("tryGetOverlappingRangesAsync with forceRefresh on collectionRid {}", collectionRid);
                return tryLookupAsync(collectionRid, routingMap, properties);
            }

            return Single.just(routingMap);

        }).map(routingMap -> {
            if (routingMap != null) {
                // TODO: the routingMap.getOverlappingRanges(range) returns Collection
                // maybe we should consider changing to ArrayList to avoid conversion
                return new ArrayList<>(routingMap.getOverlappingRanges(range));
            } else {
                logger.debug("Routing Map Null for collection: {} for range: {}, forceRefresh:{}", collectionRid, range.toString(), forceRefresh);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see com.microsoft.azure.cosmosdb.rx.internal.caches.IPartitionKeyRangeCache#tryGetPartitionKeyRangeByIdAsync(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public Single<PartitionKeyRange> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId, String partitionKeyRangeId,
            boolean forceRefresh, Map<String, Object> properties) {

        Single<CollectionRoutingMap> routingMapObs = tryLookupAsync(collectionResourceId, null, properties);

        return routingMapObs.flatMap(routingMap -> {
            if (forceRefresh && routingMap != null) {
                return tryLookupAsync(collectionResourceId, routingMap, properties);
            }

            return Single.just(routingMap);

        }).map(routingMap -> {
            if (routingMap != null) {
                return routingMap.getRangeByPartitionKeyRangeId(partitionKeyRangeId);
            } else {
                logger.debug("Routing Map Null for collection: {}, PartitionKeyRangeId: {}, forceRefresh:{}", collectionResourceId, partitionKeyRangeId, forceRefresh);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see com.microsoft.azure.cosmosdb.rx.internal.caches.IPartitionKeyRangeCache#tryGetRangeByPartitionKeyRangeId(java.lang.String, java.lang.String)
     */
    @Override
    public Single<PartitionKeyRange> tryGetRangeByPartitionKeyRangeId(String collectionRid, String partitionKeyRangeId, Map<String, Object> properties) {
        Single<CollectionRoutingMap> routingMapObs = routingMapCache.getAsync(
                collectionRid,
                null,
                () -> getRoutingMapForCollectionAsync(collectionRid, null, properties));

        return routingMapObs.map(routingMap -> routingMap.getRangeByPartitionKeyRangeId(partitionKeyRangeId))
                .onErrorResumeNext(err -> {
                    DocumentClientException dce = Utils.as(err, DocumentClientException.class);
                    logger.debug("tryGetRangeByPartitionKeyRangeId on collectionRid {} and partitionKeyRangeId {} encountered failure",
                            collectionRid, partitionKeyRangeId, err);

                    if (dce != null && Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND)) {
                        return Single.just(null);
                    }

                    return Single.error(dce);
                });
    }

    private Single<CollectionRoutingMap> getRoutingMapForCollectionAsync(
            String collectionRid,
            CollectionRoutingMap previousRoutingMap,
            Map<String, Object> properties) {

        // TODO: NOTE: main java code doesn't do anything in regard to the previous routing map
        // .Net code instead of using DocumentClient controls sending request and receiving requests here

        // here we stick to what main java sdk does, investigate later.

        Single<List<PartitionKeyRange>> rangesObs = getPartitionKeyRange(collectionRid, false, properties);

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
                // Range information either doesn't exist or is not complete.
                return Single.error(new NotFoundException(String.format("GetRoutingMapForCollectionAsync(collectionRid: {%s}), Range information either doesn't exist or is not complete.", collectionRid)));
            }

            return Single.just(routingMap);
        });
    }

    private Single<List<PartitionKeyRange>> getPartitionKeyRange(String collectionRid, boolean forceRefresh, Map<String, Object> properties) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.ReadFeed,
                collectionRid,
                ResourceType.PartitionKeyRange,
                null
                ); //this request doesn't actually go to server

        request.requestContext.resolvedCollectionRid = collectionRid;
        Single<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(request);

        return collectionObs.flatMap(coll -> {

            FeedOptions feedOptions = new FeedOptions();
            if (properties != null) {
                feedOptions.setProperties(properties);
            }
            Observable<List<PartitionKeyRange>> rs = client.readPartitionKeyRanges(coll.getSelfLink(), feedOptions)
                    // maxConcurrent = 1 to makes it in the right order
                    .flatMap(p -> Observable.from(p.getResults()), 1).toList();
            return rs.toSingle();
        });
    }
}

