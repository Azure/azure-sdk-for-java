// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.InCompleteRoutingMapException;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext.MetadataDiagnostics;
import com.azure.cosmos.implementation.NotFoundException;
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
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RxPartitionKeyRangeCacheTest {
    private RxDocumentClientImpl client;
    private RxCollectionCache collectionCache;
    private RxPartitionKeyRangeCache cache;

    @BeforeMethod(groups = "unit")
    public void before_test() {
        client = Mockito.mock(RxDocumentClientImpl.class);
        collectionCache = Mockito.mock(RxCollectionCache.class);
        // Pass null endpoint explicitly to opt into an isolated cache for the
        // original test-suite behavior (these tests don't exercise sharing).
        cache = new RxPartitionKeyRangeCache(client, collectionCache, (URI) null);
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
    public void tryLookupAsync_RetriesPartitionKeyRangeNotFound() {
        String collectionRid = "collection1";

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Collections.singletonList(range));
        when(response.getContinuationToken()).thenReturn(null);

        when(collectionCache.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));

        when(client.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenReturn(Flux.error(new NotFoundException("Owner resource does not exist")))
            .thenReturn(Flux.just(response));

        StepVerifier.create(cache.tryLookupAsync(null, collection.getResourceId(), null, new HashMap<>()))
            .expectNextMatches(routingMapHolder -> routingMapHolder != null && routingMapHolder.v != null)
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void tryLookupAsync_RetainsDiagnosticsForPartitionKeyRangeNotFoundRetry() {
        String collectionRid = "collection1";

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Collections.singletonList(range));
        when(response.getContinuationToken()).thenReturn(null);

        when(collectionCache.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));

        AtomicInteger readPkRangesAttempts = new AtomicInteger();
        when(client.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                if (readPkRangesAttempts.incrementAndGet() == 1) {
                    return Flux.error(new NotFoundException("Owner resource does not exist"));
                }

                return Flux.just(response);
            });

        MetadataDiagnosticsContext metadataDiagnosticsContext = new MetadataDiagnosticsContext();

        StepVerifier.create(cache.tryLookupAsync(metadataDiagnosticsContext, collection.getResourceId(), null, new HashMap<>()))
            .expectNextMatches(routingMapHolder -> routingMapHolder != null && routingMapHolder.v != null)
            .verifyComplete();

        assertEquals(readPkRangesAttempts.get(), 2);
        assertNotNull(metadataDiagnosticsContext.metadataDiagnosticList);
        assertTrue(metadataDiagnosticsContext.metadataDiagnosticList
            .stream()
            .anyMatch(metadataDiagnostics ->
                metadataDiagnostics.metaDataName == MetadataDiagnosticsContext.MetadataType.PARTITION_KEY_RANGE_LOOK_UP));
    }

    @Test(groups = "unit")
    public void twoCachesForSameEndpointShareRoutingMapStorage() throws Exception {
        URI endpoint = URI.create("https://test-shared-pkr-1.documents.azure.com:443/");

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
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .expectNextMatches(v -> v != null && v.v != null)
                        .verifyComplete();

            StepVerifier.create(cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .expectNextMatches(v -> v != null && v.v != null)
                        .verifyComplete();

            assertThat(clientACalls.get()).isEqualTo(1);
            assertThat(clientBCalls.get()).isZero();
        } finally {
            cacheA.close();
            cacheB.close();
        }

        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .as("close() releases the shared cache reference")
            .isZero();
    }

    @Test(groups = "unit")
    public void cachesForDifferentEndpointsDoNotShareStorage() throws Exception {
        URI endpointA = URI.create("https://test-shared-pkr-2a.documents.azure.com:443/");
        URI endpointB = URI.create("https://test-shared-pkr-2b.documents.azure.com:443/");

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

            assertThat(clientACalls.get()).isEqualTo(1);
            assertThat(clientBCalls.get()).isEqualTo(1);
        } finally {
            cacheA.close();
            cacheB.close();
        }
    }

    @Test(groups = "unit")
    public void closeIsIdempotent() throws Exception {
        URI endpoint = URI.create("https://test-shared-pkr-3.documents.azure.com:443/");
        RxDocumentClientImpl mockClient = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache mockColl = Mockito.mock(RxCollectionCache.class);

        RxPartitionKeyRangeCache c = new RxPartitionKeyRangeCache(mockClient, mockColl, endpoint);
        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .isEqualTo(1);

        c.close();
        c.close(); // second call must be a no-op
        c.close();

        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .as("repeated close() must not drive refcount negative")
            .isZero();
    }

    @Test(groups = "unit")
    public void clientWithServiceEndpointAcquiresAndReleasesRegistryRefcount() throws Exception {
        // Regression-guard for the RxDocumentClientImpl.close() -> partitionKeyRangeCache.close()
        // wiring: constructing the cache must bump the registry refcount; close() must drop it.
        URI endpoint = URI.create("https://test-pkr-lifecycle.documents.azure.com:443/");
        RxDocumentClientImpl mockClient = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache mockColl = Mockito.mock(RxCollectionCache.class);

        int before = SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint);

        // Mirrors what RxDocumentClientImpl actually uses: 3-arg ctor with the account id.
        RxPartitionKeyRangeCache c = new RxPartitionKeyRangeCache(mockClient, mockColl, endpoint);
        try {
            assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
                .isEqualTo(before + 1);
        } finally {
            c.close();
        }
        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .isEqualTo(before);
    }

    @Test(groups = "unit")
    public void forceRefreshOnSharedCacheIsVisibleToSiblingClient() throws Exception {
        // Cross-client invalidation propagation: client A force-refreshes a routing map,
        // the new value must be visible to client B's next lookup.
        URI endpoint = URI.create("https://test-shared-pkr-refresh.documents.azure.com:443/");

        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "refresh-coll-1";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        PartitionKeyRange rangeBefore = new PartitionKeyRange();
        rangeBefore.setId("0");
        rangeBefore.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        rangeBefore.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        // After refresh: a split scenario produces two child ranges with the original as parent.
        PartitionKeyRange rangeAfter1 = new PartitionKeyRange();
        rangeAfter1.setId("1");
        rangeAfter1.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        rangeAfter1.setMaxExclusive("80");
        rangeAfter1.setParents(Arrays.asList("0"));
        PartitionKeyRange rangeAfter2 = new PartitionKeyRange();
        rangeAfter2.setId("2");
        rangeAfter2.setMinInclusive("80");
        rangeAfter2.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);
        rangeAfter2.setParents(Arrays.asList("0"));

        FeedResponse<PartitionKeyRange> responseBefore = Mockito.mock(FeedResponse.class);
        when(responseBefore.getResults()).thenReturn(Arrays.asList(rangeBefore));
        when(responseBefore.getContinuationToken()).thenReturn("etag-before");

        FeedResponse<PartitionKeyRange> responseAfter = Mockito.mock(FeedResponse.class);
        when(responseAfter.getResults()).thenReturn(Arrays.asList(rangeAfter1, rangeAfter2));
        when(responseAfter.getContinuationToken()).thenReturn("etag-after");

        when(collA.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(collB.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));

        // Client A first returns the pre-split layout, then the post-split layout on refresh.
        AtomicInteger clientACalls = new AtomicInteger();
        when(clientA.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                int n = clientACalls.incrementAndGet();
                return n == 1 ? Flux.just(responseBefore) : Flux.just(responseAfter);
            });
        AtomicInteger clientBCalls = new AtomicInteger();
        when(clientB.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientBCalls.incrementAndGet();
                return Flux.just(responseAfter);
            });

        RxPartitionKeyRangeCache cacheA = new RxPartitionKeyRangeCache(clientA, collA, endpoint);
        RxPartitionKeyRangeCache cacheB = new RxPartitionKeyRangeCache(clientB, collB, endpoint);

        try {
            // Step 1: A populates the shared cache with the pre-split routing map.
            CollectionRoutingMap[] beforeMapHolder = new CollectionRoutingMap[1];
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .consumeNextWith(v -> beforeMapHolder[0] = v.v)
                        .verifyComplete();
            assertThat(beforeMapHolder[0]).isNotNull();

            // Step 2: A force-refreshes (passing previousValue == current cached map).
            CollectionRoutingMap[] afterMapHolder = new CollectionRoutingMap[1];
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, beforeMapHolder[0], new HashMap<>()))
                        .consumeNextWith(v -> afterMapHolder[0] = v.v)
                        .verifyComplete();
            assertThat(afterMapHolder[0]).isNotSameAs(beforeMapHolder[0]);

            // Step 3: B's lookup must see A's refreshed value (no fresh fetch from B).
            StepVerifier.create(cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .consumeNextWith(v -> assertThat(v.v).isSameAs(afterMapHolder[0]))
                        .verifyComplete();

            assertThat(clientACalls.get())
                .as("A populated then refreshed -> 2 calls")
                .isEqualTo(2);
            assertThat(clientBCalls.get())
                .as("B must observe A's refresh without issuing its own fetch")
                .isZero();
        } finally {
            cacheA.close();
            cacheB.close();
        }
    }

    @Test(groups = "unit")
    public void concurrentLookupsOnSharedCacheIssueSingleFetch() throws Exception {
        // Single-flight across clients: two clients sharing the same cache look up the same,
        // previously-unseen collectionRid concurrently. AsyncCacheNonBlocking must collapse the
        // fan-out so that exactly ONE /pkranges fetch happens across both clients - this is the
        // core value of the PR (collapsing N concurrent fetches into 1), not just sequential reuse.
        URI endpoint = URI.create("https://test-shared-pkr-singleflight.documents.azure.com:443/");

        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "shared-coll-singleflight";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range));
        when(response.getContinuationToken()).thenReturn("etag-sf");

        AtomicInteger clientACalls = new AtomicInteger();
        AtomicInteger clientBCalls = new AtomicInteger();

        when(collA.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(collB.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));

        // A small delay on the fetch widens the window in which both lookups are in-flight, so the
        // concurrent (rather than sequential) single-flight path is genuinely exercised. Whichever
        // client wins putIfAbsent runs the only fetch; the other attaches to the shared in-flight
        // result, so the total fetch count is exactly one regardless of interleaving.
        when(clientA.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientACalls.incrementAndGet();
                return Flux.just(response).delayElements(Duration.ofMillis(150));
            });
        when(clientB.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientBCalls.incrementAndGet();
                return Flux.just(response).delayElements(Duration.ofMillis(150));
            });

        RxPartitionKeyRangeCache cacheA = new RxPartitionKeyRangeCache(clientA, collA, endpoint);
        RxPartitionKeyRangeCache cacheB = new RxPartitionKeyRangeCache(clientB, collB, endpoint);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        try {
            Future<CollectionRoutingMap> futureA = pool.submit(() -> {
                startLatch.await();
                return cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()).block().v;
            });
            Future<CollectionRoutingMap> futureB = pool.submit(() -> {
                startLatch.await();
                return cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()).block().v;
            });

            startLatch.countDown();

            CollectionRoutingMap mapA = futureA.get(30, TimeUnit.SECONDS);
            CollectionRoutingMap mapB = futureB.get(30, TimeUnit.SECONDS);

            assertThat(mapA).isNotNull();
            assertThat(mapB).isNotNull();
            assertThat(mapA)
                .as("both clients resolve to the same shared routing map instance")
                .isSameAs(mapB);
            assertThat(clientACalls.get() + clientBCalls.get())
                .as("single-flight across clients: exactly one /pkranges fetch total")
                .isEqualTo(1);
        } finally {
            pool.shutdownNow();
            cacheA.close();
            cacheB.close();
        }

        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .as("close() releases the shared cache reference")
            .isZero();
    }

    @Test(groups = "unit")
    public void failedFetchDoesNotPoisonSharedCacheForSiblings() throws Exception {
        // Failure isolation during shared population: if client A's /pkranges fetch fails, the
        // shared entry must NOT be cached as a failure. A sibling client B must still be able to
        // populate it, and A's own subsequent lookup must succeed. A cached failure would take
        // down every sibling on the endpoint.
        URI endpoint = URI.create("https://test-shared-pkr-failiso.documents.azure.com:443/");

        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "shared-coll-failiso";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range));
        when(response.getContinuationToken()).thenReturn("etag-failiso");

        when(collA.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(collB.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));

        AtomicInteger clientACalls = new AtomicInteger();
        AtomicInteger clientBCalls = new AtomicInteger();

        // A's fetch always fails with a non-retryable, non-404 error (so it is neither retried by
        // InCompleteRoutingMapRetryPolicy nor swallowed by tryLookupAsync's 404 handling).
        when(clientA.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientACalls.incrementAndGet();
                return Flux.error(new RuntimeException("transient pkranges failure"));
            });
        when(clientB.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenAnswer(invocation -> {
                clientBCalls.incrementAndGet();
                return Flux.just(response);
            });

        RxPartitionKeyRangeCache cacheA = new RxPartitionKeyRangeCache(clientA, collA, endpoint);
        RxPartitionKeyRangeCache cacheB = new RxPartitionKeyRangeCache(clientB, collB, endpoint);

        try {
            // A's fetch fails and the failure must propagate (not be cached as the shared value).
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .verifyError();
            assertThat(clientACalls.get()).isEqualTo(1);

            // The shared entry was not poisoned: sibling B can still populate it via a fresh fetch.
            StepVerifier.create(cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .expectNextMatches(v -> v != null && v.v != null)
                        .verifyComplete();
            assertThat(clientBCalls.get())
                .as("B populates the shared cache despite A's earlier failure")
                .isEqualTo(1);

            // A's subsequent lookup now succeeds - served from the entry B populated - even though
            // A's own fetch is still broken. This proves the failure was not shared/cached.
            StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                        .expectNextMatches(v -> v != null && v.v != null)
                        .verifyComplete();
            assertThat(clientACalls.get())
                .as("A recovers via the healed shared cache without another failing fetch")
                .isEqualTo(1);
        } finally {
            cacheA.close();
            cacheB.close();
        }

        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .isZero();
    }

    @Test(groups = "unit")
    public void diagnosticsRecordedOnlyByFetchingClientNotSibling() {
        // Positive assertion of the new diagnostics semantics: the client that actually issues the
        // /pkranges fetch records a PARTITION_KEY_RANGE_LOOK_UP metadata diagnostic; a sibling that
        // is served from the already-warm shared cache records none. (Previously this behavior was
        // only "covered" by an assertion removal in CosmosDiagnosticsTest.)
        URI endpoint = URI.create("https://test-shared-pkr-diag.documents.azure.com:443/");

        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "shared-coll-diag";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range));
        when(response.getContinuationToken()).thenReturn("etag-diag");

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
            // A performs the real fetch -> records the diagnostic in its own context.
            MetadataDiagnosticsContext metaA = new MetadataDiagnosticsContext();
            StepVerifier.create(cacheA.tryLookupAsync(metaA, collectionRid, null, new HashMap<>()))
                        .expectNextMatches(v -> v != null && v.v != null)
                        .verifyComplete();

            // B is served from the shared cache -> no fetch, no diagnostic in its context.
            MetadataDiagnosticsContext metaB = new MetadataDiagnosticsContext();
            StepVerifier.create(cacheB.tryLookupAsync(metaB, collectionRid, null, new HashMap<>()))
                        .expectNextMatches(v -> v != null && v.v != null)
                        .verifyComplete();

            assertThat(clientACalls.get()).isEqualTo(1);
            assertThat(clientBCalls.get()).isZero();

            int fetchingClientLookups = 0;
            if (metaA.metadataDiagnosticList != null) {
                for (MetadataDiagnostics d : metaA.metadataDiagnosticList) {
                    if (d.metaDataName == MetadataDiagnosticsContext.MetadataType.PARTITION_KEY_RANGE_LOOK_UP) {
                        fetchingClientLookups++;
                    }
                }
            }
            assertThat(fetchingClientLookups)
                .as("the fetching client records exactly one PARTITION_KEY_RANGE_LOOK_UP diagnostic")
                .isEqualTo(1);

            assertThat(metaB.isEmpty())
                .as("a sibling served from the shared cache records no metadata diagnostic")
                .isTrue();
        } finally {
            cacheA.close();
            cacheB.close();
        }

        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .isZero();
    }

    @Test(groups = "unit")
    public void sharingDisabledYieldsIsolatedCachesPerClient() throws Exception {
        // Opt-out exercised end-to-end through RxPartitionKeyRangeCache (not just the registry):
        // with COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED=false, two clients on the SAME
        // endpoint get isolated caches and each issues its own /pkranges fetch, restoring the
        // pre-sharing behavior. Mirrors twoCachesForSameEndpointShareRoutingMapStorage with the
        // kill-switch on.
        String flag = "COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED";
        String savedFlag = System.getProperty(flag);
        System.setProperty(flag, "false");
        URI endpoint = URI.create("https://test-shared-pkr-optout.documents.azure.com:443/");
        try {
            RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
            RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
            RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);
            RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);

            String collectionRid = "isolated-coll-optout";
            DocumentCollection collection = new DocumentCollection();
            collection.setResourceId(collectionRid);
            collection.setSelfLink("dbs/db1/colls/coll1");

            PartitionKeyRange range = new PartitionKeyRange();
            range.setId("0");
            range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
            range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

            FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
            when(response.getResults()).thenReturn(Arrays.asList(range));
            when(response.getContinuationToken()).thenReturn("etag-optout");

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
                // With sharing disabled the process-wide registry must not be touched at all.
                assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
                    .as("disabled flag must keep the shared registry untouched")
                    .isZero();

                StepVerifier.create(cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                            .expectNextMatches(v -> v != null && v.v != null)
                            .verifyComplete();
                StepVerifier.create(cacheB.tryLookupAsync(null, collectionRid, null, new HashMap<>()))
                            .expectNextMatches(v -> v != null && v.v != null)
                            .verifyComplete();

                assertThat(clientACalls.get()).isEqualTo(1);
                assertThat(clientBCalls.get())
                    .as("with sharing disabled each client issues its own /pkranges fetch")
                    .isEqualTo(1);
                assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
                    .as("disabled flag never registers the endpoint")
                    .isZero();
            } finally {
                cacheA.close();
                cacheB.close();
            }
        } finally {
            if (savedFlag == null) {
                System.clearProperty(flag);
            } else {
                System.setProperty(flag, savedFlag);
            }
        }
    }

    @Test(groups = "unit")
    public void initiatingClientReleasedAfterCloseWhileSiblingKeepsSharedEntryAlive() throws Exception {
        // Regression guard for the shared-cache owner-retention leak: the client that first populates
        // a collectionRid must become GC-eligible after close(), even while a sibling client keeps the
        // shared registry entry (and its AsyncCacheNonBlocking) alive. The initial routing-map load
        // must not pin the initiating client's object graph (RxDocumentClientImpl, collection cache,
        // diagnostics) via the cached Mono's upstream source chain.
        URI endpoint = URI.create("https://test-shared-pkr-owner-release.documents.azure.com:443/");

        // Sibling client keeps the shared entry alive for the whole test, so the only thing that could
        // retain client A is the shared cache itself.
        RxDocumentClientImpl clientB = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collB = Mockito.mock(RxCollectionCache.class);
        RxPartitionKeyRangeCache cacheB = new RxPartitionKeyRangeCache(clientB, collB, endpoint);

        try {
            assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
                .isEqualTo(1);

            WeakReference<RxPartitionKeyRangeCache> weakA = populateThenCloseInitiatingClient(endpoint);

            // A released its refcount on close(); the entry is still alive via cacheB.
            assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
                .isEqualTo(1);

            boolean collected = false;
            long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (System.nanoTime() < deadlineNanos) {
                System.gc();
                System.runFinalization();
                Thread.sleep(100);
                if (weakA.get() == null) {
                    collected = true;
                    break;
                }
            }

            assertThat(collected)
                .as("client A must be GC-eligible after close() even though a sibling keeps the shared "
                    + "cache entry alive; the shared routing map must not retain A's object graph")
                .isTrue();
        } finally {
            cacheB.close();
        }

        assertThat(SharedPartitionKeyRangeCacheRegistry.getInstance().referenceCount(endpoint))
            .isZero();
    }

    // Populates the shared routing map with an isolated client A, closes it, and returns a weak
    // reference to it. A is created in this separate frame (and only referenced by the local vars
    // here) so it can't be kept alive by the caller's stack once this method returns.
    private static WeakReference<RxPartitionKeyRangeCache> populateThenCloseInitiatingClient(URI endpoint) {
        RxDocumentClientImpl clientA = Mockito.mock(RxDocumentClientImpl.class);
        RxCollectionCache collA = Mockito.mock(RxCollectionCache.class);

        String collectionRid = "owner-release-coll";
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId(collectionRid);
        collection.setSelfLink("dbs/db1/colls/coll1");

        PartitionKeyRange range = new PartitionKeyRange();
        range.setId("0");
        range.setMinInclusive(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY);
        range.setMaxExclusive(PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY);

        FeedResponse<PartitionKeyRange> response = Mockito.mock(FeedResponse.class);
        when(response.getResults()).thenReturn(Arrays.asList(range));
        when(response.getContinuationToken()).thenReturn("etag-owner-release");

        when(collA.resolveCollectionAsync(any(), any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        when(clientA.readPartitionKeyRanges(eq(collection.getSelfLink()), any(CosmosQueryRequestOptions.class)))
            .thenReturn(Flux.just(response));

        RxPartitionKeyRangeCache cacheA = new RxPartitionKeyRangeCache(clientA, collA, endpoint);
        // The init Mono captures cacheA (its client, collection cache, diagnostics); populate the
        // shared routing map so that captured Mono would be stored in the shared cache.
        cacheA.tryLookupAsync(null, collectionRid, null, new HashMap<>()).block();
        cacheA.close();

        return new WeakReference<>(cacheA);
    }
}
