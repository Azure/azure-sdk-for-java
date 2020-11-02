// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.BulkProcessingOptions;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosBulkItemResponse;
import com.azure.cosmos.CosmosBulkOperationResponse;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.TransactionalBatchResponse;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class BulkExecutor<TContext> {

    private final static Logger logger = LoggerFactory.getLogger(BulkExecutor.class);

    private final CosmosAsyncContainer container;
    private final AsyncDocumentClient docClientWrapper;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private final Flux<CosmosItemOperation> inputOperations;

    // Options for bulk execution.
    private final int maxMicroBatchSize;
    private final int maxMicroBatchConcurrency;
    private final Duration maxMicroBatchInterval;
    private final TContext batchContext;

    // Handle gone error:
    private final AtomicBoolean mainSourceCompleted;
    private final AtomicInteger totalCount;
    private final FluxProcessor<CosmosItemOperation, CosmosItemOperation> mainFluxProcessor;
    private final FluxSink<CosmosItemOperation> mainSink;
    private final List<FluxSink<CosmosItemOperation>> groupSinks;

    public BulkExecutor(CosmosAsyncContainer container,
                        Flux<CosmosItemOperation> inputOperations,
                        BulkProcessingOptions<TContext> bulkOptions) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(inputOperations, "expected non-null inputOperations");
        checkNotNull(bulkOptions, "expected non-null bulkOptions");

        this.container = container;
        this.inputOperations = inputOperations;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.throttlingRetryOptions = docClientWrapper.getConnectionPolicy().getThrottlingRetryOptions();

        // Fill the option first, to make the BulkProcessingOptions immutable, as if accessed directly, we might get
        // different values when a new group is created.
        maxMicroBatchSize = bulkOptions.getMaxMicroBatchSize();
        maxMicroBatchConcurrency = bulkOptions.getMaxMicroBatchConcurrency();
        maxMicroBatchInterval = bulkOptions.getMaxMicroBatchInterval();
        batchContext = bulkOptions.getBatchContext();

        // Initialize sink for handling gone error.
        mainSourceCompleted = new AtomicBoolean(false);
        totalCount = new AtomicInteger(0);
        mainFluxProcessor = UnicastProcessor.<CosmosItemOperation>create().serialize();
        mainSink = mainFluxProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        groupSinks = new ArrayList<>();
    }

    /**
     * The actual execution of the flux of operations. It is done in 5 steps:

     * 1. Getting partition key range ID and grouping operations using that id.
     * 2. For the flux of operations in a group, adding buffering based on size and a duration.
     * 3. For the operation we get in after buffering, process it using a batch request and return
     *    a wrapper having request, response(if-any) and exception(if-any). Either response or exception will be there.
     *
     * 4. Any internal retry is done by adding in an intermediate sink for each grouped flux.
     * 5. Any operation which failed due to partition key range gone is retried by re calculating it's range id.
     */
    public Flux<CosmosBulkOperationResponse<TContext>> execute() {

        Flux<CosmosBulkOperationResponse<TContext>> responseFlux = this.inputOperations
            .onErrorResume((error) -> {
                // eat up the error signals
                return Mono.empty();
            })
            .doOnNext((CosmosItemOperation cosmosItemOperation) -> {

                // Set the retry policy before starting execution. Should only happens once.
                BulkExecutorUtil.setRetryPolicyForBulk(cosmosItemOperation, this.throttlingRetryOptions);

                totalCount.incrementAndGet();
            })
            .doOnComplete(() -> {
                mainSourceCompleted.set(true);

                if (totalCount.get() == 0) {
                    // This is needed as there can be case that onComplete was called after last element was processed
                    // So complete the sink here also if count is 0, if source has completed and count isn't zero,
                    // then the last element in the doOnNext will close it. Sink doesn't mind in case of a double close.

                    completeAllSinks();
                }
            })
            .mergeWith(mainFluxProcessor)
            .flatMap(operation -> {

                // resolve partition key range id again for operations which comes in main sink due to gone retry.
                return BulkExecutorUtil.resolvePartitionKeyRangeId(this.docClientWrapper, this.container, operation)
                    .map((String pkRangeId) -> Pair.of(pkRangeId, operation));
            })
            .groupBy(Pair::getKey, Pair::getValue)
            .flatMap(this::executePartitionedGroup)
            .doOnNext(requestAndResponse -> {

                if (totalCount.decrementAndGet() == 0 && mainSourceCompleted.get()) {
                    // It is possible that count is zero but there are more elements in the source.
                    // Count 0 also signifies that there are no pending elements in any sink.

                    completeAllSinks();
                }
            });

        return responseFlux;
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executePartitionedGroup(
        GroupedFlux<String, CosmosItemOperation> partitionedGroupFluxOfInputOperations) {

        final String pkRange = partitionedGroupFluxOfInputOperations.key();

        final FluxProcessor<CosmosItemOperation, CosmosItemOperation> groupFluxProcessor =
            UnicastProcessor.<CosmosItemOperation>create().serialize();
        final FluxSink<CosmosItemOperation> groupSink = groupFluxProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        groupSinks.add(groupSink);

        return partitionedGroupFluxOfInputOperations
            .mergeWith(groupFluxProcessor)
            .bufferTimeout(this.maxMicroBatchSize, this.maxMicroBatchInterval)
            .onBackpressureBuffer()
            .flatMap((List<CosmosItemOperation> cosmosItemOperations) -> {
                return executeOperations(cosmosItemOperations, pkRange, groupSink);
            }, this.maxMicroBatchConcurrency);
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executeOperations(
        List<CosmosItemOperation> operations,
        String pkRange,
        FluxSink<CosmosItemOperation> groupSink) {

        ServerOperationBatchRequest serverOperationBatchRequest = BulkExecutorUtil.createBatchRequest(operations, pkRange);
        if (serverOperationBatchRequest.getBatchPendingOperations().size() > 0) {
            serverOperationBatchRequest.getBatchPendingOperations().forEach(groupSink::next);
        }

        return Flux.just(serverOperationBatchRequest.getBatchRequest())
            .publishOn(Schedulers.elastic())
            .flatMap((PartitionKeyRangeServerBatchRequest serverRequest) -> {
                return this.executePartitionKeyRangeServerBatchRequest(serverRequest, groupSink);
            });
    }

    private Flux<CosmosBulkOperationResponse<TContext>> executePartitionKeyRangeServerBatchRequest(
        PartitionKeyRangeServerBatchRequest serverRequest,
        FluxSink<CosmosItemOperation> groupSink) {

        return this.executeBatchRequest(serverRequest)
            .flatMapMany(response -> {

                return Flux.fromIterable(response.getResults()).flatMap((TransactionalBatchOperationResult result) -> {
                    return handleTransactionalBatchOperationResult(response, result, groupSink);
                });
            })
            .onErrorResume((Throwable throwable) -> {

                return Flux.fromIterable(serverRequest.getOperations()).flatMap((CosmosItemOperation itemOperation) -> {
                    return handleTransactionalBatchExecutionException(itemOperation, throwable, groupSink);
                });
            });
    }

    // Helper functions
    private Mono<CosmosBulkOperationResponse<TContext>> handleTransactionalBatchOperationResult(
        TransactionalBatchResponse response,
        TransactionalBatchOperationResult operationResult,
        FluxSink<CosmosItemOperation> groupSink) {

        CosmosBulkItemResponse cosmosBulkItemResponse = BridgeInternal.createCosmosBulkItemResponse(operationResult, response);
        CosmosItemOperation itemOperation = operationResult.getOperation();

        if (!operationResult.isSuccessStatusCode()) {

            if(itemOperation instanceof ItemBulkOperation<?>) {

                return ((ItemBulkOperation<?>) itemOperation).getRetryPolicy().shouldRetry(operationResult).flatMap(
                    result -> {
                        if (result.shouldRetry) {
                            groupSink.next(itemOperation);
                            return Mono.empty();
                        } else {
                            return Mono.just(BridgeInternal.createCosmosBulkOperationResponse(
                                itemOperation, cosmosBulkItemResponse, this.batchContext));
                        }
                    });

            } else {
                throw new UnsupportedOperationException("Unknown CosmosItemOperation.");
            }
        }

        return Mono.just(BridgeInternal.createCosmosBulkOperationResponse(
            itemOperation,
            cosmosBulkItemResponse,
            this.batchContext));
    }

    private Mono<CosmosBulkOperationResponse<TContext>> handleTransactionalBatchExecutionException(
        CosmosItemOperation itemOperation,
        Throwable throwable,
        FluxSink<CosmosItemOperation> groupSink) {

        if(throwable instanceof CosmosException && itemOperation instanceof ItemBulkOperation<?>) {
            CosmosException cosmosException = (CosmosException) throwable;
            ItemBulkOperation<?> itemBulkOperation = (ItemBulkOperation<?>) itemOperation;

            // First check if it failed due to split, so the operations need to go in a different group. If it is put in
            // the mainSink.
            if(cosmosException.getStatusCode() == HttpResponseStatus.GONE.code() &&
                itemBulkOperation.getRetryPolicy().shouldRetryForGone(
                    cosmosException.getStatusCode(),
                    cosmosException.getSubStatusCode())) {

                mainSink.next(itemOperation);
                return Mono.empty();
            } else {
                return itemBulkOperation.getRetryPolicy().shouldRetry(cosmosException).flatMap(result -> {
                    if (result.shouldRetry) {

                        groupSink.next(itemOperation);
                        return Mono.empty();
                    } else {

                        return Mono.just(BridgeInternal.createCosmosBulkOperationResponse(
                            itemOperation, throwable, this.batchContext));
                    }
                });
            }
        }

        return Mono.just(BridgeInternal.createCosmosBulkOperationResponse(itemOperation, throwable, this.batchContext));
    }

    private Mono<TransactionalBatchResponse> executeBatchRequest(PartitionKeyRangeServerBatchRequest serverRequest) {

        return this.docClientWrapper.executeBatchRequest(
            BridgeInternal.getLink(this.container), serverRequest, null, false);
    }

    private void completeAllSinks() {
        logger.info("Closing all sinks");

        mainSink.complete();
        groupSinks.forEach(FluxSink::complete);
    }
}
