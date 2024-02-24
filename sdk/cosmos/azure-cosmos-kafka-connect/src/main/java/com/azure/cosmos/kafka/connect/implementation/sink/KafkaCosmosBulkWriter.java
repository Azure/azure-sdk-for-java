package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class KafkaCosmosBulkWriter extends KafkaCosmosWriterBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCosmosBulkWriter.class);

    private final CosmosSinkWriteConfig writeConfig;

    public KafkaCosmosBulkWriter(CosmosSinkWriteConfig writeConfig) {
        checkNotNull(writeConfig, "Argument 'writeConfig' can not be null");

        this.writeConfig = writeConfig;
    }

    @Override
    public SinkWriteResponse write(CosmosAsyncContainer container, List<SinkRecord> sinkRecords) {
        LOGGER.debug("Write {} records to container {}", sinkRecords.size(), container.getId());

        SinkWriteResponse sinkWriteResponse = new SinkWriteResponse();

        if (sinkRecords == null || sinkRecords.isEmpty()) {
            return sinkWriteResponse;
        }

        List<SinkOperationContext> sinkOperationContexts =
            sinkRecords
                .stream()
                .map(sinkRecord -> new SinkOperationContext(sinkRecord))
                .collect(Collectors.toList());

        Mono
            .defer(() -> Mono.just(getToBeProcessedSinkOperation(sinkOperationContexts)))
            .flatMap(operationsToBeProcessed -> {
                if (this.writeConfig.getItemWriteStrategy() == ItemWriteStrategy.ITEM_OVERWRITE) {
                    return this.getBulkOperations(container, operationsToBeProcessed)
                        .flatMap(itemOperations -> this.executeBulkOperations(container, itemOperations));
                }

                return Mono.empty();
            })
            .repeat(() -> getToBeProcessedSinkOperation(sinkOperationContexts).size() > 0)// only repeat when there are records still need to be processed
            .blockLast();

        return sinkWriteResponse;
    }

    private List<SinkOperationContext> getToBeProcessedSinkOperation(List<SinkOperationContext> sinkOperationContexts) {
        return sinkOperationContexts
            .stream()
            .filter(
            sinkOperationContext -> {
                return !sinkOperationContext.getIsSucceeded()
                    && shouldRetry(sinkOperationContext.getException(), sinkOperationContext.getRetryCount(), this.writeConfig.getMaxRetryCount());
            })
            .collect(Collectors.toList());
    }

    private Mono<List<CosmosItemOperation>> getBulkOperations(
        CosmosAsyncContainer container,
        List<SinkOperationContext> sinkOperationContexts) {

        return this.getPartitionKeyDefinition(container)
            .flatMap(partitionKeyDefinition -> {
                List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
                switch (this.writeConfig.getItemWriteStrategy()) {
                    case ITEM_OVERWRITE:
                        sinkOperationContexts.forEach(sinkOperationContext ->
                            cosmosItemOperations.add(
                                CosmosBulkOperations.getUpsertItemOperation(
                                    sinkOperationContext.getSinkRecord().value(),
                                    this.getPartitionKeyValue(sinkOperationContext.getSinkRecord().value(), partitionKeyDefinition),
                                    sinkOperationContext
                                )
                            ));
                        break;
                    case ITEM_OVERWRITE_IF_NOT_MODIFIED:
                        sinkOperationContexts.forEach(sinkOperationContext -> {
                            String etag = getEtag(sinkOperationContext.getSinkRecord().value());
                            if (StringUtils.isEmpty(etag)) {
                                cosmosItemOperations.add(
                                    CosmosBulkOperations.getCreateItemOperation(
                                        sinkOperationContext.getSinkRecord().value(),
                                        this.getPartitionKeyValue(sinkOperationContext.getSinkRecord().value(), partitionKeyDefinition),
                                        sinkOperationContext
                                    )
                                );
                            } else {
                                cosmosItemOperations.add(
                                    CosmosBulkOperations.getReplaceItemOperation(
                                        getId(sinkOperationContext.getSinkRecord().value()),
                                        sinkOperationContext.getSinkRecord(),
                                        this.getPartitionKeyValue(sinkOperationContext.getSinkRecord().value(), partitionKeyDefinition),
                                        new CosmosBulkItemRequestOptions().setIfMatchETag(etag),
                                        sinkOperationContext)
                                );
                            }
                        });
                        break;
                    case ITEM_APPEND:
                        sinkOperationContexts.forEach(sinkOperationContext -> {
                            cosmosItemOperations.add(
                                CosmosBulkOperations.getCreateItemOperation(
                                    sinkOperationContext.getSinkRecord().value(),
                                    this.getPartitionKeyValue(sinkOperationContext.getSinkRecord().value(), partitionKeyDefinition),
                                    sinkOperationContext
                                ));

                        });
                        break;
                    case ITEM_DELETE:
                        sinkOperationContexts.forEach(sinkOperationContext -> {
                            cosmosItemOperations.add(
                                CosmosBulkOperations.getDeleteItemOperation(
                                    this.getId(sinkOperationContext.getSinkRecord().value()),
                                    this.getPartitionKeyValue(sinkOperationContext.getSinkRecord().value(), partitionKeyDefinition),
                                    sinkOperationContext
                                ));
                        });
                        break;
                    case ITEM_DELETE_IF_NOT_MODIFIED:
                        sinkOperationContexts.forEach(sinkOperationContext -> {
                            String etag = getEtag(sinkOperationContext.getSinkRecord().value());
                            CosmosBulkItemRequestOptions itemRequestOptions = new CosmosBulkItemRequestOptions();
                            if (StringUtils.isNotEmpty(etag)) {
                                itemRequestOptions.setIfMatchETag(etag);
                            }

                            CosmosBulkOperations.getDeleteItemOperation(
                                getId(sinkOperationContext.getSinkRecord().value()),
                                this.getPartitionKeyValue(sinkOperationContext.getSinkRecord().value(), partitionKeyDefinition),
                                itemRequestOptions,
                                sinkOperationContext);
                        });
                        break;
                    default:
                        return Mono.error(new IllegalArgumentException(this.writeConfig.getItemWriteStrategy() + " is not supported"));
                }

                return Mono.just(cosmosItemOperations);
            });
    }

    private Mono<Void> executeBulkOperations(CosmosAsyncContainer container, List<CosmosItemOperation> cosmosItemOperations) {
        return container
            .executeBulkOperations(Flux.fromIterable(cosmosItemOperations))
            .doOnNext(itemResponse -> {
                SinkOperationContext context = itemResponse.getOperation().getContext();
                checkNotNull(context, "sinkOperationContext should not be null");

                if (itemResponse.getException() != null
                    || itemResponse.getResponse() == null
                    || !itemResponse.getResponse().isSuccessStatusCode()) {

                    BulkOperationFailedException exception = handleErrorStatusCode(
                        itemResponse.getResponse(),
                        itemResponse.getException(),
                        context);

                    context.setException(exception);
                } else {
                    context.setSucceeded();
                }
            })
            .onErrorResume(throwable -> {
                cosmosItemOperations.forEach(cosmosItemOperation -> {
                    ((SinkOperationContext)cosmosItemOperation.getContext()).setException(throwable);
                });

                return Mono.empty();
            })
            .then();
    }

    BulkOperationFailedException handleErrorStatusCode(
        CosmosBulkItemResponse itemResponse,
        Exception exception,
        SinkOperationContext sinkOperationContext) {

        int effectiveStatusCode =
            itemResponse != null
                ? itemResponse.getStatusCode()
                : (exception != null && exception instanceof CosmosException ? ((CosmosException)exception).getStatusCode() : HttpConstants.StatusCodes.REQUEST_TIMEOUT);
        int effectiveSubStatusCode =
            itemResponse != null
                ? itemResponse.getSubStatusCode()
                : (exception != null && exception instanceof CosmosException ? ((CosmosException)exception).getSubStatusCode() : 0);

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

    private static class BulkOperationFailedException extends CosmosException {
        protected BulkOperationFailedException(int statusCode, int subStatusCode, String message, Throwable cause) {
            super(statusCode, message, null, cause);
            BridgeInternal.setSubStatusCode(this, subStatusCode);
        }
    }
}

