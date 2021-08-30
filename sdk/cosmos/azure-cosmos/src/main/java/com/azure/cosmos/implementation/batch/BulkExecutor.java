// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.CosmosDaemonThreadFactory;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
public final class BulkExecutor<TContext> {
    private final static Logger logger = LoggerFactory.getLogger(BulkExecutor.class);
    private final static AtomicLong instanceCount = new AtomicLong(0);

    private final CosmosAsyncContainer container;
    private final AsyncDocumentClient docClientWrapper;
    private final String operationContextText;
    private final OperationContextAndListenerTuple operationListener;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private final Flux<com.azure.cosmos.models.CosmosItemOperation> inputOperations;

    // Options for bulk execution.
    private final Long maxMicroBatchIntervalInMs;
    private final TContext batchContext;
    private final ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds;
    private final CosmosBulkExecutionOptions cosmosBulkExecutionOptions;

    // Handle gone error:
    private final AtomicBoolean mainSourceCompleted;
    private final AtomicInteger totalCount;
    private final FluxProcessor<CosmosItemOperation, CosmosItemOperation> mainFluxProcessor;
    private final FluxSink<CosmosItemOperation> mainSink;
    private final List<FluxSink<CosmosItemOperation>> groupSinks;
    private final ScheduledExecutorService executorService;

    public BulkExecutor(CosmosAsyncContainer container,
                        Flux<CosmosItemOperation> inputOperations,
                        CosmosBulkExecutionOptions cosmosBulkOptions) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(inputOperations, "expected non-null inputOperations");
        checkNotNull(cosmosBulkOptions, "expected non-null bulkOptions");

        this.cosmosBulkExecutionOptions = cosmosBulkOptions;
        this.container = container;
        this.inputOperations = inputOperations;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.throttlingRetryOptions = docClientWrapper.getConnectionPolicy().getThrottlingRetryOptions();

        // Fill the option first, to make the BulkProcessingOptions immutable, as if accessed directly, we might get
        // different values when a new group is created.
        maxMicroBatchIntervalInMs = cosmosBulkExecutionOptions.getMaxMicroBatchInterval().toMillis();
        batchContext = ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .getLegacyBatchScopedContext(cosmosBulkExecutionOptions);
        this.partitionScopeThresholds = ImplementationBridgeHelpers.CosmosBulkExecutionThresholdsStateHelper
            .getBulkExecutionThresholdsAccessor()
            .getPartitionScopeThresholds(cosmosBulkExecutionOptions.getThresholds());
        operationListener = ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .getOperationContext(cosmosBulkExecutionOptions);
        if (operationListener != null &&
            operationListener.getOperationContext() != null) {
            operationContextText = operationListener.getOperationContext().toString();
        } else {
            operationContextText = "n/a";
        }

        // Initialize sink for handling gone error.
        mainSourceCompleted = new AtomicBoolean(false);
        totalCount = new AtomicInteger(0);
        mainFluxProcessor = UnicastProcessor.<CosmosItemOperation>create().serialize();
        mainSink = mainFluxProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        groupSinks = new CopyOnWriteArrayList<>();

        // The evaluation whether a micro batch should be flushed to the backend happens whenever
        // a new ItemOperation arrives. If the batch size is exceeded or the oldest buffered ItemOperation
        // exceeds the MicroBatchInterval, the micro batch gets flushed to the backend.
        // To make sure we flush the buffers at least every maxMicroBatchIntervalInMs we start a timer
        // that will trigger artificial ItemOperations that are only used to flush the buffers (and will be
        // filtered out before sending requests to the backend)
        this.executorService = Executors.newSingleThreadScheduledExecutor(
                new CosmosDaemonThreadFactory("BulkExecutor-" + instanceCount.incrementAndGet()));
        this.executorService.scheduleWithFixedDelay(
            this::onFlush,
            this.maxMicroBatchIntervalInMs,
            this.maxMicroBatchIntervalInMs,
            TimeUnit.MILLISECONDS);
    }

    public Flux<CosmosBulkOperationResponse<TContext>> execute() {

        return this.inputOperations
            .onErrorContinue((throwable, o) ->
                logger.error("Skipping an error operation while processing {}. Cause: {}, Context: {}",
                    o,
                    throwable.getMessage(),
                    this.operationContextText))
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
            })
            .doOnComplete(() -> {
                mainSourceCompleted.set(true);

                long totalCountSnapshot = totalCount.get();
                logger.debug("Main source completed - # left items {}, Context: {}",
                    totalCountSnapshot,
                    this.operationContextText);
                if (totalCountSnapshot == 0) {
                    // This is needed as there can be case that onComplete was called after last element was processed
                    // So complete the sink here also if count is 0, if source has completed and count isn't zero,
                    // then the last element in the doOnNext will close it. Sink doesn't mind in case of a double close.

                    completeAllSinks();
                } else {
                    this.onFlush();
                }
            })
            .mergeWith(mainFluxProcessor)
            .flatMap(operation -> {

                // resolve partition key range id again for operations which comes in main sink due to gone retry.
                return BulkExecutorUtil.resolvePartitionKeyRangeId(this.docClientWrapper, this.container, operation)
                    .map((String pkRangeId) -> {
                        PartitionScopeThresholds partitionScopeThresholds =
                            this.partitionScopeThresholds.computeIfAbsent(
                                pkRangeId,
                                (newPkRangeId) -> new PartitionScopeThresholds(newPkRangeId, this.cosmosBulkExecutionOptions));
                        return Pair.of(partitionScopeThresholds, operation);
                    });
            })
            .groupBy(Pair::getKey, Pair::getValue)
            .flatMap(this::executePartitionedGroup)
            .doOnNext(requestAndResponse -> {

                int totalCountAfterDecrement = totalCount.decrementAndGet();
                boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
                if (totalCountAfterDecrement == 0 && mainSourceCompletedSnapshot) {
                    // It is possible that count is zero but there are more elements in the source.
                    // Count 0 also signifies that there are no pending elements in any sink.
                    logger.debug("All work completed, Context: {}", this.operationContextText);
                    completeAllSinks();
                } else {
                    logger.debug(
                        "Work left - TotalCount after decrement: {}, main sink completed {}, Context: {}",
                        totalCountAfterDecrement,
                        mainSourceCompletedSnapshot,
                        this.operationContextText);
                }
            })
            .doOnComplete(() -> {
                int totalCountSnapshot = totalCount.get();
                boolean mainSourceCompletedSnapshot = mainSourceCompleted.get();
                if (totalCountSnapshot == 0 && mainSourceCompletedSnapshot) {
                    // It is possible that count is zero but there are more elements in the source.
                    // Count 0 also signifies that there are no pending elements in any sink.
                    logger.debug("DoOnComplete: All work completed, Context: {}", this.operationContextText);
                    completeAllSinks();
                } else {
                    logger.debug(
                        "DoOnComplete: Work left - TotalCount after decrement: {}, main sink completed {}, Context: {}",
                        totalCountSnapshot,
                        mainSourceCompletedSnapshot,
                        this.operationContextText);
                }
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

        return partitionedGroupFluxOfInputOperations
            .mergeWith(groupFluxProcessor)
            .onBackpressureBuffer()
            .timestamp()
            .bufferUntil(timeStampItemOperationTuple -> {
                long timestamp = timeStampItemOperationTuple.getT1();
                CosmosItemOperation itemOperation = timeStampItemOperationTuple.getT2();

                if (itemOperation == FlushBuffersItemOperation.singleton()) {
                    if (currentMicroBatchSize.get() > 0) {
                        logger.debug(
                            "Flushing PKRange {} due to FlushItemOperation, Context: {}",
                            thresholds.getPartitionKeyRangeId(),
                            this.operationContextText);

                        firstRecordTimeStamp.set(-1);
                        currentMicroBatchSize.set(0);

                        return true;
                    }

                    // avoid counting flush operations for the micro batch size calculation
                    return false;
                }

                firstRecordTimeStamp.compareAndSet(-1, timestamp);
                long age = timestamp - firstRecordTimeStamp.get();
                long batchSize = currentMicroBatchSize.incrementAndGet();
                if (batchSize >= thresholds.getTargetMicroBatchSizeSnapshot() ||
                    age >=  this.maxMicroBatchIntervalInMs) {

                    logger.debug(
                        "Flushing PKRange {} due to BatchSize ({}) or age ({}), Context: {}",
                        thresholds.getPartitionKeyRangeId(),
                        batchSize,
                        age,
                        this.operationContextText);
                    firstRecordTimeStamp.set(-1);
                    currentMicroBatchSize.set(0);
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

                    return executeOperations(operations, thresholds, groupSink);
                },
                this.cosmosBulkExecutionOptions.getMaxMicroBatchConcurrency());
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executeOperations(
        List<CosmosItemOperation> operations,
        PartitionScopeThresholds thresholds,
        FluxSink<CosmosItemOperation> groupSink) {

        if (operations.size() == 0) {
            logger.debug("Empty operations list, Context: {}", this.operationContextText);
            return Flux.empty();
        }

        String pkRange = thresholds.getPartitionKeyRangeId();
        ServerOperationBatchRequest serverOperationBatchRequest =
            BulkExecutorUtil.createBatchRequest(operations, pkRange);
        if (serverOperationBatchRequest.getBatchPendingOperations().size() > 0) {
            serverOperationBatchRequest.getBatchPendingOperations().forEach(groupSink::next);
        }

        return Flux.just(serverOperationBatchRequest.getBatchRequest())
            .publishOn(Schedulers.boundedElastic())
            .flatMap((PartitionKeyRangeServerBatchRequest serverRequest) ->
                this.executePartitionKeyRangeServerBatchRequest(serverRequest, groupSink, thresholds));
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executePartitionKeyRangeServerBatchRequest(
        PartitionKeyRangeServerBatchRequest serverRequest,
        FluxSink<CosmosItemOperation> groupSink,
        PartitionScopeThresholds thresholds) {

        return this.executeBatchRequest(serverRequest)
            .flatMapMany(response ->
                Flux.fromIterable(response.getResults()).flatMap((CosmosBatchOperationResult result) ->
                    handleTransactionalBatchOperationResult(response, result, groupSink, thresholds)))
            .onErrorResume((Throwable throwable) -> {

                if (!(throwable instanceof Exception)) {
                    throw Exceptions.propagate(throwable);
                }

                Exception exception = (Exception) throwable;

                return Flux.fromIterable(serverRequest.getOperations()).flatMap((CosmosItemOperation itemOperation) ->
                    handleTransactionalBatchExecutionException(itemOperation, exception, groupSink, thresholds));
            });
    }

    // Helper functions
    private Mono<CosmosBulkOperationResponse<TContext>> handleTransactionalBatchOperationResult(
        CosmosBatchResponse response,
        CosmosBatchOperationResult operationResult,
        FluxSink<CosmosItemOperation> groupSink,
        PartitionScopeThresholds thresholds) {

        CosmosBulkItemResponse cosmosBulkItemResponse = ModelBridgeInternal.createCosmosBulkItemResponse(operationResult, response);
        CosmosItemOperation itemOperation = operationResult.getOperation();
        TContext actualContext = this.getActualContext(itemOperation);

        if (!operationResult.isSuccessStatusCode()) {

            if (itemOperation instanceof ItemBulkOperation<?, ?>) {

                ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) itemOperation;
                return itemBulkOperation.getRetryPolicy().shouldRetry(operationResult).flatMap(
                    result -> {
                        if (result.shouldRetry) {
                            return this.enqueueForRetry(result.backOffTime, groupSink, itemOperation, thresholds);
                        } else {
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

        if (exception instanceof CosmosException && itemOperation instanceof ItemBulkOperation<?, ?>) {
            CosmosException cosmosException = (CosmosException) exception;
            ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) itemOperation;

            // First check if it failed due to split, so the operations need to go in a different pk range group. So
            // add it in the mainSink.

            return itemBulkOperation.getRetryPolicy()
                .shouldRetryForGone(cosmosException.getStatusCode(), cosmosException.getSubStatusCode())
                .flatMap(shouldRetryGone -> {
                    if (shouldRetryGone) {
                        // retry - but don't mark as enqueued for retry in thresholds
                        mainSink.next(itemOperation);
                        return Mono.empty();
                    } else {
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
        options.setOperationContextAndListenerTuple(operationListener);

        return this.docClientWrapper.executeBatchRequest(
            BridgeInternal.getLink(this.container), serverRequest, options, false);
    }

    private void completeAllSinks() {
        logger.info("Closing all sinks, Context: {}", this.operationContextText);

        executorService.shutdown();
        logger.debug("Executor service shut down, Context: {}", this.operationContextText);
        mainSink.complete();
        logger.debug("Main sink completed, Context: {}", this.operationContextText);
        groupSinks.forEach(FluxSink::complete);
        logger.debug("All group sinks completed, Context: {}", this.operationContextText);
    }

    private void onFlush() {
        try {
            this.groupSinks.forEach(sink -> sink.next(FlushBuffersItemOperation.singleton()));
        } catch(Throwable t) {
            logger.error("Callback invocation 'onFlush' failed.", t);
        }
    }
}
