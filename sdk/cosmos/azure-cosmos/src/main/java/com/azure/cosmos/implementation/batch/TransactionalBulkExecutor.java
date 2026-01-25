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
import com.azure.cosmos.models.CosmosItemOperation;
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

import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_INTERVAL_AFTER_DRAINING_INCOMING_FLUX_IN_MILLISECONDS;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Transactional bulk executor with per-partition adaptive micro-batch sizing and grouping by partition.
 */
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
    private final Flux<CosmosBatch> inputBatches;

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
    private final Sinks.Many<CosmosBatch> mainSink;
    private final List<Sinks.Many<CosmosBatch>> groupSinks;
    private final List<Sinks.Many<Integer>> flushSignalGroupSinks;
    private final AtomicReference<Disposable> scheduledFutureForFlush;

    @SuppressWarnings({"unchecked"})
    public TransactionalBulkExecutor(
        CosmosAsyncContainer container,
        Flux<CosmosBatch> inputBatches,
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

        // Initialize main and group sinks for rerouting batches on partition changes
        this.mainSink = Sinks.many().unicast().onBackpressureBuffer();
        this.groupSinks = new CopyOnWriteArrayList<>();
        this.flushSignalGroupSinks = new CopyOnWriteArrayList<>();

        // Initialize partition thresholds map and default options for thresholds.
        this.partitionScopeThresholds = new ConcurrentHashMap<>();

        Scheduler schedulerSnapshotFromOptions = transactionalBulkOptions.getSchedulerOverride();
        this.executionScheduler = schedulerSnapshotFromOptions != null
            ? schedulerSnapshotFromOptions
            : CosmosSchedulers.TRANSACTIONAL_BULK_EXECUTOR_BOUNDED_ELASTIC;

        // setup this background task which will try to emit flush signal,
        // this is just a safeguard to prevent the pipeline got stuck in case when a cosmosBatch completes,
        // the flush signal is not issued successfuly
        int flushInterval = Configs.DEFAULT_BULK_TRANSACTIONAL_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS;
        this.scheduledFutureForFlush = new AtomicReference<>(CosmosSchedulers
            .BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC
            .schedulePeriodically(
                this::onFlush,
                flushInterval,
                flushInterval,
                TimeUnit.MILLISECONDS));

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

    private void cancelFlushTask(boolean initializeAggressiveFlush) {
        long flushIntervalAfterDrainingIncomingFlux =
            DEFAULT_MAX_MICRO_BATCH_INTERVAL_AFTER_DRAINING_INCOMING_FLUX_IN_MILLISECONDS;

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
                        .doOnNext(cosmosBatch -> {
                            totalCount.incrementAndGet();

                            setRetryPolicyForTransactionalBatch(
                                docClientWrapper,
                                this.container,
                                cosmosBatch,
                                this.throttlingRetryOptions
                            );

                            logger.trace(
                                "SetRetryPolicy for cosmos batch, pkValue: {}, totalCount: {}, Context: {}, {}",
                                cosmosBatch.getPartitionKeyValue(),
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
                                logInfoOrWarning("Getting complete signal, total count is 0, close all sinks");
                                completeAllSinks();
                            } else {
                                this.cancelFlushTask(true);
                                this.onFlush();

                                logDebugOrWarning("Scheduled new flush operation {}, Context: {}", getThreadInfo(), this.operationContextText);
                            }
                        })
                        .mergeWith(mainSink.asFlux())
                        .subscribeOn(this.executionScheduler)
                        .flatMap(cosmosBatch -> {
                            logger.trace("Before Resolve PkRangeId, PkValue {}, Context: {} {}",
                                cosmosBatch.getPartitionKeyValue(),
                                this.operationContextText,
                                getThreadInfo());

                            // resolve partition key range id and attach PartitionScopeThresholds
                            return resolvePartitionKeyRangeIdForBatch(cosmosBatch)
                                .map(pkRangeId -> {
                                    PartitionScopeThresholds thresholds = this.partitionScopeThresholds.computeIfAbsent(
                                        pkRangeId,
                                        newPkRangeId ->
                                            new PartitionScopeThresholds(
                                                pkRangeId,
                                                this.transactionalBulkExecutionOptionsImpl.getMinBatchRetryRate(),
                                                this.transactionalBulkExecutionOptionsImpl.getMaxBatchRetryRate(),
                                                this.transactionalBulkExecutionOptionsImpl.getMaxOperationsConcurrency(), // use the cosmos batch ops count as the initial value
                                                this.transactionalBulkExecutionOptionsImpl.getMaxOperationsConcurrency(),
                                                1));

                                    logger.trace("Resolved PkRangeId, {}, PKRangeId: {}, PkValue: {}, Context: {} {}",
                                        pkRangeId,
                                        cosmosBatch.getPartitionKeyValue(),
                                        this.operationContextText,
                                        getThreadInfo());

                                    return Pair.of(thresholds, cosmosBatch);
                                });
                        })
                        .groupBy(Pair::getKey, Pair::getValue)
                        .flatMap(this::executePartitionedGroupTransactional, maxConcurrentCosmosPartitions)
                        .subscribeOn(this.executionScheduler)
                        .doOnNext(requestAndResponse -> {

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
                        })
                        .doOnComplete(() -> {
                            int totalCountSnapshot = totalCount.get();
                            boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
                            if (totalCountSnapshot == 0 && mainSourceCompletedSnapshot) {
                                // It is possible that count is zero but there are more elements in the source.
                                // Count 0 also signifies that there are no pending elements in any sink.
                                logInfoOrWarning("DoOnComplete: All work completed, Context: {}", this.operationContextText);
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

    private Flux<CosmosBatchResponse> executePartitionedGroupTransactional(
        GroupedFlux<PartitionScopeThresholds, CosmosBatch> partitionedGroupFluxOfBatches) {

        final PartitionScopeThresholds thresholds = partitionedGroupFluxOfBatches.key();

        final Sinks.Many<CosmosBatch> groupSink = Sinks.many().unicast().onBackpressureBuffer();
        final Flux<CosmosBatch> groupFlux = groupSink.asFlux();
        groupSinks.add(groupSink);

        Sinks.Many<Integer> flushSignalGroupSink = Sinks.many().multicast().directBestEffort();
        Flux<Integer> flushSignalGroupFlux = flushSignalGroupSink.asFlux().share();
        flushSignalGroupSinks.add(flushSignalGroupSink);

        AtomicInteger totalOperationsInFlight = new AtomicInteger(0);
        AtomicInteger totalBatchesInFlight = new AtomicInteger(0);

        return partitionedGroupFluxOfBatches
            .mergeWith(groupFlux)
            .publishOn(this.executionScheduler)
            .concatMap(cosmosBatch -> {
                // using concatMap here for a sequential processing
                // this part is to decide whether the cosmos batch can be flushed to downstream for processing
                // based on the per-partition threshold and concurrency config
                return Mono.defer(() -> {
                    if (canFlushCosmosBatch(
                        totalOperationsInFlight,
                        totalBatchesInFlight,
                        thresholds,
                        cosmosBatch)) {

                        return Mono.just(cosmosBatch);
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
                                cosmosBatch))
                        .next()
                        .then();
                })
                .then(Mono.defer(() -> {
                    totalOperationsInFlight.addAndGet(cosmosBatch.getOperations().size());
                    totalBatchesInFlight.incrementAndGet();
                    logInfoOrWarning(
                        "Flush cosmos batch, PKRangeId: {}, PkValue: {}, totalOperationsInFlight: {}, totalBatchesInFlight: {}, Context: {} {}",
                        thresholds.getPartitionKeyRangeId(),
                        cosmosBatch.getPartitionKeyValue(),
                        totalOperationsInFlight.get(),
                        totalBatchesInFlight.get(),
                        this.operationContextText,
                        getThreadInfo());

                    return Mono.just(cosmosBatch);
                }));
            })
            .flatMap(cosmosBatch ->
                this.executeTransactionalBatchWithThresholds(
                    cosmosBatch,
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
        CosmosBatch cosmosBatch) {

        int targetBatchSize = partitionScopeThresholds.getTargetMicroBatchSizeSnapshot();
        boolean canFlush = (cosmosBatch.getOperations().size() + totalOperationsInFlight.get() <= targetBatchSize)
            || (totalConcurrentBatchesInFlight.get() <= 0);

        logger.info(
            "canFlushCosmosBatch, PkRangeId: {}, targetBatchSize {}, current total operations in flight {}, current batches in flight {}, batch op count {}, canFlush {}",
            partitionScopeThresholds.getPartitionKeyRangeId(),
            targetBatchSize,
            totalOperationsInFlight.get(),
            totalConcurrentBatchesInFlight.get(),
            cosmosBatch.getOperations().size(),
            canFlush);

        return canFlush;
    }

    private void setRetryPolicyForTransactionalBatch(
        AsyncDocumentClient docClientWrapper,
        CosmosAsyncContainer container,
        CosmosBatch cosmosBatch,
        ThrottlingRetryOptions throttlingRetryOptions) {

        ResourceThrottleRetryPolicy resourceThrottleRetryPolicy = new ResourceThrottleRetryPolicy(
            throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
            throttlingRetryOptions.getMaxRetryWaitTime(),
            true);

        TransactionalBatchRetryPolicy retryPolicy = new TransactionalBatchRetryPolicy(
            docClientWrapper.getCollectionCache(),
            docClientWrapper.getPartitionKeyRangeCache(),
            BridgeInternal.getLink(container),
            resourceThrottleRetryPolicy);

        cosmosBatchAccessor.setRetryPolicy(cosmosBatch, retryPolicy);
    }

    private Mono<CosmosBatchResponse> enqueueForRetry(
        Duration backOffTime,
        Sinks.Many<CosmosBatch> groupSink,
        CosmosBatch cosmosBatch,
        PartitionScopeThresholds thresholds) {

        // Record an enqueued retry for threshold adjustments
        this.recordResponseForRetry(cosmosBatch, thresholds);

        if (backOffTime == null || backOffTime.isZero()) {
            groupSink.emitNext(cosmosBatch, serializedEmitFailureHandler);
            return Mono.empty();
        } else {
            return Mono
                .delay(backOffTime)
                .flatMap((dummy) -> {
                    groupSink.emitNext(cosmosBatch, serializedCompleteEmitFailureHandler);
                    return Mono.empty();
                });
        }
    }

    private Mono<CosmosBatchResponse> executeTransactionalBatchWithThresholds(
        CosmosBatch cosmosBatch,
        PartitionScopeThresholds thresholds,
        Sinks.Many<CosmosBatch> groupSink,
        Sinks.Many<Integer> flushSignalGroupSink,
        AtomicInteger totalBatchesInFlight,
        AtomicInteger totalOperationsInFlight) {

        List<CosmosItemOperation> operations = cosmosBatch.getOperations();
        String batchTrackingId = UUIDs.nonBlockingRandomUUID().toString();

        logDebugOrWarning(
            "Executing transactional batch - {} operations, PK: {}, TrackingId: {}, Context: {}",
            operations.size(),
            cosmosBatch.getPartitionKeyValue(),
            batchTrackingId,
            this.operationContextText);

        CosmosBatchRequestOptions batchRequestOptions = getBatchRequestOptions();

        return this.container
            .executeCosmosBatch(cosmosBatch, batchRequestOptions)
            .publishOn(this.executionScheduler)
            .flatMap(response -> {
                logTraceOrWarning(
                    "Response for transactional batch of partitionKey %s - status code %s, ActivityId: %s, batch TrackingId %s",
                    cosmosBatch.getPartitionKeyValue(),
                    response.getStatusCode(),
                    response.getActivityId(),
                    batchTrackingId);

                if (response.isSuccessStatusCode()) {
                    recordSuccessfulResponse(cosmosBatch, thresholds);
                    return Mono.just(response);
                }

                return handleUnSuccessfulResponse(thresholds, batchTrackingId, cosmosBatch, response, groupSink);
            })
            .onErrorResume(throwable -> {
                if (!(throwable instanceof Exception)) {
                    return Mono.error(Exceptions.propagate(throwable));
                }

                Exception exception = (Exception) throwable;
                return this.handleTransactionalBatchExecutionException(
                    cosmosBatch,
                    exception,
                    groupSink,
                    thresholds,
                    batchTrackingId);
            })
            .doFinally(signalType -> {
                totalOperationsInFlight.addAndGet(-cosmosBatch.getOperations().size());
                totalBatchesInFlight.decrementAndGet();
                flushSignalGroupSink.emitNext(1, serializedEmitFailureHandler);
                logger.info(
                    "CosmosBatch completed, emit flush signal, total operations in flight {}, total batches in flight {},",
                    totalOperationsInFlight.get(),
                    totalBatchesInFlight.get());
            })
            .subscribeOn(this.executionScheduler);
    }

    private void recordSuccessfulResponse(CosmosBatch cosmosBatch, PartitionScopeThresholds thresholds) {
        for (int i = 0; i < cosmosBatch.getOperations().size(); i++) {
            thresholds.recordSuccessfulOperation();
        }
    }

    private void recordResponseForRetry(CosmosBatch cosmosBatch, PartitionScopeThresholds thresholds) {
        for (int i = 0; i < cosmosBatch.getOperations().size(); i++) {
            thresholds.recordEnqueuedRetry();
        }
    }

    private Mono<CosmosBatchResponse> handleUnSuccessfulResponse(
        PartitionScopeThresholds thresholds,
        String batchTrackingId,
        CosmosBatch cosmosBatch,
        CosmosBatchResponse response,
        Sinks.Many<CosmosBatch> groupSink) {

        logDebugOrWarning(
            "handleUnSuccessfulResponse for partitionKey %s - pkRangeId {}, statusCode {}, subStatusCode {}, batch TrackingId %s, {}",
            cosmosBatch.getPartitionKeyValue(),
            thresholds.getPartitionKeyRangeId(),
            response.getStatusCode(),
            response.getSubStatusCode(),
            batchTrackingId,
            getThreadInfo());

        // Create CosmosException for retry policy to understand:
        CosmosException exception = BridgeInternal.createCosmosException(
            null,
            response.getStatusCode(),
            null,
            BulkExecutorUtil.getResponseHeadersFromBatchOperationResult(response));
        BridgeInternal.setSubStatusCode(exception, response.getSubStatusCode());

        return this.handleTransactionalBatchExecutionException(cosmosBatch, exception, groupSink, thresholds, batchTrackingId)
            .onErrorResume(throwable -> Mono.just(response)); // the operation can not be retried, return the original response
    }

    private Mono<CosmosBatchResponse> handleTransactionalBatchExecutionException(
        CosmosBatch cosmosBatch,
        Exception exception,
        Sinks.Many<CosmosBatch> groupSink,
        PartitionScopeThresholds thresholds,
        String batchTrackingId) {

        logDebugOrWarning(
            "HandleTransactionalBatchExecutionException for partitionKey %s - pkRangeId {}, Error {}, batch TrackingId %s, {}",
            cosmosBatch.getPartitionKeyValue(),
            thresholds.getPartitionKeyRangeId(),
            exception,
            batchTrackingId,
            getThreadInfo());

        if (exception instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) exception;

            return cosmosBatchAccessor
                .getRetryPolicy(cosmosBatch)
                .shouldRetryInMainSink(cosmosException)
                .flatMap(shouldRetryInMainSink -> {
                    if (shouldRetryInMainSink) {
                        logDebugOrWarning(
                            "HandleTransactionalBatchExecutionException - Retry in main sink for partitionKey %s - pkRangeId {}, Error {}, batch TrackingId %s, {}",
                            cosmosBatch.getPartitionKeyValue(),
                            thresholds.getPartitionKeyRangeId(),
                            exception,
                            batchTrackingId,
                            getThreadInfo());

                        // retry - but don't mark as enqueued for retry in thresholds
                        mainSink.emitNext(cosmosBatch, serializedEmitFailureHandler); //TODO: validate booking marking for concurrent ops in flight
                        return Mono.empty();
                    } else {
                        return retryOtherExceptions(
                            cosmosBatch,
                            groupSink,
                            cosmosBatchAccessor.getRetryPolicy(cosmosBatch),
                            cosmosException,
                            thresholds);
                    }
                });
        }

        return Mono.error(exception);
    }

    private Mono<CosmosBatchResponse> retryOtherExceptions(
        CosmosBatch cosmosBatch,
        Sinks.Many<CosmosBatch> groupSink,
        TransactionalBatchRetryPolicy retryPolicy,
        CosmosException cosmosException,
        PartitionScopeThresholds thresholds) {

        return retryPolicy.shouldRetry(cosmosException).flatMap(result -> {
            if (result.shouldRetry) {
                return this.enqueueForRetry(result.backOffTime, groupSink, cosmosBatch, thresholds);
            } else {
                return Mono.error(cosmosException);
            }
        });
    }

    private Mono<String> resolvePartitionKeyRangeIdForBatch(CosmosBatch batch) {
        checkNotNull(batch, "expected non-null batch");

        return BulkExecutorUtil.resolvePartitionKeyRangeId(
            docClientWrapper,
            container,
            batch.getPartitionKeyValue(),
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

        return batchRequestOptions;
    }

    private void completeAllSinks() {
        logInfoOrWarning("Completing execution, Context: {}", this.operationContextText);
        logger.debug("Executor service shut down, Context: {}", this.operationContextText);

        try {
            mainSink.emitComplete(serializedCompleteEmitFailureHandler);
        } catch (Throwable t) {
            logger.warn("Failed to complete main sink, Context: {}", this.operationContextText, t);
        }

        this.shutdown();
    }

    private void onFlush() {
        try {
            this.flushSignalGroupSinks.forEach(sink -> sink.emitNext(1, serializedEmitFailureHandler));
        } catch(Throwable t) {
            logger.error("Callback invocation 'onFlush' failed. Context: {}", this.operationContextText,  t);
        }
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
