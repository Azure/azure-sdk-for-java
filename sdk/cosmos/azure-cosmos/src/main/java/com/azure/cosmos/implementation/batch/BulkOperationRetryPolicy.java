// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.HttpConstants.StatusCodes;
import com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A container to keep retry policies and functions for bulk.
 */
final class BulkOperationRetryPolicy implements IRetryPolicy {

    private static final int MAX_RETRIES = 1;

    private final RxCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final String collectionLink;
    private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;
    private int attemptedRetries;

    BulkOperationRetryPolicy(
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

    final Mono<ShouldRetryResult> shouldRetry(final TransactionalBatchOperationResult result) {

        checkNotNull(result, "expected non-null result");

        // Create CosmosException for the next retry policy to understand:
        CosmosException exception = BridgeInternal.createCosmosException(
            null,
            result.getStatusCode(),
            null,
            BulkExecutorUtil.getResponseHeadersFromBatchOperationResult(result));

        if (this.resourceThrottleRetryPolicy == null) {
            return Mono.just(ShouldRetryResult.noRetry());
        }

        return this.resourceThrottleRetryPolicy.shouldRetry(exception);
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

    Mono<Boolean> shouldRetryForGone(int statusCode, int subStatusCode) {
        if (statusCode == StatusCodes.GONE) {
            if (this.attemptedRetries++ > MAX_RETRIES) {
                return Mono.just(false);
            }

            if ((subStatusCode == SubStatusCodes.PARTITION_KEY_RANGE_GONE ||
                     subStatusCode == SubStatusCodes.COMPLETING_SPLIT ||
                     subStatusCode == SubStatusCodes.COMPLETING_PARTITION_MIGRATION)) {
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

            if (subStatusCode == SubStatusCodes.NAME_CACHE_IS_STALE) {
                refreshCollectionCache();
            }

            return Mono.just(true);
        }

        return Mono.just(false);
    }

    /**
     * TODO(rakkuma): metaDataDiagnosticContext is passed null in collectionCache.refresh function. Fix it while adding
     *  support for an operation wise Diagnostic. The value here should be merged in the individual diagnostic.
     * Issue: https://github.com/Azure/azure-sdk-for-java/issues/17647
     */
    private void refreshCollectionCache() {
        this.collectionCache.refresh(
            null,
            this.collectionLink,
            null);
    }
}
