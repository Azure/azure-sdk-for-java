// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosSchedulers;
import com.azure.cosmos.kafka.connect.implementation.sink.patch.KafkaCosmosPatchHelper;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosPointWriter extends CosmosWriterBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosPointWriter.class);

    private final CosmosSinkWriteConfig writeConfig;
    private final CosmosThroughputControlConfig throughputControlConfig;

    public CosmosPointWriter(
        CosmosSinkWriteConfig writeConfig,
        CosmosThroughputControlConfig throughputControlConfig,
        ErrantRecordReporter errantRecordReporter) {
        super(errantRecordReporter);
        checkNotNull(writeConfig, "Argument 'writeConfig' can not be null");
        this.writeConfig = writeConfig;
        this.throughputControlConfig = throughputControlConfig;
    }

    @Override
    public void writeCore(CosmosAsyncContainer container, List<SinkOperation> sinkOperations) {
        for (SinkOperation sinkOperation : sinkOperations) {
            switch (this.writeConfig.getItemWriteStrategy()) {
                case ITEM_OVERWRITE:
                    this.upsertWithRetry(container, sinkOperation);
                    break;
                case ITEM_OVERWRITE_IF_NOT_MODIFIED:
                    String etag = this.getEtag(sinkOperation.getSinkRecord().value());
                    if (StringUtils.isNotEmpty(etag)) {
                        this.replaceIfNotModifiedWithRetry(container, sinkOperation, etag);
                    } else {
                        this.createWithRetry(container, sinkOperation);
                    }
                    break;
                case ITEM_APPEND:
                    this.createWithRetry(container, sinkOperation);
                    break;
                case ITEM_DELETE:
                    this.deleteWithRetry(container, sinkOperation, false);
                    break;
                case ITEM_DELETE_IF_NOT_MODIFIED:
                    this.deleteWithRetry(container, sinkOperation, true);
                    break;
                case ITEM_PATCH:
                    this.patchWithRetry(container, sinkOperation);
                    break;
                default:
                    throw new IllegalArgumentException(this.writeConfig.getItemWriteStrategy() + " is not supported");
            }
        }
    }

    private void upsertWithRetry(CosmosAsyncContainer container, SinkOperation sinkOperation) {
        executeWithRetry(
            (operation) -> {
                CosmosItemRequestOptions cosmosItemRequestOptions = this.getCosmosItemRequestOptions();
                return container.upsertItem(operation.getSinkRecord().value(), cosmosItemRequestOptions).then();
            },
            (throwable) -> false, // no exceptions should be ignored
            sinkOperation
        );
    }

    private void createWithRetry(CosmosAsyncContainer container, SinkOperation sinkOperation) {
        executeWithRetry(
            (operation) -> {
                CosmosItemRequestOptions cosmosItemRequestOptions = this.getCosmosItemRequestOptions();
                return container.createItem(operation.getSinkRecord().value(), cosmosItemRequestOptions).then();
            },
            (throwable) -> KafkaCosmosExceptionsHelper.isResourceExistsException(throwable),
            sinkOperation
        );
    }

    private void replaceIfNotModifiedWithRetry(CosmosAsyncContainer container, SinkOperation sinkOperation, String etag) {
        executeWithRetry(
            (operation) -> {
                CosmosItemRequestOptions itemRequestOptions = this.getCosmosItemRequestOptions();
                itemRequestOptions.setIfMatchETag(etag);

                return ImplementationBridgeHelpers
                        .CosmosAsyncContainerHelper
                        .getCosmosAsyncContainerAccessor()
                        .getPartitionKeyDefinition(container)
                    .flatMap(partitionKeyDefinition -> {
                        return container.replaceItem(
                                operation.getSinkRecord().value(),
                                getId(operation.getSinkRecord().value()),
                                getPartitionKeyValue(operation.getSinkRecord().value(), partitionKeyDefinition),
                                itemRequestOptions).then();
                    });
            },
            (throwable) -> {
                return KafkaCosmosExceptionsHelper.isNotFoundException(throwable)
                    || KafkaCosmosExceptionsHelper.isPreconditionFailedException(throwable);
            },
            sinkOperation
        );
    }

    private void deleteWithRetry(CosmosAsyncContainer container, SinkOperation sinkOperation, boolean onlyIfModified) {
        executeWithRetry(
            (operation) -> {
                CosmosItemRequestOptions itemRequestOptions = this.getCosmosItemRequestOptions();
                if (onlyIfModified) {
                    String etag = this.getEtag(operation.getSinkRecord().value());
                    if (StringUtils.isNotEmpty(etag)) {
                        itemRequestOptions.setIfMatchETag(etag);
                    }
                }

                return ImplementationBridgeHelpers
                        .CosmosAsyncContainerHelper
                        .getCosmosAsyncContainerAccessor()
                        .getPartitionKeyDefinition(container)
                    .flatMap(partitionKeyDefinition -> {
                        return container.deleteItem(
                            getId(operation.getSinkRecord().value()),
                            getPartitionKeyValue(operation.getSinkRecord().value(), partitionKeyDefinition),
                            itemRequestOptions
                        );
                    }).then();
            },
            (throwable) -> {
                return KafkaCosmosExceptionsHelper.isNotFoundException(throwable)
                    || KafkaCosmosExceptionsHelper.isPreconditionFailedException(throwable);
            },
            sinkOperation
        );
    }

    private void patchWithRetry(CosmosAsyncContainer container, SinkOperation sinkOperation) {
        executeWithRetry(
            (operation) -> {
                CosmosPatchItemRequestOptions patchItemRequestOptions = new CosmosPatchItemRequestOptions();
                CosmosThroughputControlHelper.tryPopulateThroughputControlGroupName(patchItemRequestOptions, this.throughputControlConfig);
                if (StringUtils.isNotEmpty(this.writeConfig.getCosmosPatchConfig().getFilter())) {
                    patchItemRequestOptions.setFilterPredicate(this.writeConfig.getCosmosPatchConfig().getFilter());
                }

                return ImplementationBridgeHelpers
                    .CosmosAsyncContainerHelper
                    .getCosmosAsyncContainerAccessor()
                    .getPartitionKeyDefinition(container)
                    .flatMap(partitionKeyDefinition -> {
                        String itemId = this.getId(sinkOperation.getSinkRecord().value());
                        CosmosPatchOperations cosmosPatchOperations = KafkaCosmosPatchHelper.createCosmosPatchOperations(
                            itemId,
                            partitionKeyDefinition,
                            sinkOperation.getSinkRecord(),
                            this.writeConfig.getCosmosPatchConfig());

                        return container
                            .patchItem(
                                itemId,
                                getPartitionKeyValue(operation.getSinkRecord().value(), partitionKeyDefinition),
                                cosmosPatchOperations,
                                patchItemRequestOptions,
                                ObjectNode.class);
                    }).then();
            },
            (throwable) -> false, // no exceptions should be ignored
            sinkOperation
        );
    }

    private void executeWithRetry(
        Function<SinkOperation, Mono<Void>> execution,
        Function<Throwable, Boolean> shouldIgnoreFunc,
        SinkOperation sinkOperation) {

        Mono.just(this)
            .flatMap(data -> {
                if (sinkOperation.getRetryCount() > 0) {
                    LOGGER.debug("Retry for sinkRecord {}", sinkOperation.getSinkRecord().key());
                }
                return execution.apply(sinkOperation);
            })
            .doOnSuccess(response -> sinkOperation.complete())
            .onErrorResume(throwable -> {
                if (shouldIgnoreFunc.apply(throwable)) {
                    sinkOperation.complete();
                    return Mono.empty();
                }

                if (shouldRetry(throwable, sinkOperation.getRetryCount(), this.writeConfig.getMaxRetryCount())) {
                    sinkOperation.setException(throwable);
                    sinkOperation.retry();

                    return Mono.empty();
                } else {
                    // request failed after exhausted all retries
                    this.sendToDlqIfConfigured(sinkOperation);

                    sinkOperation.setException(throwable);
                    sinkOperation.complete();

                    if (this.writeConfig.getToleranceOnErrorLevel() == ToleranceOnErrorLevel.ALL) {
                        LOGGER.warn(
                            "Could not upload record {} to CosmosDB after exhausting all retries, but ToleranceOnErrorLevel is all, will only log the error message. ",
                            sinkOperation.getSinkRecord().key(),
                            sinkOperation.getException());
                        return Mono.empty();
                    } else {
                        return Mono.error(sinkOperation.getException());
                    }
                }
            })
            .repeat(() -> !sinkOperation.isCompleted())
            .then()
            .subscribeOn(KafkaCosmosSchedulers.SINK_BOUNDED_ELASTIC)
            .block();
    }

    private CosmosItemRequestOptions getCosmosItemRequestOptions() {
        CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
        CosmosThroughputControlHelper.tryPopulateThroughputControlGroupName(itemRequestOptions, this.throughputControlConfig);
        return itemRequestOptions;
    }
}

