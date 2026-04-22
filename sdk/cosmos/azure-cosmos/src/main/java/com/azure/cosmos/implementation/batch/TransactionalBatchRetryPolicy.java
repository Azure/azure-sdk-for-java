// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import reactor.core.publisher.Mono;

public class TransactionalBatchRetryPolicy implements IRetryPolicy {

    private static final int MAX_RETRIES = 1;

    private final RxCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final String collectionLink;
    private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;
    private int attemptedRetries;

    TransactionalBatchRetryPolicy(
        RxCollectionCache collectionCache,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String resourceFullName,
        ResourceThrottleRetryPolicy resourceThrottleRetryPolicy) {

        this.collectionCache = collectionCache;
        this.partitionKeyRangeCache = partitionKeyRangeCache;

        // Similar to PartitionKeyMismatchRetryPolicy constructor.
        collectionLink = Utils.getCollectionName(resourceFullName);
        this.resourceThrottleRetryPolicy = resourceThrottleRetryPolicy;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {

        if (this.resourceThrottleRetryPolicy == null) {
            return Mono.just(ShouldRetryResult.noRetry());
        }

        return this.resourceThrottleRetryPolicy.shouldRetry(exception);
    }

    @Override
    public RetryContext getRetryContext() {
        return this.resourceThrottleRetryPolicy.getRetryContext();
    }

    Mono<Boolean> shouldRetryInMainSink(CosmosException exception) {

        int statusCode = exception.getStatusCode();
        int subStatusCode = exception.getSubStatusCode();

        if (Exceptions.isStaledResourceException(statusCode, subStatusCode)) {
            refreshCollectionCache();
            return Mono.just(true);
        }

        if (statusCode == HttpConstants.StatusCodes.GONE) {
            if (++this.attemptedRetries > MAX_RETRIES) {
                return Mono.just(false);
            }

            if ((subStatusCode == HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE ||
                subStatusCode == HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE ||
                subStatusCode == HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION)) {
                return collectionCache
                    .resolveByNameAsync(null, collectionLink, null)
                    .flatMap(collection -> this.partitionKeyRangeCache
                        .tryGetOverlappingRangesAsync(null /*metaDataDiagnosticsContext*/,
                            collection.getResourceId(),
                            FeedRangeEpkImpl.forFullRange()
                                            .getRange(),
                            true,
                            null /*properties*/)
                        .then(Mono.just(true)));
            }

            return Mono.just(true);
        }

        return Mono.just(false);
    }

    private void refreshCollectionCache() {
        this.collectionCache.refresh(
            null,
            this.collectionLink,
            null);
    }
}
