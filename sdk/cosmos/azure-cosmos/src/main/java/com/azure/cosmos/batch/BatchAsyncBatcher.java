// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.*;

/**
 * Maintains a batch of operations and dispatches it as a unit of work.
 * <p>
 * The dispatch process involves:
 * <ol>
 * <li>Creating a {@link PartitionKeyRangeServerBatchRequest}.
 * <li>Verifying overflow that might happen due to serialization. Any operations that did not fit, get sent
 * to the {@link BatchAsyncBatcherRetrier}.
 * <li>Delegating to {@link BatchAsyncBatcherExecutor} to execute a request.
 * <li>Delegating to {@link BatchAsyncBatcherRetrier} to retry a request, if a split is detected. In this case
 * all operations in the request are sent to the {@link BatchAsyncBatcherRetrier} for re-queueing.
 * </ol>
 * The result of the request is used to wire up all responses with the original tasks for each operation.
 * @see ItemBatchOperation
 *
 * TODO: Exception handling and diagnostics. Functionality wise this is complete and subscribe works as intened and executes asap.
 */
public class BatchAsyncBatcher {

    private static final Logger logger = LoggerFactory.getLogger(BatchAsyncBatcher.class);

    private final Semaphore interlockIncrementCheck = new Semaphore(1);
    private final ArrayList<ItemBatchOperation<?>> operations;
    private long currentSize = 0;
    private boolean dispatched = false;
    private final BatchAsyncBatcherExecutor executor;
    private final int maxBatchByteSize;
    private final int maxBatchOperationCount;
    private final BatchAsyncBatcherRetrier retrier;

    public BatchAsyncBatcher(
        final int maxBatchOperationCount,
        final int maxBatchByteSize,
        final BatchAsyncBatcherExecutor executor,
        final BatchAsyncBatcherRetrier retrier) {

        checkArgument(maxBatchOperationCount > 0,
            "expected maxBatchOperationCount > 0, not %s",
            maxBatchOperationCount);

        checkArgument(maxBatchByteSize > 0,
            "expected maxBatchByteSize > 0, not %s",
            maxBatchByteSize);

        checkNotNull(executor, "expected non-null executor");
        checkNotNull(retrier, "expected non-null retrier");

        this.operations = new ArrayList<>(maxBatchOperationCount);
        this.maxBatchOperationCount = maxBatchOperationCount;
        this.maxBatchByteSize = maxBatchByteSize;
        this.executor = executor;
        this.retrier = retrier;
    }

    public boolean tryAdd(ItemBatchOperation<?> operation) {

        if (this.dispatched) {
            logger.error("Add operation attempted on dispatched batch.");
            return false;
        }

        checkNotNull(operation, "expected non-null operation");
        checkNotNull(operation.getContext(), "expected non-null operation context");

        if (this.operations.size() == this.maxBatchOperationCount) {
            logger.info("Batch is full - Max operation count {} reached.", this.maxBatchOperationCount);
            return false;
        }

        int itemByteSize = operation.getApproximateSerializedLength();

        if (!this.operations.isEmpty() && itemByteSize + this.currentSize > this.maxBatchByteSize) {
            logger.info("Batch is full - Max byte size {} reached.", this.maxBatchByteSize);
            return false;
        }

        this.currentSize += itemByteSize;

        // Operation index is in the scope of the current batch
        operation.setOperationIndex(this.operations.size());
        operation.getContext().setCurrentBatcher(this);
        this.operations.add(operation);

        return true;
    }

    void dispatchBatch(BatchPartitionMetric partitionMetric) {

        checkState(interlockIncrementCheck.tryAcquire(), "failed to acquire dispatch permit");

        this.createBatchRequestAsync()
            .exceptionally(exception -> {

                // Exceptions happening during request creation, fail the entire list
                this.failBatchOperations(this.operations, exception);
                throw new CompletionException(exception);
            }).thenAcceptAsync(batchRequest -> {

                // Serialization might leave some pending operations out of the batch
                PartitionKeyRangeServerBatchRequest serverRequest = batchRequest.getBatchRequest();
                final List<ItemBatchOperation<?>> batchPendingOperations = batchRequest.getBatchPendingOperations();

                try {
                    // Any overflow goes to a new batch
                    for (ItemBatchOperation<?> batchOperation : batchPendingOperations) {
                        this.retrier.apply(batchOperation);
                    }
                } catch (Exception exception) {
                    // If retrier throws some exception, fail the pending operation but try out the serverRequest
                    this.failBatchOperations(batchPendingOperations, exception);
                }

                try {
                   this.executeServerRequest(serverRequest, partitionMetric);
                } catch (Throwable throwable) {
                    // Exceptions happening during execution, fail all the tasks part of the request (excluding overflow)
                    this.failBatchOperations(serverRequest.getOperations(), throwable);
                    throw new CompletionException(throwable);
                }
            })
            .exceptionally(throwable -> {
                // At the end of every exception, so as dispatcher doesn't return or throws any exception
                this.operations.clear();
                this.dispatched = true;

                // eat up the exception
                return null;
            });
    }

    public CompletableFuture<ServerOperationBatchRequest> createBatchRequestAsync() {

        // All operations should be for the same PKRange
        final String partitionKeyRangeId = this.operations.get(0).getContext().getPartitionKeyRangeId();

        // Check on what happens to this list
        final List<ItemBatchOperation<?>> operations = UnmodifiableList.unmodifiableList(this.operations);
        return PartitionKeyRangeServerBatchRequest.createAsync(
            partitionKeyRangeId,
            operations,
            this.maxBatchByteSize,
            this.maxBatchOperationCount,
            false);
    }

    private void executeServerRequest(PartitionKeyRangeServerBatchRequest serverRequest, BatchPartitionMetric partitionMetric) throws Throwable {

        Instant startBatchExecution = Instant.now();
        this.executor.apply(serverRequest)
            .subscribe((PartitionKeyRangeBatchExecutionResult executionResult) -> {

                // Fill partition metric
                boolean throttled = executionResult.getServerResponse().stream()
                    .anyMatch(r -> r.getStatus() == HttpResponseStatus.TOO_MANY_REQUESTS);
                partitionMetric.add(
                    executionResult.getServerResponse().size(),
                    Duration.between(startBatchExecution, Instant.now()).toMillis(),
                    throttled ? 1: 0);

                PartitionKeyRangeBatchResponse batchResponse = new PartitionKeyRangeBatchResponse(
                    serverRequest.getOperations().size(),
                    executionResult.getServerResponse());

                for (ItemBatchOperation<?> itemBatchOperation : batchResponse.getBatchOperations()) {

                    final TransactionalBatchOperationResult<?> operationResult = batchResponse.get(itemBatchOperation.getOperationIndex());
                    final ItemBatchOperationContext context = itemBatchOperation.getContext();

                    // Bulk has diagnostics per a item itemBatchOperation.
                    // Batch has a single diagnostics for the execute itemBatchOperation
                    operationResult.setCosmosDiagnostics(batchResponse.getCosmosDiagnostics());

                    if (!operationResult.isSuccessStatusCode()) {
                        context.shouldRetry(operationResult).subscribe(
                            result -> {
                                if (result.shouldRetry) {
                                    this.retrier.apply(itemBatchOperation);
                                } else {
                                    context.complete(this, operationResult);
                                }
                            });
                    } else {
                        context.complete(this, operationResult);
                    }
                }

                this.operations.clear();
                this.dispatched = true;
        }, throwable -> {
                this.failBatchOperations(serverRequest.getOperations(), throwable);
                this.operations.clear();
                this.dispatched = true;
        });
    }

    private void failBatchOperations(List<ItemBatchOperation<?>> batchOperations, Throwable exception) {
        for (ItemBatchOperation<?> batchOperation : batchOperations) {
            batchOperation.getContext().fail(this, exception);
        }
    }

    public final boolean isEmpty() {
        return this.operations.isEmpty();
    }
}
