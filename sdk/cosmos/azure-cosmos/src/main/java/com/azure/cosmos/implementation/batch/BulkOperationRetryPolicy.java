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
import com.azure.cosmos.implementation.RetryPolicyWithDiagnostics;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A container to keep retry policies and functions for bulk.
 */
final class BulkOperationRetryPolicy extends RetryPolicyWithDiagnostics {

    private static final int MAX_RETRIES = 1;

    private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;
    private int attemptedRetries;

    BulkOperationRetryPolicy(ResourceThrottleRetryPolicy resourceThrottleRetryPolicy) {
        this.resourceThrottleRetryPolicy = resourceThrottleRetryPolicy;
    }

    final Mono<IRetryPolicy.ShouldRetryResult> shouldRetry(final TransactionalBatchOperationResult result) {

        checkNotNull(result, "expected non-null result");

        // Create CosmosException for the next retry policy to understand:
        CosmosException exception = BridgeInternal.createCosmosException(
            result.getStatusCode(),
            null,
            BulkExecutorUtil.getResponseHeadersFromBatchOperationResult(result));

        if (this.resourceThrottleRetryPolicy == null) {
            return Mono.just(IRetryPolicy.ShouldRetryResult.noRetry());
        }

        return this.resourceThrottleRetryPolicy.shouldRetry(exception);
    }

    @Override
    public Mono<IRetryPolicy.ShouldRetryResult> shouldRetry(Exception exception) {

        if (this.resourceThrottleRetryPolicy == null) {
            return Mono.just(IRetryPolicy.ShouldRetryResult.noRetry());
        }

        return this.resourceThrottleRetryPolicy.shouldRetry(exception);
    }

    boolean shouldRetryForGone(int statusCode, int subStatusCode) {

        if (statusCode == StatusCodes.GONE
            && (subStatusCode == SubStatusCodes.PARTITION_KEY_RANGE_GONE || subStatusCode == SubStatusCodes.NAME_CACHE_IS_STALE)
            && this.attemptedRetries < MAX_RETRIES) {
            this.attemptedRetries++;
            return true;
        }

        return false;
    }
}
