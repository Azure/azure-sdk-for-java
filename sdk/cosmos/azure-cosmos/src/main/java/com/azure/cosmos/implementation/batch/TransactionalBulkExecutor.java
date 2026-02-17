// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.CosmosTransactionalBulkExecutionOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.UUIDs;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.BridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import com.azure.cosmos.CosmosException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstants.DEFAULT_MAX_TRANSACTIONAL_BATCH_INTERVAL_AFTER_DRAINING_INCOMING_FLUX_IN_MILLISECONDS;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * The Core logic of bulk execution is here.
 *
 * The actual execution of the flux of operations. It is done in following steps:

 * 1. Getting partition key range ID and grouping operations using that id.
 * 2. For the flux of operations in a group, using two simple counters totalOperationsInFlight and totalBatchesInFlight and a flushSignalFlux to control concurrency.
 * 3. For the operation we get in after buffering, process it using a batch request and return
 *    a wrapper having request, response(if-any) and exception(if-any). Either response or exception will be there.
 *
 * 4. Any internal retry is done by adding in an intermediate sink for each grouped flux.
 * 5. Any operation which failed due to partition key range gone is retried by putting it in the main sink which leads
 *    to re-calculation of partition key range id.
 * 6. At the end and this is very essential, we close all the sinks as the sink continues to waits for more and the
 *    execution isn't finished even if all the operations have been executed(figured out by completion call of source)
 *
 **/
public final class TransactionalBulkExecutor implements Disposable {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBulkExecutor.class);
    private final static AtomicLong instanceCount = new AtomicLong(0);

    private static final ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.CosmosBatchRequestOptionsAccessor cosmosBatchRequestOptionsAccessor =
        ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.getCosmosBatchRequestOptionsAccessor();

    private static final ImplementationBridgeHelpers.CosmosBatchHelper.CosmosBatchAccessor cosmosBatchAccessor =
        ImplementationBridgeHelpers.CosmosBatchHelper.getCosmosBatchAccessor();

    private final CosmosAsyncContainer container;
    private final AsyncDocumentClient docClientWrapper;
    private final String operationContextText;
    private final OperationContextAndListenerTuple operationListener;
    private final Flux<CosmosBatchBulkOperation> inputBatches;

    private final CosmosTransactionalBulkExecutionOptionsImpl transactionalBulkExecutionOptionsImpl;

    // Partition thresholds map
    private final ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds;

    private final AtomicBoolean mainSourceCompleted = new AtomicBoolean(false);
    // Handle shutdown
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicInteger totalCount;
    private final String identifier = "TransactionalBulkExecutor-" + instanceCount.incrementAndGet();
    private final BulkExecutorDiagnosticsTracker diagnosticsTracker;
    private final CosmosItemSerializer effectiveItemSerializer;
    private final Scheduler executionScheduler;
    private final ThrottlingRetryOptions throttlingRetryOptions;

    private final static Sinks.EmitFailureHandler serializedEmitFailureHandler = new SerializedEmitFailureHandler();
    private final static Sinks.EmitFailureHandler serializedCompleteEmitFailureHandler = new SerializedCompleteEmitFailureHandler();
    private final Sinks.Many<CosmosBatchBulkOperation> mainSink;
    private final List<Sinks.Many<CosmosBatchBulkOperation>> groupSinks;
    private final List<Sinks.Many<Integer>> flushSignalGroupSinks;
    private final AtomicReference<Disposable> scheduledFutureForFlush;

    @SuppressWarnings({"unchecked"})
    public TransactionalBulkExecutor(
        CosmosAsyncContainer container,
        Flux<CosmosBatchBulkOperation> inputBatches,
        CosmosTransactionalBulkExecutionOptionsImpl transactionalBulkOptions) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(inputBatches, "expected non-null inputOperations");
        checkNotNull(transactionalBulkOptions, "expected non-null transactionalBulkOptions");

        this.transactionalBulkExecutionOptionsImpl = transactionalBulkOptions;
        this.container = container;
        this.inputBatches = inputBatches;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.effectiveItemSerializer = this.docClientWrapper.getEffectiveItemSerializer(transactionalBulkOptions.getCustomItemSerializer());
        this.operationListener = transactionalBulkExecutionOptionsImpl.getOperationContextAndListenerTuple();
        if (operationListener != null &&
            operationListener.getOperationContext() != null) {
            operationContextText = identifier + "[" + operationListener.getOperationContext().toString() + "]";
        } else {
            operationContextText = identifier +"[n/a]";
        }

        this.diagnosticsTracker = transactionalBulkExecutionOptionsImpl.getDiagnosticsTracker();
        this.throttlingRetryOptions = docClientWrapper.getConnectionPolicy().getThrottlingRetryOptions();

        this.totalCount = new AtomicInteger(0);

        // Initialize main sinks for rerouting batches on partition changes
        this.mainSink = Sinks.many().unicast().onBackpressureBuffer();
        // Initialize group sinks for retriable exceptions
        this.groupSinks = new CopyOnWriteArrayList<>();
        // Initialize flush signal group sinks for flush batch signals
        this.flushSignalGroupSinks = new CopyOnWriteArrayList<>();

        // Initialize partition thresholds map and default options for thresholds.
        this.partitionScopeThresholds = new ConcurrentHashMap<>();

        Scheduler schedulerSnapshotFromOptions = transactionalBulkOptions.getSchedulerOverride();
        this.executionScheduler = schedulerSnapshotFromOptions != null
            ? schedulerSnapshotFromOptions
            : CosmosSchedulers.TRANSACTIONAL_BULK_EXECUTOR_BOUNDED_ELASTIC;

        // setup this background task which will try to emit flush signal,
        // A safeguard to prevent the pipeline got stuck in case when a cosmosBatch completes,
        // the flush signal is not issued successfully or missed
        int flushInterval = Configs.getBulkTransactionalBatchFlushIntervalInMs();
        this.scheduledFutureForFlush = new AtomicReference<>(
            CosmosSchedulers
                .TRANSACTIONAL_BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC
                .schedulePeriodically(
                    this::onFlush,
                    flushInterval,
                    flushInterval,
                    TimeUnit.MILLISECONDS));

        logger.info("Instantiated TransactionalBulkExecutor, Context: {}", this.operationContextText);
    }

    @Override
    public void dispose() {
        if (this.isDisposed.compareAndSet(false, true)) {
            logDebugOrWarning("Transactional bulk executor is disposed");
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
        long flushIntervalAfterDrainingIncomingFlux =
            DEFAULT_MAX_TRANSACTIONAL_BATCH_INTERVAL_AFTER_DRAINING_INCOMING_FLUX_IN_MILLISECONDS;

        Disposable newFlushTask = initializeAggressiveFlush
            ? CosmosSchedulers
            .TRANSACTIONAL_BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC
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
                logDebugOrWarning("Cancelled all future scheduled flush tasks {}, Context: {}", getThreadInfo(), this.operationContextText);
            } catch (Exception e) {
                logger.warn("Failed to cancel scheduled flush tasks{}, Context: {}", getThreadInfo(), this.operationContextText, e);
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

            this.cancelFlushTask(false);

            // Complete all flush group sinks so any waiting subscribers can finish
            try {
                flushSignalGroupSinks.forEach(Sinks.Many::tryEmitComplete);
                logger.debug("All flush group sinks completed, Context: {}", this.operationContextText);
            } catch (Throwable t) {
                logger.warn("Error completing flush group sinks, Context: {}", this.operationContextText, t);
            }

            // Complete all group sinks so any waiting subscribers can finish
            try {
                groupSinks.forEach(Sinks.Many::tryEmitComplete);
                logger.debug("All group sinks completed, Context: {}", this.operationContextText);
            } catch (Throwable t) {
                logger.warn("Error completing group sinks, Context: {}", this.operationContextText, t);
            }

            logger.debug("Shutdown complete, Context: {}", this.operationContextText);
        }
    }

    public Flux<CosmosBulkTransactionalBatchResponse> execute() {
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

    private Flux<CosmosBulkTransactionalBatchResponse> executeCore() {

        // For transactional batches,
        // resolve partition key range id per transactional batch and group by the partition
        // to allow dynamically adjust the concurrency based on the per-partition threshold
        Integer nullableMaxConcurrentCosmosPartitions = transactionalBulkExecutionOptionsImpl.getMaxConcurrentCosmosPartitions();
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
                    logDebugOrWarning("TransactionalBulkExecutor.execute with MaxConcurrentPartitions: {}, Context: {}",
                        maxConcurrentCosmosPartitions,
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
                        .doOnNext(cosmosBatchBulkOperation -> {
                            totalCount.incrementAndGet();

                            setRetryPolicyForTransactionalBatch(
                                docClientWrapper,
                                this.container,
                                cosmosBatchBulkOperation,
                                this.throttlingRetryOptions
                            );

                            logger.trace(
                                "SetRetryPolicy for cosmos batch, PkValue: {}, TotalCount: {}, Context: {}, {}",
                                cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
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
                                logInfoOrWarning("Getting complete signal, Total count is 0, close all sinks");
                                completeAllSinks();
                            } else {
                                this.cancelFlushTask(true);
                                this.onFlush();

                                logDebugOrWarning("Scheduled new flush operation {}, Context: {}", getThreadInfo(), this.operationContextText);
                            }
                        })
                        .mergeWith(mainSink.asFlux())
                        .subscribeOn(this.executionScheduler)
                        .flatMap(cosmosBatchBulkOperation -> {
                            logger.trace("Before Resolve PkRangeId, PkValue: {}, OpCount: {}, Context: {} {}",
                                cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                                cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
                                this.operationContextText,
                                getThreadInfo());

                            // resolve partition key range id and attach PartitionScopeThresholds
                            return resolvePartitionKeyRangeIdForBatch(cosmosBatchBulkOperation)
                                .map(pkRangeId -> {
                                    PartitionScopeThresholds thresholds =
                                        this.partitionScopeThresholds.computeIfAbsent(
                                            pkRangeId,
                                            newPkRangeId -> new PartitionScopeThresholds(pkRangeId, this.transactionalBulkExecutionOptionsImpl));

                                    logTraceOrWarning("Resolved PkRangeId: {}, PkValue: {}, OpCount: {}, Context: {} {}",
                                        pkRangeId,
                                        cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                                        cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
                                        this.operationContextText,
                                        getThreadInfo());

                                    return Pair.of(thresholds, cosmosBatchBulkOperation);
                                });
                        })
                        .groupBy(Pair::getKey, Pair::getValue)
                        .flatMap(this::executePartitionedGroupTransactional, maxConcurrentCosmosPartitions)
                        .subscribeOn(this.executionScheduler)
                        .doOnNext(response -> doOnResponseOrError())
                        .doOnError(throwable -> doOnResponseOrError())
                        .doOnComplete(() -> {
                            int totalCountSnapshot = totalCount.get();
                            boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
                            if (totalCountSnapshot == 0 && mainSourceCompletedSnapshot) {
                                // It is possible that count is zero but there are more elements in the source.
                                // Count 0 also signifies that there are no pending elements in any sink.
                                logInfoOrWarning(
                                    "DoOnComplete: All work completed, Context: {} {}",
                                    this.operationContextText,
                                    getThreadInfo());
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

    private void doOnResponseOrError() {
        int totalCountAfterDecrement = totalCount.decrementAndGet();
        boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
        if (totalCountAfterDecrement == 0 && mainSourceCompletedSnapshot) {
            // It is possible that count is zero but there are more elements in the source.
            // Count 0 also signifies that there are no pending elements in any sink.
            logInfoOrWarning("All work completed, TotalCount: {}, Context: {} {}",
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
            logTraceOrWarning(
                "Work left - TotalCount after decrement: {}, main sink completed {}, Context: {} {}",
                totalCountAfterDecrement,
                mainSourceCompletedSnapshot,
                this.operationContextText,
                getThreadInfo());
        }
    }

    private Flux<CosmosBulkTransactionalBatchResponse> executePartitionedGroupTransactional(
        GroupedFlux<PartitionScopeThresholds, CosmosBatchBulkOperation> partitionedGroupFluxOfBatches) {

        final PartitionScopeThresholds thresholds = partitionedGroupFluxOfBatches.key();

        final Sinks.Many<CosmosBatchBulkOperation> groupSink = Sinks.many().unicast().onBackpressureBuffer();
        final Flux<CosmosBatchBulkOperation> groupFlux = groupSink.asFlux();
        groupSinks.add(groupSink);

        Sinks.Many<Integer> flushSignalGroupSink = Sinks.many().multicast().directBestEffort();
        Flux<Integer> flushSignalGroupFlux = flushSignalGroupSink.asFlux().share();
        flushSignalGroupSinks.add(flushSignalGroupSink);

        AtomicInteger totalOperationsInFlight = new AtomicInteger(0);
        AtomicInteger totalBatchesInFlight = new AtomicInteger(0);

        return partitionedGroupFluxOfBatches
            .mergeWith(groupFlux)
            .publishOn(this.executionScheduler)
            .concatMap(cosmosBatchBulkOperation -> {
                // using concatMap here for a sequential processing
                // this part is to decide whether the cosmos batch can be flushed to downstream for processing
                // based on the per-partition threshold and concurrency config
                return Mono.defer(() -> {
                    if (canFlushCosmosBatch(
                        totalOperationsInFlight,
                        totalBatchesInFlight,
                        thresholds,
                        cosmosBatchBulkOperation)) {

                        return Mono.just(cosmosBatchBulkOperation);
                    }

                    // there is no capacity for new cosmos batch to be executed currently
                    // wait for flush signal
                    // the flush signal can either come from when a cosmos batch has completed or the background flush task
                    return flushSignalGroupFlux
                        .filter((flushSignal) ->
                            canFlushCosmosBatch(
                                totalOperationsInFlight,
                                totalBatchesInFlight,
                                thresholds,
                                cosmosBatchBulkOperation))
                        .next()
                        .then();
                })
                .then(Mono.defer(() -> {
                    totalOperationsInFlight.addAndGet(cosmosBatchBulkOperation.getCosmosBatch().getOperations().size());
                    totalBatchesInFlight.incrementAndGet();
                    logTraceOrWarning(
                        "Flush cosmos batch, PKRangeId: {}, PkValue: {}, TotalOpsInFlight: {}, TotalBatchesInFlight: {}, BatchOpCount: {}, Context: {} {}",
                        thresholds.getPartitionKeyRangeId(),
                        cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                        totalOperationsInFlight.get(),
                        totalBatchesInFlight.get(),
                        cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
                        this.operationContextText,
                        getThreadInfo());

                    return Mono.just(cosmosBatchBulkOperation);
                }));
            })
            .flatMap(cosmosBatchBulkOperation ->
                this.executeTransactionalBatchWithThresholds(
                    cosmosBatchBulkOperation,
                    thresholds,
                    groupSink,
                    flushSignalGroupSink,
                    totalBatchesInFlight,
                    totalOperationsInFlight));
    }

    private boolean canFlushCosmosBatch(
        AtomicInteger totalOperationsInFlight,
        AtomicInteger totalConcurrentBatchesInFlight,
        PartitionScopeThresholds partitionScopeThresholds,
        CosmosBatchBulkOperation cosmosBatchBulkOperation) {

        int targetBatchSizeSnapshot = partitionScopeThresholds.getTargetMicroBatchSizeSnapshot();
        int totalOpsInFlightSnapshot = totalOperationsInFlight.get();
        int totalBatchesInFlightSnapshot = totalConcurrentBatchesInFlight.get();

        boolean canFlush = (cosmosBatchBulkOperation.getCosmosBatch().getOperations().size() + totalOpsInFlightSnapshot <= targetBatchSizeSnapshot)
            || (totalBatchesInFlightSnapshot <= 0);

        logTraceOrWarning(
            "canFlushCosmosBatch - PkRangeId: {}, PkValue: {}, TargetBatchSize {}, TotalOpsInFlight: {}, TotalBatchesInFlight: {}, BatchOpCount: {}, CanFlush {}, Context: {} {}",
            partitionScopeThresholds.getPartitionKeyRangeId(),
            cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
            targetBatchSizeSnapshot,
            totalOpsInFlightSnapshot,
            totalBatchesInFlightSnapshot,
            cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
            canFlush,
            this.operationContextText,
            getThreadInfo());

        return canFlush;
    }

    private void setRetryPolicyForTransactionalBatch(
        AsyncDocumentClient docClientWrapper,
        CosmosAsyncContainer container,
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        ThrottlingRetryOptions throttlingRetryOptions) {

        ResourceThrottleRetryPolicy resourceThrottleRetryPolicy = new ResourceThrottleRetryPolicy(
            throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
            throttlingRetryOptions.getMaxRetryWaitTime(),
            false);

        TransactionalBatchRetryPolicy retryPolicy = new TransactionalBatchRetryPolicy(
            docClientWrapper.getCollectionCache(),
            docClientWrapper.getPartitionKeyRangeCache(),
            BridgeInternal.getLink(container),
            resourceThrottleRetryPolicy);

        cosmosBatchAccessor.setRetryPolicy(cosmosBatchBulkOperation.getCosmosBatch(), retryPolicy);
    }

    private Mono<CosmosBulkTransactionalBatchResponse> enqueueForRetry(
        Duration backOffTime,
        Sinks.Many<CosmosBatchBulkOperation> groupSink,
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        PartitionScopeThresholds thresholds,
        String batchTrackingId) {

        // Record an enqueued retry for threshold adjustments
        this.recordResponseForRetryInThreshold(cosmosBatchBulkOperation, thresholds);

        if (backOffTime == null || backOffTime.isZero()) {
            logDebugOrWarning(
                "enqueueForRetry - Retry in group sink for PkRangeId: {}, PkValue: {}, Batch trackingId: {}, Context: {} {}",
                thresholds.getPartitionKeyRangeId(),
                cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                batchTrackingId,
                this.operationContextText,
                getThreadInfo());

            groupSink.emitNext(cosmosBatchBulkOperation, serializedEmitFailureHandler);
            return Mono.empty();
        } else {
            logDebugOrWarning(
                "enqueueForRetry - Retry in group sink for PkRangeId: {}, PkValue: {}, BackoffTime: {}, Batch trackingId: {}, Context: {} {}",
                thresholds.getPartitionKeyRangeId(),
                cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                backOffTime,
                batchTrackingId,
                this.operationContextText,
                getThreadInfo());

            return Mono
                .delay(backOffTime)
                .flatMap((dummy) -> {
                    groupSink.emitNext(cosmosBatchBulkOperation, serializedCompleteEmitFailureHandler);
                    return Mono.empty();
                });
        }
    }

    private Mono<CosmosBulkTransactionalBatchResponse> executeTransactionalBatchWithThresholds(
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        PartitionScopeThresholds thresholds,
        Sinks.Many<CosmosBatchBulkOperation> groupSink,
        Sinks.Many<Integer> flushSignalGroupSink,
        AtomicInteger totalBatchesInFlight,
        AtomicInteger totalOperationsInFlight) {

        String batchTrackingId = UUIDs.nonBlockingRandomUUID().toString();

        logTraceOrWarning(
            "executeTransactionalBatchWithThresholds - PkRangeId: {}, PkValue: {}, BatchOpCount:{}, TrackingId: {}, Context: {} {}",
            thresholds.getPartitionKeyRangeId(),
            cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
            cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
            batchTrackingId,
            this.operationContextText,
            getThreadInfo());

        CosmosBatchRequestOptions batchRequestOptions = getBatchRequestOptions();

        return this.container
            .executeCosmosBatch(cosmosBatchBulkOperation.getCosmosBatch(), batchRequestOptions)
            .publishOn(this.executionScheduler)
            .flatMap(response -> {
                logTraceOrWarning(
                    "Response for transactional batch - PkRangeId: {}, PkValue: {}, BatchOpCount: {}, ResponseOpCount: {}, StatusCode: {}, SubStatusCode: {}, ActivityId: {}, Batch trackingId {}, Context: {} {}",
                    thresholds.getPartitionKeyRangeId(),
                    cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                    cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
                    response.getResults().size(),
                    response.getStatusCode(),
                    response.getSubStatusCode(),
                    response.getActivityId(),
                    batchTrackingId,
                    this.operationContextText,
                    getThreadInfo());

                if (diagnosticsTracker != null && response.getDiagnostics() != null) {
                    diagnosticsTracker.trackDiagnostics(response.getDiagnostics().getDiagnosticsContext());
                }

                cosmosBatchBulkOperation.getStatusTracker().recordStatusCode(
                    response.getStatusCode(),
                    response.getSubStatusCode());

                if (response.isSuccessStatusCode()) {
                    recordSuccessfulResponseInThreshold(cosmosBatchBulkOperation, thresholds);
                    return Mono.just(
                        new CosmosBulkTransactionalBatchResponse(
                            cosmosBatchBulkOperation,
                            response,
                            null)
                    );
                }

                return handleUnsuccessfulResponse(thresholds, batchTrackingId, cosmosBatchBulkOperation, response, groupSink);
            })
            .onErrorResume(throwable -> {
                if (!(throwable instanceof Exception)) {
                    return Mono.error(Exceptions.propagate(throwable));
                }

                Exception exception = (Exception) throwable;
                return this.handleTransactionalBatchExecutionException(
                    cosmosBatchBulkOperation,
                    exception,
                    groupSink,
                    thresholds,
                    batchTrackingId);
            })
            .doFinally(signalType -> {
                int totalOpsInFlightSnapshot = totalOperationsInFlight.addAndGet(-cosmosBatchBulkOperation.getCosmosBatch().getOperations().size());
                int totalBatchesInFlightSnapshot = totalBatchesInFlight.decrementAndGet();
                flushSignalGroupSink.emitNext(1, serializedEmitFailureHandler);
                logTraceOrWarning(
                    "CosmosBatch completed, emit flush signal - SignalType: {}, PkRangeId: {}, PkValue: {}, BatchOpCount: {}, TotalOpsInFlight: {}, TotalBatchesInFlight: {}, Context: {} {}",
                    signalType,
                    thresholds.getPartitionKeyRangeId(),
                    cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                    cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
                    totalOpsInFlightSnapshot,
                    totalBatchesInFlightSnapshot,
                    this.operationContextText,
                    getThreadInfo());
            })
            .subscribeOn(this.executionScheduler);
    }

    private void recordSuccessfulResponseInThreshold(CosmosBatchBulkOperation cosmosBatchBulkOperation, PartitionScopeThresholds thresholds) {
        for (int i = 0; i < cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(); i++) {
            thresholds.recordSuccessfulOperation();
        }
    }

    private void recordResponseForRetryInThreshold(CosmosBatchBulkOperation cosmosBatchBulkOperation, PartitionScopeThresholds thresholds) {
        for (int i = 0; i < cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(); i++) {
            thresholds.recordEnqueuedRetry();
        }
    }

    private Mono<CosmosBulkTransactionalBatchResponse> handleUnsuccessfulResponse(
        PartitionScopeThresholds thresholds,
        String batchTrackingId,
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        CosmosBatchResponse response,
        Sinks.Many<CosmosBatchBulkOperation> groupSink) {

        logDebugOrWarning(
            "handleUnsuccessfulResponse - PkRangeId: {}, PkValue: {}, BatchOpCount: {}, StatusCode {}, SubStatusCode {}, Batch trackingId {}, Context: {} {}",
            thresholds.getPartitionKeyRangeId(),
            cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
            cosmosBatchBulkOperation.getCosmosBatch().getOperations().size(),
            response.getStatusCode(),
            response.getSubStatusCode(),
            batchTrackingId,
            this.operationContextText,
            getThreadInfo());

        // Create CosmosException for retry policy to understand:
        CosmosException exception = BridgeInternal.createCosmosException(
            null,
            response.getStatusCode(),
            null,
            BulkExecutorUtil.getResponseHeadersFromBatchOperationResult(response));
        BridgeInternal.setSubStatusCode(exception, response.getSubStatusCode());

        return this.handleTransactionalBatchExecutionException(cosmosBatchBulkOperation, exception, groupSink, thresholds, batchTrackingId)
            .onErrorResume(throwable -> {
                logDebugOrWarning(
                    "handleUnsuccessfulResponse - Can not be retried. PkRangeId: {}, PkValue: {}, Batch trackingId {}, Context: {} {}",
                    thresholds.getPartitionKeyRangeId(),
                    cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                    batchTrackingId,
                    this.operationContextText,
                    getThreadInfo(),
                    throwable);

                return Mono.just(
                    new CosmosBulkTransactionalBatchResponse(
                        cosmosBatchBulkOperation,
                        response,
                        null
                    )
                ); // the operation can not be retried, return the original response
            });
    }

    private Mono<CosmosBulkTransactionalBatchResponse> handleTransactionalBatchExecutionException(
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        Exception exception,
        Sinks.Many<CosmosBatchBulkOperation> groupSink,
        PartitionScopeThresholds thresholds,
        String batchTrackingId) {

        logDebugOrWarning(
            "HandleTransactionalBatchExecutionException - PkRangeId: {}, PkRangeValue: {}, Exception {}, Batch TrackingId {}, Context: {} {}",
            cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
            thresholds.getPartitionKeyRangeId(),
            exception,
            batchTrackingId,
            this.operationContextText,
            getThreadInfo());

        if (exception instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) exception;

            cosmosBatchBulkOperation.getStatusTracker().recordStatusCode(
                cosmosException.getStatusCode(),
                cosmosException.getSubStatusCode());

            return cosmosBatchAccessor
                .getRetryPolicy(cosmosBatchBulkOperation.getCosmosBatch())
                .shouldRetryInMainSink(cosmosException)
                .flatMap(shouldRetryInMainSink -> {
                    if (shouldRetryInMainSink) {
                        logDebugOrWarning(
                            "HandleTransactionalBatchExecutionException - Retry in main sink for PkRangeId: {}, PkValue: {}, Error {}, Batch TrackingId {}, Context: {} {}",
                            thresholds.getPartitionKeyRangeId(),
                            cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
                            exception,
                            batchTrackingId,
                            this.operationContextText,
                            getThreadInfo());

                        // retry - but don't mark as enqueued for retry in thresholds
                        mainSink.emitNext(cosmosBatchBulkOperation, serializedEmitFailureHandler); //TODO: validate booking marking for concurrent ops in flight
                        return Mono.empty();
                    } else {
                        return retryOtherExceptions(
                            cosmosBatchBulkOperation,
                            groupSink,
                            cosmosBatchAccessor.getRetryPolicy(cosmosBatchBulkOperation.getCosmosBatch()),
                            cosmosException,
                            thresholds,
                            batchTrackingId);
                    }
                });
        }

        // track for non-cosmos exception
        cosmosBatchBulkOperation
            .getStatusTracker()
            .recordStatusCode(-1, -1);

        return Mono.just(
            new CosmosBulkTransactionalBatchResponse(cosmosBatchBulkOperation, null, exception)
        );
    }

    private Mono<CosmosBulkTransactionalBatchResponse> retryOtherExceptions(
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        Sinks.Many<CosmosBatchBulkOperation> groupSink,
        TransactionalBatchRetryPolicy retryPolicy,
        CosmosException cosmosException,
        PartitionScopeThresholds thresholds,
        String batchTrackingId) {

        return retryPolicy.shouldRetry(cosmosException).flatMap(result -> {
            if (result.shouldRetry) {
                return this.enqueueForRetry(result.backOffTime, groupSink, cosmosBatchBulkOperation, thresholds, batchTrackingId);
            } else {
                return Mono.just(
                    new CosmosBulkTransactionalBatchResponse(
                        cosmosBatchBulkOperation,
                        null,
                        cosmosException
                    )
                );
            }
        });
    }

    private Mono<String> resolvePartitionKeyRangeIdForBatch(CosmosBatchBulkOperation cosmosBatchBulkOperation) {
        checkNotNull(cosmosBatchBulkOperation, "expected non-null cosmosBatchBulkOperation");

        return BulkExecutorUtil.resolvePartitionKeyRangeId(
            docClientWrapper,
            container,
            cosmosBatchBulkOperation.getCosmosBatch().getPartitionKeyValue(),
            null);
    }

    private CosmosBatchRequestOptions getBatchRequestOptions() {
        CosmosBatchRequestOptions batchRequestOptions = new CosmosBatchRequestOptions();
        batchRequestOptions.setExcludedRegions(transactionalBulkExecutionOptionsImpl.getExcludedRegions());
        batchRequestOptions.setKeywordIdentifiers(transactionalBulkExecutionOptionsImpl.getKeywordIdentifiers());
        cosmosBatchRequestOptionsAccessor
            .setThroughputControlGroupName(
                batchRequestOptions,
                transactionalBulkExecutionOptionsImpl.getThroughputControlGroupName());

        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicySnapshot =
            transactionalBulkExecutionOptionsImpl.getCosmosEndToEndLatencyPolicyConfig();
        if (e2eLatencyPolicySnapshot != null) {
            cosmosBatchRequestOptionsAccessor
                .setEndToEndOperationLatencyPolicyConfig(
                    batchRequestOptions,
                    e2eLatencyPolicySnapshot);
        }

        Map<String, String> customOptions = transactionalBulkExecutionOptionsImpl.getHeaders();
        if (customOptions != null && !customOptions.isEmpty()) {
            for(Map.Entry<String, String> entry : customOptions.entrySet()) {
                cosmosBatchRequestOptionsAccessor
                    .setHeader(batchRequestOptions, entry.getKey(), entry.getValue());
            }
        }
        cosmosBatchRequestOptionsAccessor
            .setOperationContextAndListenerTuple(batchRequestOptions, operationListener);

        batchRequestOptions.setCustomItemSerializer(this.effectiveItemSerializer);

        cosmosBatchRequestOptionsAccessor.setDisableRetryForThrottledBatchRequest(batchRequestOptions, true);

        return batchRequestOptions;
    }

    private void completeAllSinks() {
        logInfoOrWarning("Completing execution, Context: {}", this.operationContextText);

        try {
            mainSink.emitComplete(serializedCompleteEmitFailureHandler);
        } catch (Throwable t) {
            logger.warn("Failed to complete main sink, Context: {}", this.operationContextText, t);
        }

        this.shutdown();
    }

    private void onFlush() {
        try {
            logTraceOrWarning("onFlush - emitting flush signal for each group");
            this.flushSignalGroupSinks.forEach(sink -> sink.emitNext(1, serializedEmitFailureHandler));
        } catch(Throwable t) {
            logger.error("Callback invocation 'onFlush' failed. Context: {}", this.operationContextText,  t);
        }
    }

    private static class SerializedEmitFailureHandler implements Sinks.EmitFailureHandler {

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED)) {
                logger.debug(
                    "SerializedEmitFailureHandler.onEmitFailure, emit result {} - Signal:{}, Result: {}",
                    Sinks.EmitResult.FAIL_NON_SERIALIZED,
                    signalType,
                    emitResult);

                return true;
            }

            if (emitResult.equals((Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER))) {
                // For flushSignalGroupSink which is a Sinks.Many.Multicast, when this happens, it means there is no active subscriber
                // this can happen usually at the end of the execution when all the operations have flushed
                logger.trace(
                    "SerializedEmitFailureHandler.onEmitFailure, emit result {} - Signal:{}, Result: {}",
                    Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER,
                    signalType,
                    emitResult);

                return false;
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
