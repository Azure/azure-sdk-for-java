// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.InCompleteRoutingMapException;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class RxPartitionKeyRangeCacheTest {
    private RxDocumentClientImpl client;
    private RxCollectionCache collectionCache;
    private RxPartitionKeyRangeCache cache;

    @BeforeMethod(groups = "unit")
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

    @Test(groups = "unit")
    public void validateIsCompleteSetOfRanges_ValidRanges() {
        PartitionKeyRange range1 = new PartitionKeyRange();
        range1.setId("0");
        range1.setMinInclusive("");
        range1.setMaxExclusive("0000000030");

        PartitionKeyRange range2 = new PartitionKeyRange();
        range2.setId("1");
        range2.setMinInclusive("0000000030");
        range2.setMaxExclusive("0000000070");

        PartitionKeyRange range3 = new PartitionKeyRange();
        range3.setId("2");
        range3.setMinInclusive("0000000070");
        range3.setMaxExclusive("FF");

        CollectionRoutingMap routingMap = InMemoryCollectionRoutingMap
            .tryCreateCompleteRoutingMap(
                Arrays.asList(
                    new ImmutablePair<>(range1, null),
                    new ImmutablePair<>(range2, null),
                    new ImmutablePair<>(range3, null)
                ),
                "dummyCollectionId",
                null);

        // Verify that the routing map was created successfully
        // A non-null result indicates the ranges form a complete set
        assertNotNull(routingMap, "Routing map should be created for valid complete set of ranges");
    }

    @Test(groups = "unit")
    public void validateIsCompleteSetOfRanges_WithOverlap() {
        PartitionKeyRange range1 = new PartitionKeyRange();
        range1.setId("0");
        range1.setMinInclusive("");
        range1.setMaxExclusive("0000000050");

        PartitionKeyRange range2 = new PartitionKeyRange();
        range2.setId("1");
        range2.setMinInclusive("0000000030"); // Overlaps with range1
        range2.setMaxExclusive("FF");

        try {
            InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
                Arrays.asList(
                    new ImmutablePair<>(range1, null),
                    new ImmutablePair<>(range2, null)
                ),
                "dummyCollectionId",
                null);
            fail("Expected InCompleteRoutingMapException for overlapping ranges");
        } catch (InCompleteRoutingMapException e) {
            assertEquals(
                e.getMessage(),
                "Ranges overlap for collectionRid dummyCollectionId, previous range [{\"min\":\"\",\"max\":\"0000000050\"}], current range [{\"min\":\"0000000030\",\"max\":\"FF\"}]",
                "Unexpected error message");
        }
    }

    @Test(groups = "unit")
    public void validateIsCompleteSetOfRanges_EmptyList() {
        try {
            InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
                Collections.emptyList(),
                "dummyCollectionId",
                null);
            fail("Expected InCompleteRoutingMapException for empty ranges list");
        } catch (InCompleteRoutingMapException e) {
            assertEquals(e.getMessage(), "Empty ranges for collectionRid dummyCollectionId", "Unexpected error message");
        }
    }

    @Test(groups = "unit")
    public void validateIsCompleteSetOfRanges_WithGap() {
        PartitionKeyRange range1 = new PartitionKeyRange();
        range1.setId("0");
        range1.setMinInclusive("");
        range1.setMaxExclusive("0000000020");

        PartitionKeyRange range2 = new PartitionKeyRange();
        range2.setId("1");
        range2.setMinInclusive("0000000030"); // Overlaps with range1
        range2.setMaxExclusive("FF");

        try {
            InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
                Arrays.asList(
                    new ImmutablePair<>(range1, null),
                    new ImmutablePair<>(range2, null)
                ),
                "dummyCollectionId",
                null);
            fail("Expected InCompleteRoutingMapException for overlapping ranges");
        } catch (InCompleteRoutingMapException e) {
            assertEquals(
                e.getMessage(),
                "Ranges incomplete for collectionRid dummyCollectionId, previous range [{\"min\":\"\",\"max\":\"0000000020\"}], current range [{\"min\":\"0000000030\",\"max\":\"FF\"}]",
                "Unexpected error message");
        }
    }

    @Test(groups = "unit")
    public void tryLookupAsync_RetriesOnceAndConvertsToNotFoundException() {
        String collectionRid = "collection1";

        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        // First attempt - returns incomplete routing map
        FeedResponse<PartitionKeyRange> response1 = Mockito.mock(FeedResponse.class);
        when(response1.getResults()).thenReturn(Collections.emptyList());

        // Second attempt - still returns incomplete routing map
        FeedResponse<PartitionKeyRange> response2 = Mockito.mock(FeedResponse.class);
        when(response2.getResults()).thenReturn(Collections.emptyList());

        when(collectionCache.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));

        when(client.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenReturn(Flux.just(response1))
            .thenReturn(Flux.just(response2));

        StepVerifier.create(cache.tryLookupAsync(null, collection.getResourceId(), null, new HashMap<>()))
            .expectNextMatches(s -> s.v == null)
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void twoCachesForSameEndpointShareRoutingMapStorage() throws Exception {
        // Two RxPartitionKeyRangeCache instances pointing at the same endpoint should
        // share their underlying AsyncCacheNonBlocking, so a routing map populated by
        // one is immediately visible to the other without a second /pkranges call.
        URI endpoint = new URI("https://test-shared-pkr-1.documents.azure.com:443/");

        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "shared-coll-1";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range));
        when(response.getContinuationToken()).thenReturn("etag-1");

        AtomicInteger clientACalls = new AtomicInteger();
        AtomicInteger clientBCalls = new AtomicInteger();

        when(collA.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(clientA.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientACalls.incrementAndGet();
                return Flux.just(response);
            });

        when(collB.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(clientB.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientBCalls.incrementAndGet();
                return Flux.just(response);
            });

        RxPartitionKeyRangeCache cacheA = new RxPartitionKeyRangeCache(clientA, collA, endpoint);
        RxPartitionKeyRangeCache cacheB = new RxPartitionKeyRangeCache(clientB, collB, endpoint);

        try {
            // First client populates the cache.
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                .expectNextMatches(v -> v != null && v.v != null)
                .verifyComplete();

            // Second client must hit the shared cache without issuing its own /pkranges read.
            StepVerifier.create(cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                .expectNextMatches(v -> v != null && v.v != null)
                .verifyComplete();

            assertThat(clientACalls.get())
                .as("client A populates the shared routing map")
                .isEqualTo(1);
            assertThat(clientBCalls.get())
                .as("client B must hit the shared cache without issuing its own /pkranges call")
                .isZero();
        } finally {
            cacheA.close();
            cacheB.close();
        }

        assertThat(SharedRoutingMapCacheRegistry.getInstance().referenceCount(endpoint.toString()))
            .as("close() releases the shared cache reference")
            .isZero();
    }

    @Test(groups = "unit")
    public void cachesForDifferentEndpointsDoNotShareStorage() throws Exception {
        URI endpointA = new URI("https://test-shared-pkr-2a.documents.azure.com:443/");
        URI endpointB = new URI("https://test-shared-pkr-2b.documents.azure.com:443/");

        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "shared-coll-2";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll2");

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range));
        when(response.getContinuationToken()).thenReturn("etag-2");

        AtomicInteger clientACalls = new AtomicInteger();
        AtomicInteger clientBCalls = new AtomicInteger();

        when(collA.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(clientA.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientACalls.incrementAndGet();
                return Flux.just(response);
            });
        when(collB.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(clientB.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientBCalls.incrementAndGet();
                return Flux.just(response);
            });

        RxPartitionKeyRangeCache cacheA = new RxPartitionKeyRangeCache(clientA, collA, endpointA);
        RxPartitionKeyRangeCache cacheB = new RxPartitionKeyRangeCache(clientB, collB, endpointB);

        try {
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                .expectNextMatches(v -> v != null && v.v != null)
                .verifyComplete();

            StepVerifier.create(cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                .expectNextMatches(v -> v != null && v.v != null)
                .verifyComplete();

            // Two different endpoints → no sharing, each client issues its own read.
            assertThat(clientACalls.get()).isEqualTo(1);
            assertThat(clientBCalls.get()).isEqualTo(1);
        } finally {
            cacheA.close();
            cacheB.close();
        }
    }

    @Test(groups = "unit")
    public void closeIsIdempotent() throws Exception {
        URI endpoint = new URI("https://test-shared-pkr-3.documents.azure.com:443/");
        RxDocumentClientImpl mockClient = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache mockColl = Mockito.mock(RxCollectionCache.class);

        RxPartitionKeyRangeCache c = new RxPartitionKeyRangeCache(mockClient, mockColl, endpoint);
        assertThat(SharedRoutingMapCacheRegistry.getInstance().referenceCount(endpoint.toString()))
            .isEqualTo(1);

        c.close();
        c.close(); // second call must be a no-op
        c.close();

        assertThat(SharedRoutingMapCacheRegistry.getInstance().referenceCount(endpoint.toString()))
            .as("repeated close() must not drive refcount negative")
            .isZero();
    }
}