// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosSchedulers;
import com.azure.cosmos.kafka.connect.implementation.sink.patch.KafkaCosmosPatchHelper;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosBulkPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosBulkWriter extends CosmosWriterBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosBulkWriter.class);
    private static final int MAX_DELAY_ON_408_REQUEST_TIMEOUT_IN_MS = 10000;
    private static final int MIN_DELAY_ON_408_REQUEST_TIMEOUT_IN_MS = 1000;
    private static final Random RANDOM = new Random();

    private final CosmosSinkWriteConfig writeConfig;
    private final CosmosThroughputControlConfig throughputControlConfig;
    private final Sinks.EmitFailureHandler emitFailureHandler;

    public CosmosBulkWriter(
        CosmosSinkWriteConfig writeConfig,
        CosmosThroughputControlConfig throughputControlConfig,
        ErrantRecordReporter errantRecordReporter) {
        super(errantRecordReporter);
        checkNotNull(writeConfig, "Argument 'writeConfig' can not be null");

        this.writeConfig = writeConfig;
        this.throughputControlConfig = throughputControlConfig;
        this.emitFailureHandler = new KafkaCosmosEmitFailureHandler();
    }

    @Override
    public void writeCore(CosmosAsyncContainer container, List<SinkOperation> sinkOperations) {
        Sinks.Many<CosmosItemOperation> bulkRetryEmitter = Sinks.many().unicast().onBackpressureBuffer();
        CosmosBulkExecutionOptions bulkExecutionOptions = this.getBulkExecutionOperations();
        AtomicInteger totalPendingRecords = new AtomicInteger(sinkOperations.size());
        Runnable onTaskCompleteCheck = () -> {
            if (totalPendingRecords.decrementAndGet() <= 0) {
                bulkRetryEmitter.emitComplete(emitFailureHandler);
            }
        };

        Flux.fromIterable(sinkOperations)
            .flatMap(sinkOperation -> this.getBulkOperation(container, sinkOperation))
            .collectList()
            .flatMapMany(itemOperations -> {

                Flux<CosmosBulkOperationResponse<Object>> cosmosBulkOperationResponseFlux =
                    container
                        .executeBulkOperations(
                            Flux.fromIterable(itemOperations)
                                .mergeWith(bulkRetryEmitter.asFlux())
                                .publishOn(KafkaCosmosSchedulers.SINK_BOUNDED_ELASTIC),
                            bulkExecutionOptions);
                return cosmosBulkOperationResponseFlux;
            })
            .flatMap(itemResponse -> {
                SinkOperation sinkOperation = itemResponse.getOperation().getContext();
                checkNotNull(sinkOperation, "sinkOperation should not be null");

                if (itemResponse.getResponse() != null && itemResponse.getResponse().isSuccessStatusCode()) {
                    // success
                    this.completeSinkOperation(sinkOperation, onTaskCompleteCheck);
                } else {
                    BulkOperationFailedException exception = handleErrorStatusCode(
                        itemResponse.getResponse(),
                        itemResponse.getException(),
                        sinkOperation);

                    if (shouldIgnore(exception)) {
                        this.completeSinkOperation(sinkOperation, onTaskCompleteCheck);
                    } else {
                        if (shouldRetry(exception, sinkOperation.getRetryCount(), this.writeConfig.getMaxRetryCount())) {
                            sinkOperation.setException(exception);
                            return this.scheduleRetry(container, itemResponse.getOperation().getContext(), bulkRetryEmitter, exception);
                        } else {
                            // operation failed after exhausting all retries
                            this.completeSinkOperationWithFailure(sinkOperation, exception, onTaskCompleteCheck);
                            if (this.writeConfig.getToleranceOnErrorLevel() == ToleranceOnErrorLevel.ALL) {
                                LOGGER.warn(
                                    "Could not upload record {} to CosmosDB after exhausting all retries, "
                                        + "but ToleranceOnErrorLevel is all, will only log the error message. ",
                                    sinkOperation.getSinkRecord().key(),
                                    sinkOperation.getException());
                                return Mono.empty();
                            } else {
                                return Mono.error(exception);
                            }
                        }
                    }
                }

                return Mono.empty();
            })
            .subscribeOn(KafkaCosmosSchedulers.SINK_BOUNDED_ELASTIC)
            .blockLast();
    }

    private CosmosBulkExecutionOptions getBulkExecutionOperations() {
        CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions();
        bulkExecutionOptions.setInitialMicroBatchSize(this.writeConfig.getBulkInitialBatchSize());
        if (this.writeConfig.getBulkMaxConcurrentCosmosPartitions() > 0) {
            ImplementationBridgeHelpers
                .CosmosBulkExecutionOptionsHelper
                .getCosmosBulkExecutionOptionsAccessor()
                .setMaxConcurrentCosmosPartitions(bulkExecutionOptions, this.writeConfig.getBulkMaxConcurrentCosmosPartitions());
        }

        CosmosThroughputControlHelper.tryPopulateThroughputControlGroupName(bulkExecutionOptions, this.throughputControlConfig);

        return bulkExecutionOptions;
    }

    private Mono<CosmosItemOperation> getBulkOperation(
        CosmosAsyncContainer container,
        SinkOperation sinkOperation) {
        return ImplementationBridgeHelpers
            .CosmosAsyncContainerHelper
            .getCosmosAsyncContainerAccessor()
            .getPartitionKeyDefinition(container)
            .flatMap(partitionKeyDefinition -> {
                CosmosItemOperation cosmosItemOperation;

                switch (this.writeConfig.getItemWriteStrategy()) {
                    case ITEM_OVERWRITE:
                        cosmosItemOperation = this.getUpsertItemOperation(sinkOperation, partitionKeyDefinition);
                        break;
                    case ITEM_OVERWRITE_IF_NOT_MODIFIED:
                        String etag = getEtag(sinkOperation.getSinkRecord().value());
                        if (StringUtils.isEmpty(etag)) {
                            cosmosItemOperation = this.getCreateItemOperation(sinkOperation, partitionKeyDefinition);
                        } else {
                            cosmosItemOperation = this.getReplaceItemOperation(sinkOperation, partitionKeyDefinition, etag);
                        }
                        break;
                    case ITEM_APPEND:
                        cosmosItemOperation = this.getCreateItemOperation(sinkOperation, partitionKeyDefinition);
                        break;
                    case ITEM_DELETE:
                        cosmosItemOperation = this.getDeleteItemOperation(sinkOperation, partitionKeyDefinition, null);
                        break;
                    case ITEM_DELETE_IF_NOT_MODIFIED:
                        String itemDeleteEtag = getEtag(sinkOperation.getSinkRecord().value());
                        cosmosItemOperation = this.getDeleteItemOperation(sinkOperation, partitionKeyDefinition, itemDeleteEtag);
                        break;
                    case ITEM_PATCH:
                        cosmosItemOperation = this.getPatchItemOperation(sinkOperation, partitionKeyDefinition);
                        break;
                    default:
                        return Mono.error(new IllegalArgumentException(this.writeConfig.getItemWriteStrategy() + " is not supported"));
                }

                return Mono.just(cosmosItemOperation);
            });
    }

    private CosmosItemOperation getUpsertItemOperation(
        SinkOperation sinkOperation,
        PartitionKeyDefinition partitionKeyDefinition) {

        return CosmosBulkOperations.getUpsertItemOperation(
            sinkOperation.getSinkRecord().value(),
            this.getPartitionKeyValue(sinkOperation.getSinkRecord().value(), partitionKeyDefinition),
            sinkOperation);
    }

    private CosmosItemOperation getCreateItemOperation(
        SinkOperation sinkOperation,
        PartitionKeyDefinition partitionKeyDefinition) {
        return CosmosBulkOperations.getCreateItemOperation(
            sinkOperation.getSinkRecord().value(),
            this.getPartitionKeyValue(sinkOperation.getSinkRecord().value(), partitionKeyDefinition),
            sinkOperation);
    }

    private CosmosItemOperation getReplaceItemOperation(
        SinkOperation sinkOperation,
        PartitionKeyDefinition partitionKeyDefinition,
        String etag) {

        CosmosBulkItemRequestOptions itemRequestOptions = new CosmosBulkItemRequestOptions();
        if (StringUtils.isNotEmpty(etag)) {
            itemRequestOptions.setIfMatchETag(etag);
        }

        return CosmosBulkOperations.getReplaceItemOperation(
            getId(sinkOperation.getSinkRecord().value()),
            sinkOperation.getSinkRecord().value(),
            this.getPartitionKeyValue(sinkOperation.getSinkRecord().value(), partitionKeyDefinition),
            new CosmosBulkItemRequestOptions().setIfMatchETag(etag),
            sinkOperation);
    }

    private CosmosItemOperation getDeleteItemOperation(
        SinkOperation sinkOperation,
        PartitionKeyDefinition partitionKeyDefinition,
        String etag) {

        CosmosBulkItemRequestOptions itemRequestOptions = new CosmosBulkItemRequestOptions();
        if (StringUtils.isNotEmpty(etag)) {
            itemRequestOptions.setIfMatchETag(etag);
        }

        return CosmosBulkOperations.getDeleteItemOperation(
            this.getId(sinkOperation.getSinkRecord().value()),
            this.getPartitionKeyValue(sinkOperation.getSinkRecord().value(), partitionKeyDefinition),
            itemRequestOptions,
            sinkOperation);
    }

    private CosmosItemOperation getPatchItemOperation(
        SinkOperation sinkOperation,
        PartitionKeyDefinition partitionKeyDefinition) {

        CosmosBulkPatchItemRequestOptions patchItemRequestOptions = new CosmosBulkPatchItemRequestOptions();
        if (StringUtils.isNotEmpty(this.writeConfig.getCosmosPatchConfig().getFilter())) {
            patchItemRequestOptions.setFilterPredicate(this.writeConfig.getCosmosPatchConfig().getFilter());
        }

        String itemId = this.getId(sinkOperation.getSinkRecord().value());
        CosmosPatchOperations cosmosPatchOperations = KafkaCosmosPatchHelper.createCosmosPatchOperations(
            itemId,
            partitionKeyDefinition,
            sinkOperation.getSinkRecord(),
            this.writeConfig.getCosmosPatchConfig());

        return CosmosBulkOperations
            .getPatchItemOperation(
                itemId,
                this.getPartitionKeyValue(sinkOperation.getSinkRecord().value(), partitionKeyDefinition),
                cosmosPatchOperations,
                patchItemRequestOptions,
                sinkOperation);
    }

    private Mono<Void> scheduleRetry(
        CosmosAsyncContainer container,
        SinkOperation sinkOperation,
        Sinks.Many<CosmosItemOperation> bulkRetryEmitter,
        BulkOperationFailedException exception) {

        sinkOperation.retry();
        Mono<Void> retryMono =
            getBulkOperation(container, sinkOperation)
                .flatMap(itemOperation -> {
                    bulkRetryEmitter.emitNext(itemOperation, emitFailureHandler);
                    return Mono.empty();
                });

        if (KafkaCosmosExceptionsHelper.isTimeoutException(exception)) {
            Duration delayDuration = Duration.ofMillis(
                MIN_DELAY_ON_408_REQUEST_TIMEOUT_IN_MS
                    + RANDOM.nextInt(MAX_DELAY_ON_408_REQUEST_TIMEOUT_IN_MS - MIN_DELAY_ON_408_REQUEST_TIMEOUT_IN_MS));

            return retryMono.delaySubscription(delayDuration);
        }

        return retryMono;
    }

    BulkOperationFailedException handleErrorStatusCode(
        CosmosBulkItemResponse itemResponse,
        Exception exception,
        SinkOperation sinkOperationContext) {

        int effectiveStatusCode =
            itemResponse != null
                ? itemResponse.getStatusCode()
                : (exception != null && exception instanceof CosmosException ? ((CosmosException) exception).getStatusCode() : HttpConstants.StatusCodes.REQUEST_TIMEOUT);
        int effectiveSubStatusCode =
            itemResponse != null
                ? itemResponse.getSubStatusCode()
                : (exception != null && exception instanceof CosmosException ? ((CosmosException) exception).getSubStatusCode() : 0);

        String errorMessage =
            String.format(
                "Request failed with effectiveStatusCode: {%s}, effectiveSubStatusCode: {%s}, kafkaOffset: {%s}, kafkaPartition: {%s}, topic: {%s}",
                effectiveStatusCode,
                effectiveSubStatusCode,
                sinkOperationContext.getKafkaOffset(),
                sinkOperationContext.getKafkaPartition(),
                sinkOperationContext.getTopic());


        return new BulkOperationFailedException(effectiveStatusCode, effectiveSubStatusCode, errorMessage, exception);
    }

    private boolean shouldIgnore(BulkOperationFailedException failedException) {
        switch (this.writeConfig.getItemWriteStrategy()) {
            case ITEM_APPEND:
                return KafkaCosmosExceptionsHelper.isResourceExistsException(failedException);
            case ITEM_DELETE:
                return KafkaCosmosExceptionsHelper.isNotFoundException(failedException);
            case ITEM_DELETE_IF_NOT_MODIFIED:
                return KafkaCosmosExceptionsHelper.isNotFoundException(failedException)
                    || KafkaCosmosExceptionsHelper.isPreconditionFailedException(failedException);
            case ITEM_OVERWRITE_IF_NOT_MODIFIED:
                return KafkaCosmosExceptionsHelper.isResourceExistsException(failedException)
                    || KafkaCosmosExceptionsHelper.isNotFoundException(failedException)
                    || KafkaCosmosExceptionsHelper.isPreconditionFailedException(failedException);
            default:
                return false;
        }
    }

    private void completeSinkOperation(SinkOperation sinkOperationContext, Runnable onCompleteRunnable) {
        sinkOperationContext.complete();
        onCompleteRunnable.run();
    }

    public void completeSinkOperationWithFailure(
        SinkOperation sinkOperationContext,
        Exception exception,
        Runnable onCompleteRunnable) {

        sinkOperationContext.setException(exception);
        sinkOperationContext.complete();
        onCompleteRunnable.run();

        this.sendToDlqIfConfigured(sinkOperationContext);
    }

    private static class BulkOperationFailedException extends CosmosException {
        protected BulkOperationFailedException(int statusCode, int subStatusCode, String message, Throwable cause) {
            super(statusCode, message, null, cause);
            BridgeInternal.setSubStatusCode(this, subStatusCode);
        }
    }

    private static class KafkaCosmosEmitFailureHandler implements Sinks.EmitFailureHandler {

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED)) {
                LOGGER.debug("emitFailureHandler - Signal: {}, Result: {}", signalType, emitResult.toString());
                return true;
            } else {
                LOGGER.error("emitFailureHandler - Signal: {}, Result: {}", signalType, emitResult.toString());
                return false;
            }
        }
    }
}

