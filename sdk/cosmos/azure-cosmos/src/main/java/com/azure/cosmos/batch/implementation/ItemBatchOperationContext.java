// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import com.azure.cosmos.batch.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.IRetryPolicy.ShouldRetryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Context for a particular Batch operation.
 */
public class ItemBatchOperationContext implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ItemBatchOperationContext.class);

    private final CompletableFuture<TransactionalBatchOperationResult<?>> operationResultFuture;
    private BatchAsyncBatcher currentBatcher;
    private final String partitionKeyRangeId;
    private final BatchPartitionKeyRangeGoneRetryPolicy retryPolicy;

    /**
     * Instantiates a new Item batch operation context.
     *
     * @param partitionKeyRangeId the partition key range id
     */
    public ItemBatchOperationContext(final String partitionKeyRangeId) {
        this(partitionKeyRangeId, null);
    }

    /**
     * Instantiates a new Item batch operation context.
     *
     * @param partitionKeyRangeId the partition key range id
     * @param retryPolicy the retry policy
     */
    public ItemBatchOperationContext(
        final String partitionKeyRangeId,
        final BatchPartitionKeyRangeGoneRetryPolicy retryPolicy) {

        checkNotNull(partitionKeyRangeId, "expected non-null partitionKeyRangeId");
        this.operationResultFuture = new CompletableFuture<>();
        this.partitionKeyRangeId = partitionKeyRangeId;
        this.retryPolicy = retryPolicy;
    }

    /**
     * Complete.
     *
     * @param completer the completer
     * @param result the result
     */
    public final void complete(BatchAsyncBatcher completer, TransactionalBatchOperationResult<?> result) {
        if (this.assertBatcher(completer)) {
            this.operationResultFuture.complete(result);
        }
        this.close();
    }

    /**
     * Fail.
     *
     * @param completer the completer
     * @param error the error
     */
    public final void fail(BatchAsyncBatcher completer, Throwable error) {
        if (this.assertBatcher(completer, error)) {
            this.operationResultFuture.completeExceptionally(error);
        }
        this.close();
    }

    /**
     * Based on the Retry Policy, if a failed response should retry.
     *
     * @param result result of batch operation.
     *
     * @return indicates whether a retry should be attempted.
     */
    public final Mono<ShouldRetryResult> shouldRetry(final TransactionalBatchOperationResult<?> result) {

        checkNotNull(result, "expected non-null result");

        if (this.retryPolicy == null || result.isSuccessStatusCode()) {
            return Mono.just(ShouldRetryResult.noRetry());
        }

        return this.retryPolicy.shouldRetry(result.toResponseMessage());
    }

    private boolean assertBatcher(BatchAsyncBatcher completer) {
        return assertBatcher(completer, null);
    }

    private boolean assertBatcher(BatchAsyncBatcher completer, Throwable error) {
        if (completer != this.getCurrentBatcher()) {
            final String message = "operation was completed by incorrect batcher";
            logger.error(message);
            this.operationResultFuture.completeExceptionally(new RuntimeException(message, error));
            return false;
        }

        return true;
    }

    /**
     * Gets current batcher.
     *
     * @return the current batcher
     */
    public final BatchAsyncBatcher getCurrentBatcher() {
        return currentBatcher;
    }

    /**
     * Sets current batcher.
     *
     * @param value the value
     */
    public final void setCurrentBatcher(BatchAsyncBatcher value) {
        currentBatcher = value;
    }

    /**
     * Gets operation result future.
     *
     * @return the operation result future
     */
    public final CompletableFuture<TransactionalBatchOperationResult<?>> getOperationResultFuture() {
        return this.operationResultFuture;
    }

    /**
     * Gets partition key range id.
     *
     * @return the partition key range id
     */
    public final String getPartitionKeyRangeId() {
        return partitionKeyRangeId;
    }

    /**
     * Closes the current {@link ItemBatchOperationContext item batch operation context}.
     */
    public final void close() {
        this.operationResultFuture.cancel(true);
        this.setCurrentBatcher(null);
    }
}
