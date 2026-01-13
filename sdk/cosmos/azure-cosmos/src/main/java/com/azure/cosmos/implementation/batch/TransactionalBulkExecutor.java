// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * The Core logic of bulk execution is here.
 *
 * The actual execution of the flux of operations. It is done in following steps:

 * 1. Getting partition key range ID and grouping operations using that id.
 * 2. For the flux of operations in a group, adding buffering based on size and a duration.
 * 3. For the operation we get in after buffering, process it using a batch request and return
 *    a wrapper having request, response(if-any) and exception(if-any). Either response or exception will be there.
 *
 * 4. Any internal retry is done by adding in an intermediate sink for each grouped flux.
 * 5. Any operation which failed due to partition key range gone is retried by putting it in the main sink which leads
 *    to re-calculation of partition key range id.
 * 6. At the end and this is very essential, we close all the sinks as the sink continues to waits for more and the
 *    execution isn't finished even if all the operations have been executed(figured out by completion call of source)
 *
 * Note: Sink will move to a new interface from 3.5 and this is documentation for it:
 *    - https://github.com/reactor/reactor-core/blob/master/docs/asciidoc/processors.adoc
 *
 *    For our use case, Sinks.many().unicast() will work.
 */
public final class TransactionalBulkExecutor<TContext> implements Disposable {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBulkExecutor.class);
    private final static AtomicLong instanceCount = new AtomicLong(0);

    private final CosmosAsyncContainer container;
    private final AsyncDocumentClient docClientWrapper;
    private final String operationContextText;
    private final OperationContextAndListenerTuple operationListener;
    private final Flux<CosmosBatch> inputBatches;

    private final CosmosTransactionalBulkExecutionOptionsImpl transactionalBulkExecutionOptions;

    // Handle gone error:
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicInteger totalCount;
    private final String identifier = "TransactionalBulkExecutor-" + instanceCount.incrementAndGet();
    private final BulkExecutorDiagnosticsTracker diagnosticsTracker;
    private final CosmosItemSerializer effectiveItemSerializer;
    private final Scheduler executionScheduler;

    @SuppressWarnings({"unchecked"})
    public TransactionalBulkExecutor(CosmosAsyncContainer container,
                        Flux<CosmosBatch> inputBatches,
                        CosmosTransactionalBulkExecutionOptionsImpl transactionalBulkOptions) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(inputBatches, "expected non-null inputOperations");
        checkNotNull(transactionalBulkOptions, "expected non-null transactionalBulkOptions");

        this.transactionalBulkExecutionOptions = transactionalBulkOptions;
        this.container = container;
        this.inputBatches = inputBatches;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.effectiveItemSerializer = this.docClientWrapper.getEffectiveItemSerializer(transactionalBulkOptions.getCustomItemSerializer());
        operationListener = transactionalBulkExecutionOptions.getOperationContextAndListenerTuple();
        if (operationListener != null &&
            operationListener.getOperationContext() != null) {
            operationContextText = identifier + "[" + operationListener.getOperationContext().toString() + "]";
        } else {
            operationContextText = identifier +"[n/a]";
        }

        this.diagnosticsTracker = transactionalBulkExecutionOptions.getDiagnosticsTracker();

        totalCount = new AtomicInteger(0);

        Scheduler schedulerSnapshotFromOptions = transactionalBulkOptions.getSchedulerOverride();
        this.executionScheduler = schedulerSnapshotFromOptions != null
            ? schedulerSnapshotFromOptions
            : CosmosSchedulers.TRANSACTIONAL_BULK_EXECUTOR_BOUNDED_ELASTIC;

        logger.info("Instantiated TransactionalBulkExecutor, Context: {}",
            this.operationContextText);
    }

    @Override
    public void dispose() {
        if (this.isDisposed.compareAndSet(false, true)) {
            long totalCountSnapshot = totalCount.get();
            if (totalCountSnapshot == 0) {
                completeAllSinks();
            } else {
                this.shutdown();
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return this.isDisposed.get();
    }

    private void logInfoOrWarning(String msg, Object... args) {
        if (this.diagnosticsTracker == null || !this.diagnosticsTracker.verboseLoggingAfterReEnqueueingRetriesEnabled()) {
            logger.info(msg, args);
        } else {
            logger.warn(msg, args);
        }
    }

    private void logTraceOrWarning(String msg, Object... args) {
        if (this.diagnosticsTracker == null || !this.diagnosticsTracker.verboseLoggingAfterReEnqueueingRetriesEnabled()) {
            logger.trace(msg, args);
        } else {
            logger.warn(msg, args);
        }
    }

    private void logDebugOrWarning(String msg, Object... args) {
        if (this.diagnosticsTracker == null || !this.diagnosticsTracker.verboseLoggingAfterReEnqueueingRetriesEnabled()) {
            logger.debug(msg, args);
        } else {
            logger.warn(msg, args);
        }
    }

    private void shutdown() {
        if (this.isShutdown.compareAndSet(false, true)) {
            logDebugOrWarning("Shutting down, Context: {}", this.operationContextText);
            logger.debug("Shutdown complete, Context: {}", this.operationContextText);
        }
    }

    public Flux<CosmosBatchResponse> execute() {
        return this
            .executeCore()
            .doFinally((SignalType signal) -> {
                if (signal == SignalType.ON_COMPLETE) {
                    logDebugOrWarning("TransactionalBulkExecutor.execute flux completed - # left items {}, Context: {}, {}",
                        this.totalCount.get(),
                        this.operationContextText,
                        getThreadInfo());
                } else {
                    int itemsLeftSnapshot = this.totalCount.get();
                    logInfoOrWarning("TransactionalBulkExecutor.execute flux terminated - Signal: {} - # left items {}, Context: {}, {}",
                        signal,
                        itemsLeftSnapshot,
                        this.operationContextText,
                        getThreadInfo());
                }

                this.dispose();
            });
    }

    private Flux<CosmosBatchResponse> executeCore() {

        // For transactional batches, batches are pre-constructed by the writer with proper partition key grouping.
        // We just need to execute each pre-built batch.
        Integer nullableMaxConcurrentCosmosPartitions = transactionalBulkExecutionOptions.getMaxConcurrentCosmosPartitions();
        int maxConcurrentBatches = nullableMaxConcurrentCosmosPartitions != null ?
            Math.max(256, nullableMaxConcurrentCosmosPartitions) : 256;

        logDebugOrWarning("TransactionalBulkExecutor.executeCore with MaxConcurrentBatches: {}, Context: {}",
            maxConcurrentBatches,
            this.operationContextText);

        return this.inputBatches
            .publishOn(this.executionScheduler)
            .onErrorMap(throwable -> {
                logger.warn("{}: Error observed when processing input batches. Cause: {}, Context: {}",
                    getThreadInfo(),
                    throwable.getMessage(),
                    this.operationContextText,
                    throwable);
                return throwable;
            })
            .flatMap(
                this::executeTransactionalBatch,
                maxConcurrentBatches)
            .subscribeOn(this.executionScheduler);
    }

    private Mono<CosmosBatchResponse> executeTransactionalBatch(CosmosBatch cosmosBatch) {
        // Extract operations from the pre-built batch
        List<CosmosItemOperation> operations = cosmosBatch.getOperations();

        String batchTrackingId = UUIDs.nonBlockingRandomUUID().toString();
        PartitionKey partitionKey = cosmosBatch.getPartitionKeyValue();

        logDebugOrWarning(
            "Executing transactional batch - {} operations, PK: {}, TrackingId: {}, Context: {}",
            operations.size(),
            partitionKey,
            batchTrackingId,
            this.operationContextText);

        // Set up request options
        CosmosBatchRequestOptions batchRequestOptions = getBatchRequestOptions();
        return this.container
            .executeCosmosBatch(cosmosBatch, batchRequestOptions)
            .doOnSuccess(response -> {
                logTraceOrWarning(
                    "Response for batch of partitionKey %s - status code %s, ActivityId: %s, batch TrackingId %s",
                    cosmosBatch.getPartitionKeyValue(),
                    response.getStatusCode(),
                    response.getActivityId(),
                    batchTrackingId);
            })
            .doOnError(throwable -> {
                logTraceOrWarning(
                    "Failed to get response for batch of partitionKey %s - batch TrackingId %s",
                    cosmosBatch.getPartitionKeyValue(),
                    batchTrackingId,
                    throwable);
            })
            .subscribeOn(this.executionScheduler);
    }

    private CosmosBatchRequestOptions getBatchRequestOptions() {
        CosmosBatchRequestOptions batchRequestOptions = new CosmosBatchRequestOptions();
        batchRequestOptions.setExcludedRegions(transactionalBulkExecutionOptions.getExcludedRegions());
        batchRequestOptions.setKeywordIdentifiers(transactionalBulkExecutionOptions.getKeywordIdentifiers());
        batchRequestOptions.setThroughputControlGroupName(transactionalBulkExecutionOptions.getThroughputControlGroupName());

        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicySnapshot =
            transactionalBulkExecutionOptions.getCosmosEndToEndLatencyPolicyConfig();
        if (e2eLatencyPolicySnapshot != null) {
            batchRequestOptions.setEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicySnapshot);
        }

        Map<String, String> customOptions = transactionalBulkExecutionOptions.getHeaders();
        if (customOptions != null && !customOptions.isEmpty()) {
            for(Map.Entry<String, String> entry : customOptions.entrySet()) {
                ImplementationBridgeHelpers
                    .CosmosBatchRequestOptionsHelper
                    .getCosmosBatchRequestOptionsAccessor()
                    .setHeader(batchRequestOptions, entry.getKey(), entry.getValue());
            }
        }
        batchRequestOptions.setOperationContextAndListenerTuple(operationListener);

        return batchRequestOptions;
    }

    private void completeAllSinks() {
        logInfoOrWarning("Completing execution, Context: {}", this.operationContextText);
        logger.debug("Executor service shut down, Context: {}", this.operationContextText);
        this.shutdown();
    }

    private static String getThreadInfo() {
        StringBuilder sb = new StringBuilder();
        Thread t = Thread.currentThread();
        sb
            .append("Thread[")
            .append("Name: ")
            .append(t.getName())
            .append(",Group: ")
            .append(t.getThreadGroup() != null ? t.getThreadGroup().getName() : "n/a")
            .append(", isDaemon: ")
            .append(t.isDaemon())
            .append(", Id: ")
            .append(t.getId())
            .append("]");

        return sb.toString();
    }
}
