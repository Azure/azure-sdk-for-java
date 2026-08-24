// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkOperationRetryPolicyTest {
    private static final int TIMEOUT = 40000;

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void shouldRetryInMainSinkRefreshesPartitionKeyRangesOnNameCacheStaleRetryLimit() {
        RxCollectionCache collectionCache = Mockito.mock(RxCollectionCache.class);
        RxPartitionKeyRangeCache partitionKeyRangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId("collectionRid");

        Mockito.when(collectionCache.resolveByNameAsync(Mockito.isNull(), Mockito.eq("dbs/db/colls/coll"), Mockito.isNull()))
            .thenReturn(Mono.just(collection));
        Mockito.when(partitionKeyRangeCache.tryGetOverlappingRangesAsync(
                Mockito.isNull(),
                Mockito.eq("collectionRid"),
                Mockito.any(Range.class),
                Mockito.eq(true),
                Mockito.isNull()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(Collections.<PartitionKeyRange>emptyList())));

        BulkOperationRetryPolicy retryPolicy = new BulkOperationRetryPolicy(
            collectionCache,
            partitionKeyRangeCache,
            "dbs/db/colls/coll",
            null);
        ItemBulkOperation<?, ?> itemOperation = new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE,
            "id",
            PartitionKey.NONE,
            null,
            null,
            null);

        Boolean shouldRetryInMainSink = retryPolicy.shouldRetryInMainSink(
            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
            HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE_EXCEEDED_RETRY_LIMIT,
            itemOperation,
            null).block();

        assertThat(shouldRetryInMainSink).isTrue();
        Mockito.verify(collectionCache, Mockito.times(1))
            .resolveByNameAsync(Mockito.isNull(), Mockito.eq("dbs/db/colls/coll"), Mockito.isNull());
        Mockito.verify(partitionKeyRangeCache, Mockito.times(1))
            .tryGetOverlappingRangesAsync(
                Mockito.isNull(),
                Mockito.eq("collectionRid"),
                Mockito.any(Range.class),
                Mockito.eq(true),
                Mockito.isNull());
        Mockito.verify(collectionCache, Mockito.never()).refresh(Mockito.any(), Mockito.anyString(), Mockito.any());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void shouldRetryInMainSinkStopsRetryingNameCacheStaleAfterBound() {
        RxCollectionCache collectionCache = Mockito.mock(RxCollectionCache.class);
        RxPartitionKeyRangeCache partitionKeyRangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId("collectionRid");

        Mockito.when(collectionCache.resolveByNameAsync(Mockito.isNull(), Mockito.eq("dbs/db/colls/coll"), Mockito.isNull()))
            .thenReturn(Mono.just(collection));
        Mockito.when(partitionKeyRangeCache.tryGetOverlappingRangesAsync(
                Mockito.isNull(),
                Mockito.eq("collectionRid"),
                Mockito.any(Range.class),
                Mockito.eq(true),
                Mockito.isNull()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(Collections.<PartitionKeyRange>emptyList())));

        BulkOperationRetryPolicy retryPolicy = new BulkOperationRetryPolicy(
            collectionCache,
            partitionKeyRangeCache,
            "dbs/db/colls/coll",
            null);
        ItemBulkOperation<?, ?> itemOperation = new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE,
            "id",
            PartitionKey.NONE,
            null,
            null,
            null);

        // The new 503/NAME_CACHE_IS_STALE_EXCEEDED_RETRY_LIMIT path shares the bulk retry counter (MAX_RETRIES = 1),
        // so it must retry at most twice and then stop - it must not become an unbounded retry loop.
        assertThat(invokeNameCacheStaleRetry(retryPolicy, itemOperation)).isTrue();
        assertThat(invokeNameCacheStaleRetry(retryPolicy, itemOperation)).isTrue();
        assertThat(invokeNameCacheStaleRetry(retryPolicy, itemOperation)).isFalse();

        // The cache refresh only happens on the two attempts that actually retried.
        Mockito.verify(collectionCache, Mockito.times(2))
            .resolveByNameAsync(Mockito.isNull(), Mockito.eq("dbs/db/colls/coll"), Mockito.isNull());
        Mockito.verify(collectionCache, Mockito.never()).refresh(Mockito.any(), Mockito.anyString(), Mockito.any());
    }

    private static Boolean invokeNameCacheStaleRetry(
        BulkOperationRetryPolicy retryPolicy,
        ItemBulkOperation<?, ?> itemOperation) {

        return retryPolicy.shouldRetryInMainSink(
            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
            HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE_EXCEEDED_RETRY_LIMIT,
            itemOperation,
            null).block();
    }
}
