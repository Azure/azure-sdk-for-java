// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class RxPartitionKeyRangeCacheTest {
    private RxDocumentClientImpl client;
    private RxCollectionCache collectionCache;
    private RxPartitionKeyRangeCache cache;
    
    @BeforeClass(groups = "unit")
    public void before_test() {
        client = Mockito.mock(RxDocumentClientImpl.class);
        collectionCache = Mockito.mock(RxCollectionCache.class);
        cache = new RxPartitionKeyRangeCache(client, collectionCache);
    }

    @Test(groups = "unit")
    public void getRoutingMapUsesChangeFeedNextIfNoneMatchWhenNotEmpty() {
        String collectionRid = "collection1";
        String changeFeedToken = "token1";
        
        PartitionKeyRange range1 = new PartitionKeyRange();
        range1.setId("0");
        range1.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range1.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);
        
        CollectionRoutingMap previousRoutingMap = InMemoryCollectionRoutingMap
            .tryCreateCompleteRoutingMap(Arrays.asList(ImmutablePair.of(range1, null)), collectionRid, changeFeedToken);

        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range1));
        when(response.getContinuationToken()).thenReturn("newToken");

        when(collectionCache.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        
        when(client.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenReturn(Flux.just(response));

        StepVerifier.create(cache.tryLookupAsync(null, collectionRid, previousRoutingMap, new HashMap<>()))
            .expectNextMatches(routingMapHolder -> 
                routingMapHolder != null && 
                routingMapHolder.v != null &&
                changeFeedToken.equals(previousRoutingMap.getChangeFeedNextIfNoneMatch()))
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void getRoutingMapWithEmptyChangeFeedNextIfNoneMatch() {
        String collectionRid = "collection1";
        
        PartitionKeyRange range1 = new PartitionKeyRange();
        range1.setId("0");
        range1.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range1.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);
        
        CollectionRoutingMap previousRoutingMap = InMemoryCollectionRoutingMap
            .tryCreateCompleteRoutingMap(
                Arrays.asList(ImmutablePair.of(range1, null)),
                collectionRid,
                null);

        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range1));
        when(response.getContinuationToken()).thenReturn("newToken");

        when(collectionCache.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        
        when(client.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenReturn(Flux.just(response));

        StepVerifier.create(cache.tryLookupAsync(null, collectionRid, previousRoutingMap, new HashMap<>()))
            .expectNextMatches(routingMapHolder ->
                routingMapHolder != null &&
                routingMapHolder.v != null &&
                previousRoutingMap.getChangeFeedNextIfNoneMatch() == null)
            .verifyComplete();
    }
}