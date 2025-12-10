// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.CosmosBulkExecutionOptionsImpl;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.UUIDs;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.withContext;
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
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();
    private static final ImplementationBridgeHelpers.CosmosBatchResponseHelper.CosmosBatchResponseAccessor cosmosBatchResponseAccessor =
        ImplementationBridgeHelpers.CosmosBatchResponseHelper.getCosmosBatchResponseAccessor();

    private final CosmosAsyncContainer container;
    private final int maxMicroBatchPayloadSizeInBytes;
    private final AsyncDocumentClient docClientWrapper;
    private final String operationContextText;
    private final OperationContextAndListenerTuple operationListener;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private final Flux<CosmosBatch> inputBatches;

    private final TContext batchContext;
    private final CosmosBulkExecutionOptionsImpl cosmosBulkExecutionOptions;

    // Handle gone error:
    private final AtomicBoolean mainSourceCompleted;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicInteger totalCount;
    private final CosmosAsyncClient cosmosClient;
    private final String bulkSpanName;
    private final String identifier = "TransactionalBulkExecutor-" + instanceCount.incrementAndGet();
    private final BulkExecutorDiagnosticsTracker diagnosticsTracker;
    private final CosmosItemSerializer effectiveItemSerializer;
    private final Scheduler executionScheduler;

    @SuppressWarnings({"unchecked"})
    public TransactionalBulkExecutor(CosmosAsyncContainer container,
                        Flux<CosmosBatch> inputOperations,
                        CosmosBulkExecutionOptionsImpl cosmosBulkOptions) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(inputOperations, "expected non-null inputOperations");
        checkNotNull(cosmosBulkOptions, "expected non-null bulkOptions");

        this.maxMicroBatchPayloadSizeInBytes = cosmosBulkOptions.getMaxMicroBatchPayloadSizeInBytes();
        this.cosmosBulkExecutionOptions = cosmosBulkOptions;
        this.container = container;
        this.bulkSpanName = "transactionalBatch." + this.container.getId();
        this.inputBatches = inputOperations;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.cosmosClient = ImplementationBridgeHelpers
            .CosmosAsyncDatabaseHelper
            .getCosmosAsyncDatabaseAccessor()
            .getCosmosAsyncClient(container.getDatabase());
        this.effectiveItemSerializer = this.docClientWrapper.getEffectiveItemSerializer(cosmosBulkOptions.getCustomItemSerializer());

        this.throttlingRetryOptions = docClientWrapper.getConnectionPolicy().getThrottlingRetryOptions();

        // Fill the option first, to make the BulkProcessingOptions immutable, as if accessed directly, we might get
        // different values when a new group is created.
        batchContext = (TContext) cosmosBulkExecutionOptions.getLegacyBatchScopedContext();
        operationListener = cosmosBulkExecutionOptions.getOperationContextAndListenerTuple();
        if (operationListener != null &&
            operationListener.getOperationContext() != null) {
            operationContextText = identifier + "[" + operationListener.getOperationContext().toString() + "]";
        } else {
            operationContextText = identifier +"[n/a]";
        }

        this.diagnosticsTracker = cosmosBulkExecutionOptions.getDiagnosticsTracker();

        // For transactional batches, no sinks needed - batches are pre-constructed
        mainSourceCompleted = new AtomicBoolean(false);
        totalCount = new AtomicInteger(0);

        // No periodic flush scheduler needed - batches arrive pre-constructed from writer

        Scheduler schedulerSnapshotFromOptions = cosmosBulkOptions.getSchedulerOverride();
        this.executionScheduler = schedulerSnapshotFromOptions != null
            ? schedulerSnapshotFromOptions
            : CosmosSchedulers.BULK_EXECUTOR_BOUNDED_ELASTIC;

        logger.info("Instantiated BulkExecutor, Context: {}",
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

    public Flux<CosmosBulkOperationResponse<TContext>> execute() {
        return this
            .executeCore()
            .doFinally((SignalType signal) -> {
                if (signal == SignalType.ON_COMPLETE) {
                    logDebugOrWarning("BulkExecutor.execute flux completed - # left items {}, Context: {}, {}",
                        this.totalCount.get(),
                        this.operationContextText,
                        getThreadInfo());
                } else {
                    int itemsLeftSnapshot = this.totalCount.get();
                    if (itemsLeftSnapshot > 0) {
                        logInfoOrWarning("BulkExecutor.execute flux terminated - Signal: {} - # left items {}, Context: {}, {}",
                            signal,
                            itemsLeftSnapshot,
                            this.operationContextText,
                            getThreadInfo());
                    } else {
                        logDebugOrWarning("BulkExecutor.execute flux terminated - Signal: {} - # left items {}, Context: {}, {}",
                            signal,
                            itemsLeftSnapshot,
                            this.operationContextText,
                            getThreadInfo());
                    }
                }

                this.dispose();
            });
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executeCore() {

        // For transactional batches, batches are pre-constructed by the writer with proper partition key grouping.
        // We just need to execute each pre-built batch.
        Integer nullableMaxConcurrentCosmosPartitions = cosmosBulkExecutionOptions.getMaxConcurrentCosmosPartitions();
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
                cosmosBatch -> executeTransactionalBatch(cosmosBatch),
                maxConcurrentBatches)
            .subscribeOn(this.executionScheduler);
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executeTransactionalBatch(CosmosBatch cosmosBatch) {
        // Extract operations from the pre-built batch
        List<CosmosItemOperation> operations = cosmosBatch.getOperations();
        
        if (operations == null || operations.isEmpty()) {
            logger.trace("Empty batch received, Context: {}", this.operationContextText);
            return Flux.empty();
        }

        String batchTrackingId = UUIDs.nonBlockingRandomUUID().toString();
        PartitionKey partitionKey = cosmosBatch.getPartitionKeyValue();
        
        // Validate batch payload size before execution
        int totalSerializedLength = this.calculateTotalSerializedLength(operations);
        if (totalSerializedLength > this.maxMicroBatchPayloadSizeInBytes) {
            String errorMessage = String.format(
                "Transactional batch exceeds maximum payload size: %d bytes (limit: %d bytes), PK: %s",
                totalSerializedLength,
                this.maxMicroBatchPayloadSizeInBytes,
                partitionKey);
            logger.error("{}, Context: {}", errorMessage, this.operationContextText);
            
            // Return error responses for all operations in the batch
            CosmosException payloadSizeException = BridgeInternal.createCosmosException(
                HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE,
                errorMessage);
            
            return Flux.fromIterable(operations)
                .map(operation -> {
                    TContext actualContext = this.getActualContext(operation);
                    return ModelBridgeInternal.createCosmosBulkOperationResponse(
                        operation,
                        payloadSizeException,
                        actualContext);
                });
        }
        
        logDebugOrWarning(
            "Executing transactional batch - {} operations, PK: {}, TrackingId: {}, Context: {}",
            operations.size(),
            partitionKey,
            batchTrackingId,
            this.operationContextText);

        // Create SinglePartitionKeyServerBatchRequest with atomic batch semantics
        final SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createBatchRequest(
            partitionKey,
            operations,
            this.effectiveItemSerializer);
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        // Set up request options
        RequestOptions options = new RequestOptions();
        options.setThroughputControlGroupName(cosmosBulkExecutionOptions.getThroughputControlGroupName());
        options.setExcludedRegions(cosmosBulkExecutionOptions.getExcludedRegions());
        options.setKeywordIdentifiers(cosmosBulkExecutionOptions.getKeywordIdentifiers());

        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicySnapshot =
            cosmosBulkExecutionOptions.getCosmosEndToEndLatencyPolicyConfig();
        if (e2eLatencyPolicySnapshot != null) {
            options.setCosmosEndToEndLatencyPolicyConfig(e2eLatencyPolicySnapshot);
        }

        Map<String, String> customOptions = cosmosBulkExecutionOptions.getHeaders();
        if (customOptions != null && !customOptions.isEmpty()) {
            for(Map.Entry<String, String> entry : customOptions.entrySet()) {
                options.setHeader(entry.getKey(), entry.getValue());
            }
        }
        options.setOperationContextAndListenerTuple(operationListener);

        return this.docClientWrapper
            .executeBatchRequest(
                BridgeInternal.getLink(this.container),
                request,
                options,
                false,
                false)
            .subscribeOn(this.executionScheduler)
            .flatMapMany(cosmosBatchResponse -> {
                List<CosmosBatchOperationResult> results = cosmosBatchResponse.getResults();
                
                if (results == null || results.size() != operations.size()) {
                    String errorMessage = String.format(
                        "Transactional batch response mismatch: expected %d results, got %d",
                        operations.size(),
                        results != null ? results.size() : 0);
                    logger.error("{}, Context: {}", errorMessage, this.operationContextText);
                    
                    // Return error responses for all operations
                    return Flux.fromIterable(operations)
                        .map(operation -> {
                            Exception exception = new IllegalStateException(errorMessage);
                            TContext actualContext = this.getActualContext(operation);
                            return ModelBridgeInternal.createCosmosBulkOperationResponse(
                                operation,
                                exception,
                                actualContext);
                        });
                }
                
                // Map results back to operations
                return Flux.range(0, operations.size())
                    .map(index -> {
                        CosmosItemOperation operation = operations.get(index);
                        CosmosBatchOperationResult operationResult = results.get(index);
                        TContext actualContext = this.getActualContext(operation);
                        
                        CosmosBulkItemResponse cosmosBulkItemResponse = ModelBridgeInternal.createCosmosBulkItemResponse(
                            operationResult,
                            cosmosBatchResponse);
                        
                        if (!operationResult.isSuccessStatusCode()) {
                            logDebugOrWarning(
                                "Transactional batch operation failed - PK: {}, Status: {}, Operation: {}, Context: {}",
                                String.valueOf(partitionKey),
                                operationResult.getStatusCode(),
                                getItemOperationDiagnostics(operation),
                                this.operationContextText);
                        }
                        
                        return ModelBridgeInternal.createCosmosBulkOperationResponse(
                            operation,
                            cosmosBulkItemResponse,
                            actualContext);
                    });
            })
            .onErrorResume(throwable -> {
                logger.error(
                    "Transactional batch execution failed - PK: {}, Error: {}, Context: {}",
                    String.valueOf(partitionKey),
                    throwable.getMessage(),
                    this.operationContextText,
                    throwable);
                
                // Convert Throwable to Exception if needed
                Exception exception = throwable instanceof Exception ?
                    (Exception) throwable :
                    new RuntimeException(throwable);
                
                // Return error responses for all operations in the batch
                return Flux.fromIterable(operations)
                    .map(operation -> {
                        TContext actualContext = this.getActualContext(operation);
                        return ModelBridgeInternal.createCosmosBulkOperationResponse(
                            operation,
                            exception,
                            actualContext);
                    });
            });
    }

    private int calculateTotalSerializedLength(List<CosmosItemOperation> operations) {
        int totalLength = 0;
        for (CosmosItemOperation operation : operations) {
            if (operation instanceof CosmosItemOperationBase) {
                totalLength += ((CosmosItemOperationBase) operation).getSerializedLength(this.effectiveItemSerializer);
            }
        }
        return totalLength;
    }

    private TContext getActualContext(CosmosItemOperation itemOperation) {
        ItemBulkOperation<?, ?> itemBulkOperation = null;

        if (itemOperation instanceof ItemBulkOperation<?, ?>) {
            itemBulkOperation = (ItemBulkOperation<?, ?>) itemOperation;
        }

        if (itemBulkOperation == null) {
            return this.batchContext;
        }

        TContext operationContext = itemBulkOperation.getContext();
        if (operationContext != null) {
            return operationContext;
        }

        return this.batchContext;
    }

    private void completeAllSinks() {
        logInfoOrWarning("Completing execution, Context: {}", this.operationContextText);
        logger.debug("Executor service shut down, Context: {}", this.operationContextText);
        this.shutdown();
    }

    private static String getItemOperationDiagnostics(CosmosItemOperation operation) {

        if (operation == FlushBuffersItemOperation.singleton()) {
            return "ItemOperation[Type: Flush]";
        }

        return "ItemOperation[Type: "
            + operation.getOperationType().toString()
            + ", PK: "
            + (operation.getPartitionKeyValue() != null ? operation.getPartitionKeyValue().toString() : "n/a")
            + ", id: "
            + operation.getId()
            + "]";
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
