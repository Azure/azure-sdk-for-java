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
import java.util.List;
import java.util.stream.Collectors;

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
import com.microsoft.azure.cosmosdb.rx.internal.Exceptions;
import com.microsoft.azure.cosmosdb.rx.internal.ICollectionRoutingMapCache;
import com.microsoft.azure.cosmosdb.rx.internal.IRoutingMapProvider;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientImpl;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 **/
public class RxPartitionKeyRangeCache implements IRoutingMapProvider, ICollectionRoutingMapCache {

    private final Logger logger = LoggerFactory.getLogger(RxPartitionKeyRangeCache.class);

    private final AsyncCache<String, CollectionRoutingMap> routingMapCache;
    private final RxDocumentClientImpl client;

    private final RxCollectionCache collectionCache;

    public RxPartitionKeyRangeCache(RxDocumentClientImpl client, RxCollectionCache collectionCache) {
        this.routingMapCache = new AsyncCache<>();
        this.client = client;
        this.collectionCache = collectionCache;
    }

    @Override
    public Single<CollectionRoutingMap> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue) {
        return routingMapCache.getAsync(
                collectionRid,
                previousValue,
                () -> getRoutingMapForCollectionAsync(collectionRid, previousValue))
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
    public Single<List<PartitionKeyRange>> tryGetOverlappingRangesAsync(String collectionRid,
            Range<String> range, boolean forceRefresh) {

        Single<CollectionRoutingMap> routingMapObs = tryLookupAsync(collectionRid, null);

        return routingMapObs.flatMap(routingMap -> {
            if (forceRefresh && routingMap != null) {
                logger.debug("tryGetOverlappingRangesAsync with forceRefresh on collectionRid {}", collectionRid);
                return tryLookupAsync(collectionRid, routingMap);
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

    @Override
    public Single<PartitionKeyRange> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId,
            String partitionKeyRangeId, boolean forceRefresh) {

        Single<CollectionRoutingMap> routingMapObs = tryLookupAsync(collectionResourceId, null);

        return routingMapObs.flatMap(routingMap -> {
            if (forceRefresh && routingMap != null) {
                return tryLookupAsync(collectionResourceId, routingMap);
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

    public Single<PartitionKeyRange> tryGetRangeByPartitionKeyRangeId(String collectionRid, String partitionKeyRangeId) {
        Single<CollectionRoutingMap> routingMapObs = routingMapCache.getAsync(
                collectionRid,
                null,
                () -> getRoutingMapForCollectionAsync(collectionRid, null));

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
            CollectionRoutingMap previousRoutingMap) {

        // TODO: NOTE: main java code doesn't do anything in regard to the previous routing map
        // .Net code instead of using DocumentClient controls sending request and receiving requests here

        // here we stick to what main java sdk does, investigate later.

        Single<List<PartitionKeyRange>> rangesObs = getPartitionKeyRange(collectionRid, false);

        return rangesObs.flatMap(ranges -> {

            List<ImmutablePair<PartitionKeyRange, Boolean>> rangesTuples =
                    ranges.stream().map(range -> new  ImmutablePair<>(range, true)).collect(Collectors.toList());

            // TODO: this may return null if the provided ranges is not complete
            // is this better to return null in this case?
            InMemoryCollectionRoutingMap<Boolean> collectionRoutingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(rangesTuples,
                    StringUtils.EMPTY);
            return Single.just(collectionRoutingMap);
        });
    }

    private Single<List<PartitionKeyRange>> getPartitionKeyRange(String collectionRid, boolean forceRefresh) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.ReadFeed,
                collectionRid,
                ResourceType.PartitionKeyRange,
                null
                // TODOAuthorizationTokenType.Invalid)
                ); //this request doesn't actually go to server

        request.setResolvedCollectionRid(collectionRid);
        Single<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(request);

        return collectionObs.flatMap(coll -> {

            Observable<List<PartitionKeyRange>> rs = client.readPartitionKeyRanges(coll.getSelfLink(), null)
                    // maxConcurrent = 1 to makes it in the right order
                    .flatMap(p -> Observable.from(p.getResults()), 1).toList();
            return rs.toSingle();
        });
    }
}

