// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants.StatusCodes;
import com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A container to keep retry policies and functions for bulk.
 */
final class BulkOperationRetryPolicy implements IRetryPolicy {

    private static final int MAX_RETRIES = 1;

    private final RxCollectionCache collectionCache;
    private final String collectionLink;
    private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;
    private int attemptedRetries;

    BulkOperationRetryPolicy(
        RxCollectionCache collectionCache,
        String resourceFullName,
        ResourceThrottleRetryPolicy resourceThrottleRetryPolicy) {

        this.collectionCache = collectionCache;

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

    boolean shouldRetryForGone(int statusCode, int subStatusCode) {

        if (statusCode == StatusCodes.GONE
            && (subStatusCode == SubStatusCodes.PARTITION_KEY_RANGE_GONE ||
                subStatusCode == SubStatusCodes.NAME_CACHE_IS_STALE ||
                subStatusCode == SubStatusCodes.COMPLETING_SPLIT ||
                subStatusCode == SubStatusCodes.COMPLETING_PARTITION_MIGRATION)
            && this.attemptedRetries < MAX_RETRIES) {

            this.attemptedRetries++;

            if (subStatusCode == SubStatusCodes.NAME_CACHE_IS_STALE) {
                refreshCollectionCache();
            }

            return true;
        }

        return false;
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
