// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
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
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
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
public final class BulkExecutor<TContext> implements Disposable {

    private final static Logger logger = LoggerFactory.getLogger(BulkExecutor.class);
    private final static AtomicLong instanceCount = new AtomicLong(0);
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();

    private final CosmosAsyncContainer container;
    private final int maxMicroBatchPayloadSizeInBytes;
    private final AsyncDocumentClient docClientWrapper;
    private final String operationContextText;
    private final OperationContextAndListenerTuple operationListener;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private final Flux<com.azure.cosmos.models.CosmosItemOperation> inputOperations;

    // Options for bulk execution.
    private final Long maxMicroBatchIntervalInMs;

    private final TContext batchContext;
    private final ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds;
    private final CosmosBulkExecutionOptionsImpl cosmosBulkExecutionOptions;

    // Handle gone error:
    private final AtomicBoolean mainSourceCompleted;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicInteger totalCount;
    private final static Sinks.EmitFailureHandler serializedEmitFailureHandler = new SerializedEmitFailureHandler();
    private final static Sinks.EmitFailureHandler serializedCompleteEmitFailureHandler =
        new SerializedCompleteEmitFailureHandler();
    private final Sinks.Many<CosmosItemOperation> mainSink;
    private final List<FluxSink<CosmosItemOperation>> groupSinks;
    private final CosmosAsyncClient cosmosClient;
    private final String bulkSpanName;
    private final AtomicReference<Disposable> scheduledFutureForFlush;
    private final String identifier = "BulkExecutor-" + instanceCount.incrementAndGet();
    private final BulkExecutorDiagnosticsTracker diagnosticsTracker;
    private final CosmosItemSerializer effectiveItemSerializer;
    private final Scheduler executionScheduler;

    @SuppressWarnings({"unchecked"})
    public BulkExecutor(CosmosAsyncContainer container,
                        Flux<CosmosItemOperation> inputOperations,
                        CosmosBulkExecutionOptionsImpl cosmosBulkOptions) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(inputOperations, "expected non-null inputOperations");
        checkNotNull(cosmosBulkOptions, "expected non-null bulkOptions");

        this.maxMicroBatchPayloadSizeInBytes = cosmosBulkOptions.getMaxMicroBatchPayloadSizeInBytes();
        this.cosmosBulkExecutionOptions = cosmosBulkOptions;
        this.container = container;
        this.bulkSpanName = "nonTransactionalBatch." + this.container.getId();
        this.inputOperations = inputOperations;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.cosmosClient = ImplementationBridgeHelpers
            .CosmosAsyncDatabaseHelper
            .getCosmosAsyncDatabaseAccessor()
            .getCosmosAsyncClient(container.getDatabase());
        this.effectiveItemSerializer = this.docClientWrapper.getEffectiveItemSerializer(cosmosBulkOptions.getCustomItemSerializer());

        this.throttlingRetryOptions = docClientWrapper.getConnectionPolicy().getThrottlingRetryOptions();

        // Fill the option first, to make the BulkProcessingOptions immutable, as if accessed directly, we might get
        // different values when a new group is created.
        maxMicroBatchIntervalInMs = cosmosBulkExecutionOptions.getMaxMicroBatchInterval().toMillis();
        batchContext = (TContext) cosmosBulkExecutionOptions.getLegacyBatchScopedContext();
        this.partitionScopeThresholds = ImplementationBridgeHelpers.CosmosBulkExecutionThresholdsStateHelper
            .getBulkExecutionThresholdsAccessor()
            .getPartitionScopeThresholds(cosmosBulkExecutionOptions.getThresholdsState());
        operationListener = cosmosBulkExecutionOptions.getOperationContextAndListenerTuple();
        if (operationListener != null &&
            operationListener.getOperationContext() != null) {
            operationContextText = identifier + "[" + operationListener.getOperationContext().toString() + "]";
        } else {
            operationContextText = identifier +"[n/a]";
        }

        this.diagnosticsTracker = cosmosBulkExecutionOptions.getDiagnosticsTracker();

        // Initialize sink for handling gone error.
        mainSourceCompleted = new AtomicBoolean(false);
        totalCount = new AtomicInteger(0);
        mainSink =  Sinks.many().unicast().onBackpressureBuffer();
        groupSinks = new CopyOnWriteArrayList<>();

        this.scheduledFutureForFlush = new AtomicReference<>(CosmosSchedulers
            .BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC
            .schedulePeriodically(
                this::onFlush,
                this.maxMicroBatchIntervalInMs,
                this.maxMicroBatchIntervalInMs,
                TimeUnit.MILLISECONDS));

        Scheduler schedulerSnapshotFromOptions = cosmosBulkOptions.getSchedulerOverride();
        this.executionScheduler = schedulerSnapshotFromOptions != null ? schedulerSnapshotFromOptions : CosmosSchedulers.BULK_EXECUTOR_BOUNDED_ELASTIC;

        logger.debug("Instantiated BulkExecutor, Context: {}",
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

    private void cancelFlushTask(boolean initializeAggressiveFlush) {
        long flushIntervalAfterDrainingIncomingFlux = Math.min(
            this.maxMicroBatchIntervalInMs,
            BatchRequestResponseConstants
                .DEFAULT_MAX_MICRO_BATCH_INTERVAL_AFTER_DRAINING_INCOMING_FLUX_IN_MILLISECONDS);

        Disposable newFlushTask = initializeAggressiveFlush
            ? CosmosSchedulers
                .BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC
                .schedulePeriodically(
                    this::onFlush,
                    flushIntervalAfterDrainingIncomingFlux,
                    flushIntervalAfterDrainingIncomingFlux,
                    TimeUnit.MILLISECONDS)
            : null;

        Disposable scheduledFutureSnapshot = this.scheduledFutureForFlush.getAndSet(newFlushTask);

        if (scheduledFutureSnapshot != null) {
            try {
                scheduledFutureSnapshot.dispose();
                logDebugOrWarning("Cancelled all future scheduled tasks {}, Context: {}", getThreadInfo(), this.operationContextText);
            } catch (Exception e) {
                logger.warn("Failed to cancel scheduled tasks{}, Context: {}", getThreadInfo(), this.operationContextText, e);
            }
        }
    }

    private void logInfoOrWarning(String msg, Object... args) {
        if (this.diagnosticsTracker == null || !this.diagnosticsTracker.verboseLoggingAfterReEnqueueingRetriesEnabled()) {
            logger.info(msg, args);
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

            groupSinks.forEach(FluxSink::complete);
            logger.debug("All group sinks completed, Context: {}", this.operationContextText);

            this.cancelFlushTask(false);
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

        // The groupBy below is running into a hang if the flatMap above is
        // not allowing at least a concurrency of the number of unique values
        // you groupBy on.
        // The groupBy is used to isolate Cosmos physical partitions
        // so when there is no config override we enforce that the flatMap is using a concurrency of
        // Math.max(default concurrency (256), #of partitions * 2 (to accommodate for some splits))
        // The config override can be used by the Spark connector when customers follow best practices and
        // repartition the data frame to avoid that each Spark partition contains data spread across all
        // physical partitions. When repartitioning the incoming data it is possible to ensure that each
        // Spark partition will only target a subset of Cosmos partitions. This will improve the efficiency
        // and mean fewer than #of Partitions concurrency will be needed for
        // large containers. (with hundreds of physical partitions)
        Integer nullableMaxConcurrentCosmosPartitions = cosmosBulkExecutionOptions.getMaxConcurrentCosmosPartitions();
        Mono<Integer> maxConcurrentCosmosPartitionsMono = nullableMaxConcurrentCosmosPartitions != null ?
            Mono.just(Math.max(256, nullableMaxConcurrentCosmosPartitions)) :
            ImplementationBridgeHelpers
                .CosmosAsyncContainerHelper
                .getCosmosAsyncContainerAccessor()
                .getFeedRanges(this.container, false).map(ranges -> Math.max(256, ranges.size() * 2));

        return
            maxConcurrentCosmosPartitionsMono
            .subscribeOn(this.executionScheduler)
            .flatMapMany(maxConcurrentCosmosPartitions -> {

                logDebugOrWarning("BulkExecutor.execute with MaxConcurrentPartitions: {}, Context: {}",
                    maxConcurrentCosmosPartitions,
                    this.operationContextText);

                return this.inputOperations
                    .publishOn(this.executionScheduler)
                    .onErrorMap(throwable -> {
                        logger.error("{}: Skipping an error operation while processing. Cause: {}, Context: {}",
                            getThreadInfo(),
                            throwable.getMessage(),
                            this.operationContextText,
                            throwable);

                        return throwable;
                    })
                    .doOnNext((CosmosItemOperation cosmosItemOperation) -> {
                        // Set the retry policy before starting execution. Should only happens once.
                        BulkExecutorUtil.setRetryPolicyForBulk(
                            docClientWrapper,
                            this.container,
                            cosmosItemOperation,
                            this.throttlingRetryOptions);

                        if (cosmosItemOperation != FlushBuffersItemOperation.singleton()) {
                            totalCount.incrementAndGet();
                        }

                        logger.trace(
                            "SetupRetryPolicy, {}, TotalCount: {}, Context: {}, {}",
                            getItemOperationDiagnostics(cosmosItemOperation),
                            totalCount.get(),
                            this.operationContextText,
                            getThreadInfo()
                        );
                    })
                    .doOnComplete(() -> {
                        mainSourceCompleted.set(true);

                        long totalCountSnapshot = totalCount.get();
                        logDebugOrWarning("Main source completed - # left items {}, Context: {}",
                            totalCountSnapshot,
                            this.operationContextText);
                        if (totalCountSnapshot == 0) {
                            // This is needed as there can be case that onComplete was called after last element was processed
                            // So complete the sink here also if count is 0, if source has completed and count isn't zero,
                            // then the last element in the doOnNext will close it. Sink doesn't mind in case of a double close.

                            completeAllSinks();
                        } else {
                            this.cancelFlushTask(true);
                            this.onFlush();

                            logDebugOrWarning("Scheduled new flush operation {}, Context: {}", getThreadInfo(), this.operationContextText);
                        }
                    })
                    .mergeWith(mainSink.asFlux())
                    .subscribeOn(this.executionScheduler)
                    .flatMap(
                        operation -> {
                            logger.trace("Before Resolve PkRangeId, {}, Context: {} {}",
                                getItemOperationDiagnostics(operation),
                                this.operationContextText,
                                getThreadInfo());

                            // resolve partition key range id again for operations which comes in main sink due to gone retry.
                            return BulkExecutorUtil.resolvePartitionKeyRangeId(this.docClientWrapper, this.container, operation)
                                                   .map((String pkRangeId) -> {
                                                       PartitionScopeThresholds partitionScopeThresholds =
                                                           this.partitionScopeThresholds.computeIfAbsent(
                                                               pkRangeId,
                                                               (newPkRangeId) -> new PartitionScopeThresholds(newPkRangeId, this.cosmosBulkExecutionOptions));

                                                       logger.trace("Resolved PkRangeId, {}, PKRangeId: {} Context: {} {}",
                                                           getItemOperationDiagnostics(operation),
                                                           pkRangeId,
                                                           this.operationContextText,
                                                           getThreadInfo());

                                                       return Pair.of(partitionScopeThresholds, operation);
                                                   });
                        })
                    .groupBy(Pair::getKey, Pair::getValue)
                    .flatMap(
                        this::executePartitionedGroup,
                        maxConcurrentCosmosPartitions)
                    .subscribeOn(this.executionScheduler)
                    .doOnNext(requestAndResponse -> {

                        int totalCountAfterDecrement = totalCount.decrementAndGet();
                        boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
                        if (totalCountAfterDecrement == 0 && mainSourceCompletedSnapshot) {
                            // It is possible that count is zero but there are more elements in the source.
                            // Count 0 also signifies that there are no pending elements in any sink.
                            logDebugOrWarning("All work completed, {}, TotalCount: {}, Context: {} {}",
                                getItemOperationDiagnostics(requestAndResponse.getOperation()),
                                totalCountAfterDecrement,
                                this.operationContextText,
                                getThreadInfo());
                            completeAllSinks();
                        } else {
                            if (totalCountAfterDecrement == 0) {
                                logDebugOrWarning(
                                    "No Work left - but mainSource not yet completed, Context: {} {}",
                                    this.operationContextText,
                                    getThreadInfo());
                            }
                            logger.trace(
                                "Work left - TotalCount after decrement: {}, main sink completed {}, {}, Context: {} {}",
                                totalCountAfterDecrement,
                                mainSourceCompletedSnapshot,
                                getItemOperationDiagnostics(requestAndResponse.getOperation()),
                                this.operationContextText,
                                getThreadInfo());
                        }
                    })
                    .doOnComplete(() -> {
                        int totalCountSnapshot = totalCount.get();
                        boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
                        if (totalCountSnapshot == 0 && mainSourceCompletedSnapshot) {
                            // It is possible that count is zero but there are more elements in the source.
                            // Count 0 also signifies that there are no pending elements in any sink.
                            logDebugOrWarning("DoOnComplete: All work completed, Context: {}", this.operationContextText);
                            completeAllSinks();
                        } else {
                            logDebugOrWarning(
                                "DoOnComplete: Work left - TotalCount after decrement: {}, main sink completed {}, Context: {} {}",
                                totalCountSnapshot,
                                mainSourceCompletedSnapshot,
                                this.operationContextText,
                                getThreadInfo());
                        }
                    });
            });
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executePartitionedGroup(
        GroupedFlux<PartitionScopeThresholds, CosmosItemOperation> partitionedGroupFluxOfInputOperations) {

        final PartitionScopeThresholds thresholds = partitionedGroupFluxOfInputOperations.key();

        final FluxProcessor<CosmosItemOperation, CosmosItemOperation> groupFluxProcessor =
            UnicastProcessor.<CosmosItemOperation>create().serialize();
        final FluxSink<CosmosItemOperation> groupSink = groupFluxProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        groupSinks.add(groupSink);

        AtomicLong firstRecordTimeStamp = new AtomicLong(-1);
        AtomicLong currentMicroBatchSize = new AtomicLong(0);
        AtomicInteger currentTotalSerializedLength = new AtomicInteger(0);

        return partitionedGroupFluxOfInputOperations
            .mergeWith(groupFluxProcessor)
            .onBackpressureBuffer()
            .timestamp()
            .subscribeOn(this.executionScheduler)
            .bufferUntil(timeStampItemOperationTuple -> {
                long timestamp = timeStampItemOperationTuple.getT1();
                CosmosItemOperation itemOperation = timeStampItemOperationTuple.getT2();

                logger.trace(
                    "BufferUntil - enqueued {}, {}, Context: {} {}",
                    timestamp,
                    getItemOperationDiagnostics(itemOperation),
                    this.operationContextText,
                    getThreadInfo());

                if (itemOperation == FlushBuffersItemOperation.singleton()) {
                    long currentMicroBatchSizeSnapshot = currentMicroBatchSize.get();
                    if (currentMicroBatchSizeSnapshot > 0) {
                        logger.trace(
                            "Flushing PKRange {} (batch size: {}) due to FlushItemOperation, Context: {} {}",
                            thresholds.getPartitionKeyRangeId(),
                            currentMicroBatchSizeSnapshot,
                            this.operationContextText,
                            getThreadInfo());

                        firstRecordTimeStamp.set(-1);
                        currentMicroBatchSize.set(0);
                        currentTotalSerializedLength.set(0);

                        return true;
                    }

                    // avoid counting flush operations for the micro batch size calculation
                    return false;
                }

                firstRecordTimeStamp.compareAndSet(-1, timestamp);
                long age = timestamp - firstRecordTimeStamp.get();
                long batchSize = currentMicroBatchSize.incrementAndGet();
                int totalSerializedLength = this.calculateTotalSerializedLength(currentTotalSerializedLength, itemOperation);

                if (batchSize >= thresholds.getTargetMicroBatchSizeSnapshot() ||
                    age >= this.maxMicroBatchIntervalInMs ||
                    totalSerializedLength >= this.maxMicroBatchPayloadSizeInBytes) {

                    logDebugOrWarning(
                        "BufferUntil - Flushing PKRange {} due to BatchSize ({}), payload size ({}) or age ({}), " +
                            "Triggering {}, Context: {} {}",
                        thresholds.getPartitionKeyRangeId(),
                        batchSize,
                        totalSerializedLength,
                        age,
                        getItemOperationDiagnostics(itemOperation),
                        this.operationContextText,
                        getThreadInfo());
                    firstRecordTimeStamp.set(-1);
                    currentMicroBatchSize.set(0);
                    currentTotalSerializedLength.set(0);
                    return true;
                }

                return false;
            })
            .flatMap(
                (List<Tuple2<Long, CosmosItemOperation>> timeStampAndItemOperationTuples) -> {
                    List<CosmosItemOperation> operations = new ArrayList<>(timeStampAndItemOperationTuples.size());
                    for (Tuple2<Long, CosmosItemOperation> timeStampAndItemOperationTuple :
                        timeStampAndItemOperationTuples) {

                        CosmosItemOperation itemOperation = timeStampAndItemOperationTuple.getT2();
                        if (itemOperation == FlushBuffersItemOperation.singleton()) {
                            continue;
                        }
                        operations.add(itemOperation);
                    }

                    logDebugOrWarning(
                        "Flushing PKRange {} micro batch with {} operations,  Context: {} {}",
                        thresholds.getPartitionKeyRangeId(),
                        operations.size(),
                        this.operationContextText,
                        getThreadInfo());

                    return executeOperations(operations, thresholds, groupSink);
                },
                this.cosmosBulkExecutionOptions.getMaxMicroBatchConcurrency());
    }

    private int calculateTotalSerializedLength(AtomicInteger currentTotalSerializedLength, CosmosItemOperation item) {
        if (item instanceof CosmosItemOperationBase) {
            return currentTotalSerializedLength.accumulateAndGet(
                ((CosmosItemOperationBase) item).getSerializedLength(this.effectiveItemSerializer),
                Integer::sum);
        }

        return currentTotalSerializedLength.get();
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executeOperations(
        List<CosmosItemOperation> operations,
        PartitionScopeThresholds thresholds,
        FluxSink<CosmosItemOperation> groupSink) {

        if (operations.size() == 0) {
            logger.trace("Empty operations list, Context: {}", this.operationContextText);
            return Flux.empty();
        }

        String pkRange = thresholds.getPartitionKeyRangeId();
        ServerOperationBatchRequest serverOperationBatchRequest =
            BulkExecutorUtil.createBatchRequest(operations, pkRange, this.maxMicroBatchPayloadSizeInBytes, this.effectiveItemSerializer);
        if (serverOperationBatchRequest.getBatchPendingOperations().size() > 0) {
            serverOperationBatchRequest.getBatchPendingOperations().forEach(groupSink::next);
        }

        return Flux.just(serverOperationBatchRequest.getBatchRequest())
            .publishOn(this.executionScheduler)
            .flatMap((PartitionKeyRangeServerBatchRequest serverRequest) ->
                this.executePartitionKeyRangeServerBatchRequest(serverRequest, groupSink, thresholds));
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executePartitionKeyRangeServerBatchRequest(
        PartitionKeyRangeServerBatchRequest serverRequest,
        FluxSink<CosmosItemOperation> groupSink,
        PartitionScopeThresholds thresholds) {

        return this.executeBatchRequest(serverRequest)
            .subscribeOn(this.executionScheduler)
            .flatMapMany(response -> {

                if (diagnosticsTracker != null && response.getDiagnostics() != null) {
                    diagnosticsTracker.trackDiagnostics(response.getDiagnostics().getDiagnosticsContext());
                }

                return Flux
                    .fromIterable(response.getResults())
                    .publishOn(this.executionScheduler)
                    .flatMap((CosmosBatchOperationResult result) ->
                    handleTransactionalBatchOperationResult(response, result, groupSink, thresholds));
            })
            .onErrorResume((Throwable throwable) -> {

                if (!(throwable instanceof Exception)) {
                    throw Exceptions.propagate(throwable);
                }

                Exception exception = (Exception) throwable;

                return Flux
                    .fromIterable(serverRequest.getOperations())
                    .publishOn(this.executionScheduler)
                    .flatMap((CosmosItemOperation itemOperation) ->
                        handleTransactionalBatchExecutionException(itemOperation, exception, groupSink, thresholds));
            });
    }

    // Helper functions
    private Mono<CosmosBulkOperationResponse<TContext>> handleTransactionalBatchOperationResult(
        CosmosBatchResponse response,
        CosmosBatchOperationResult operationResult,
        FluxSink<CosmosItemOperation> groupSink,
        PartitionScopeThresholds thresholds) {

        CosmosBulkItemResponse cosmosBulkItemResponse = ModelBridgeInternal
            .createCosmosBulkItemResponse(operationResult, response);
        CosmosItemOperation itemOperation = operationResult.getOperation();
        TContext actualContext = this.getActualContext(itemOperation);

        logDebugOrWarning(
            "HandleTransactionalBatchOperationResult - PKRange {}, Response Status Code {}, " +
                "Operation Status Code, {}, {}, Context: {} {}",
            thresholds.getPartitionKeyRangeId(),
            response.getStatusCode(),
            operationResult.getStatusCode(),
            getItemOperationDiagnostics(itemOperation),
            this.operationContextText,
            getThreadInfo());

        if (!operationResult.isSuccessStatusCode()) {

            if (itemOperation instanceof ItemBulkOperation<?, ?>) {

                ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) itemOperation;
                return itemBulkOperation.getRetryPolicy().shouldRetry(operationResult).flatMap(
                    result -> {
                        if (result.shouldRetry) {
                            logDebugOrWarning(
                                "HandleTransactionalBatchOperationResult - enqueue retry, PKRange {}, Response " +
                                    "Status Code {}, Operation Status Code, {}, {}, Context: {} {}",
                                thresholds.getPartitionKeyRangeId(),
                                response.getStatusCode(),
                                operationResult.getStatusCode(),
                                getItemOperationDiagnostics(itemOperation),
                                this.operationContextText,
                                getThreadInfo());
                            return this.enqueueForRetry(result.backOffTime, groupSink, itemOperation, thresholds);
                        } else {
                            // reduce log noise level for commonly expected/normal status codes
                            if (response.getStatusCode() == HttpConstants.StatusCodes.CONFLICT ||
                                response.getStatusCode() == HttpConstants.StatusCodes.PRECONDITION_FAILED) {

                                logDebugOrWarning(
                                    "HandleTransactionalBatchOperationResult - Fail, PKRange {}, Response Status " +
                                        "Code {}, Operation Status Code {}, {}, Context: {} {}",
                                    thresholds.getPartitionKeyRangeId(),
                                    response.getStatusCode(),
                                    operationResult.getStatusCode(),
                                    getItemOperationDiagnostics(itemOperation),
                                    this.operationContextText,
                                    getThreadInfo());
                            } else {
                                logger.error(
                                    "HandleTransactionalBatchOperationResult - Fail, PKRange {}, Response Status " +
                                        "Code {}, Operation Status Code {}, {}, Context: {} {}",
                                    thresholds.getPartitionKeyRangeId(),
                                    response.getStatusCode(),
                                    operationResult.getStatusCode(),
                                    getItemOperationDiagnostics(itemOperation),
                                    this.operationContextText,
                                    getThreadInfo());
                            }
                            return Mono.just(ModelBridgeInternal.createCosmosBulkOperationResponse(
                                itemOperation, cosmosBulkItemResponse, actualContext));
                        }
                    });

            } else {
                throw new UnsupportedOperationException("Unknown CosmosItemOperation.");
            }
        }

        thresholds.recordSuccessfulOperation();
        return Mono.just(ModelBridgeInternal.createCosmosBulkOperationResponse(
            itemOperation,
            cosmosBulkItemResponse,
            actualContext));
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

    private Mono<CosmosBulkOperationResponse<TContext>> handleTransactionalBatchExecutionException(
        CosmosItemOperation itemOperation,
        Exception exception,
        FluxSink<CosmosItemOperation> groupSink,
        PartitionScopeThresholds thresholds) {

        logDebugOrWarning(
            "HandleTransactionalBatchExecutionException, PKRange {}, Error: {}, {}, Context: {} {}",
            thresholds.getPartitionKeyRangeId(),
            exception,
            getItemOperationDiagnostics(itemOperation),
            this.operationContextText,
            getThreadInfo());

        if (exception instanceof CosmosException && itemOperation instanceof ItemBulkOperation<?, ?>) {
            CosmosException cosmosException = (CosmosException) exception;
            ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) itemOperation;

            // First check if it failed due to split, so the operations need to go in a different pk range group. So
            // add it in the mainSink.

            return itemBulkOperation.getRetryPolicy()
                .shouldRetryForGone(cosmosException.getStatusCode(), cosmosException.getSubStatusCode(), itemBulkOperation, cosmosException)
                .flatMap(shouldRetryGone -> {
                    if (shouldRetryGone) {
                        logDebugOrWarning(
                            "HandleTransactionalBatchExecutionException - Retry due to split, PKRange {}, Error: " +
                                "{}, {}, Context: {} {}",
                            thresholds.getPartitionKeyRangeId(),
                            exception,
                            getItemOperationDiagnostics(itemOperation),
                            this.operationContextText,
                            getThreadInfo());
                        // retry - but don't mark as enqueued for retry in thresholds
                        mainSink.emitNext(itemOperation, serializedEmitFailureHandler);
                        return Mono.empty();
                    } else {
                        logDebugOrWarning(
                            "HandleTransactionalBatchExecutionException - Retry other, PKRange {}, Error: " +
                                "{}, {}, Context: {} {}",
                            thresholds.getPartitionKeyRangeId(),
                            exception,
                            getItemOperationDiagnostics(itemOperation),
                            this.operationContextText,
                            getThreadInfo());
                        return retryOtherExceptions(
                            itemOperation,
                            exception,
                            groupSink,
                            cosmosException,
                            itemBulkOperation,
                            thresholds);
                    }
                });
        }

        TContext actualContext = this.getActualContext(itemOperation);
        return Mono.just(ModelBridgeInternal.createCosmosBulkOperationResponse(itemOperation, exception, actualContext));
    }

    private Mono<CosmosBulkOperationResponse<TContext>> enqueueForRetry(
        Duration backOffTime,
        FluxSink<CosmosItemOperation> groupSink,
        CosmosItemOperation itemOperation,
        PartitionScopeThresholds thresholds) {

        thresholds.recordEnqueuedRetry();
        if (backOffTime == null || backOffTime.isZero()) {
            groupSink.next(itemOperation);
            return Mono.empty();
        } else {
            return Mono
                .delay(backOffTime)
                .flatMap((dummy) -> {
                    groupSink.next(itemOperation);
                    return Mono.empty();
                });
        }
    }

    private Mono<CosmosBulkOperationResponse<TContext>> retryOtherExceptions(
        CosmosItemOperation itemOperation,
        Exception exception,
        FluxSink<CosmosItemOperation> groupSink,
        CosmosException cosmosException,
        ItemBulkOperation<?, ?> itemBulkOperation,
        PartitionScopeThresholds thresholds) {

        TContext actualContext = this.getActualContext(itemOperation);
        return itemBulkOperation.getRetryPolicy().shouldRetry(cosmosException).flatMap(result -> {
            if (result.shouldRetry) {
                return this.enqueueForRetry(result.backOffTime, groupSink, itemBulkOperation, thresholds);
            } else {
                return Mono.just(ModelBridgeInternal.createCosmosBulkOperationResponse(
                    itemOperation, exception, actualContext));
            }
        });
    }

    private Mono<CosmosBatchResponse> executeBatchRequest(PartitionKeyRangeServerBatchRequest serverRequest) {
        RequestOptions options = new RequestOptions();
        options.setThroughputControlGroupName(cosmosBulkExecutionOptions.getThroughputControlGroupName());
        options.setExcludedRegions(cosmosBulkExecutionOptions.getExcludedRegions());

        //  This logic is to handle custom bulk options which can be passed through encryption or through some other project
        Map<String, String> customOptions = cosmosBulkExecutionOptions.getHeaders();
        if (customOptions != null && !customOptions.isEmpty()) {
            for(Map.Entry<String, String> entry : customOptions.entrySet()) {
                options.setHeader(entry.getKey(), entry.getValue());
            }
        }
        options.setOperationContextAndListenerTuple(operationListener);

        // The request options here are used for the BulkRequest exchanged with the service
        // If contentResponseOnWrite is not enabled here (or at the client level) the
        // service will not even send a bulk response payload - so all the
        // CosmosBulItemRequestOptions are irrelevant - all payloads will be null
        // Instead we should automatically enforce contentResponseOnWrite for all
        // bulk requests whenever at least one of the item operations requires a content response (either
        // because it is a read operation or because contentResponseOnWrite was enabled explicitly)
        if (!this.docClientWrapper.isContentResponseOnWriteEnabled() &&
            serverRequest.getOperations().size() > 0) {

            for (CosmosItemOperation itemOperation : serverRequest.getOperations()) {
                if (itemOperation instanceof ItemBulkOperation<?, ?>) {

                    ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) itemOperation;
                    if (itemBulkOperation.getOperationType() == CosmosItemOperationType.READ ||
                        (itemBulkOperation.getRequestOptions() != null &&
                            itemBulkOperation.getRequestOptions().isContentResponseOnWriteEnabled() != null &&
                            itemBulkOperation.getRequestOptions().isContentResponseOnWriteEnabled())) {

                        options.setContentResponseOnWriteEnabled(true);
                        break;
                    }
                }
            }
        }

        return withContext(context -> {
            final Mono<CosmosBatchResponse> responseMono = this.docClientWrapper.executeBatchRequest(
                BridgeInternal.getLink(this.container), serverRequest, options, false);

            return clientAccessor.getDiagnosticsProvider(this.cosmosClient)
                .traceEnabledBatchResponsePublisher(
                    responseMono,
                    context,
                    this.bulkSpanName,
                    this.container.getDatabase().getId(),
                    this.container.getId(),
                    this.cosmosClient,
                    options.getConsistencyLevel(),
                    OperationType.Batch,
                    ResourceType.Document,
                    options,
                    this.cosmosBulkExecutionOptions.getMaxMicroBatchSize());
        });
    }

    private void completeAllSinks() {
        logInfoOrWarning("Closing all sinks, Context: {}", this.operationContextText);

        logger.debug("Executor service shut down, Context: {}", this.operationContextText);
        mainSink.emitComplete(serializedCompleteEmitFailureHandler);

        this.shutdown();
    }

    private void onFlush() {
        try {
            this.groupSinks.forEach(sink -> sink.next(FlushBuffersItemOperation.singleton()));
        } catch(Throwable t) {
            logger.error("Callback invocation 'onFlush' failed. Context: {}", this.operationContextText,  t);
        }
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

    private static class SerializedEmitFailureHandler implements Sinks.EmitFailureHandler {

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED)) {
                logger.debug("SerializedEmitFailureHandler.onEmitFailure - Signal:{}, Result: {}", signalType, emitResult);

                return true;
            }

            logger.error("SerializedEmitFailureHandler.onEmitFailure - Signal:{}, Result: {}", signalType, emitResult);
            return false;
        }
    }

    private static class SerializedCompleteEmitFailureHandler implements Sinks.EmitFailureHandler {

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED)) {
                logger.debug("SerializedCompleteEmitFailureHandler.onEmitFailure - Signal:{}, Result: {}", signalType, emitResult);

                return true;
            }

            if (emitResult == Sinks.EmitResult.FAIL_CANCELLED || emitResult == Sinks.EmitResult.FAIL_TERMINATED) {
                logger.debug("SerializedCompleteEmitFailureHandler.onEmitFailure - Main sink already completed, Signal:{}, Result: {}", signalType, emitResult);
                return false;
            }

            logger.error("SerializedCompleteEmitFailureHandler.onEmitFailure - Signal:{}, Result: {}", signalType, emitResult);
            return false;
        }
    }
}
