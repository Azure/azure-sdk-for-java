package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.kafka.connect.implementation.CosmosExceptionsHelper;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import org.apache.kafka.connect.sink.SinkRecord;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class KafkaCosmosPointWriter extends KafkaCosmosWriterBase {
    private final CosmosSinkWriteConfig writeConfig;
    private final ToleranceOnErrorLevel toleranceOnErrorLevel;

    public KafkaCosmosPointWriter(CosmosSinkWriteConfig writeConfig, ToleranceOnErrorLevel toleranceOnErrorLevel) {
        checkNotNull(writeConfig, "Argument 'writeConfig' can not be null");
        this.writeConfig = writeConfig;
        this.toleranceOnErrorLevel = toleranceOnErrorLevel;
    }

    @Override
    public SinkWriteResponse write(CosmosAsyncContainer container, List<SinkRecord> sinkRecords) {
        checkNotNull(sinkRecords, "Argument 'sinkRecords' should not be null");
        SinkWriteResponse sinkWriteResponse = new SinkWriteResponse();

        for (SinkRecord sinkRecord : sinkRecords) {
            SinkOperationContext sinkOperationContext = new SinkOperationContext(sinkRecord);
            switch (this.writeConfig.getItemWriteStrategy()) {
                case ITEM_OVERWRITE:
                    this.upsertWithRetry(container, sinkOperationContext);
                    break;
                case ITEM_OVERWRITE_IF_NOT_MODIFIED:
                    String etag = this.getEtag(sinkRecord.value());
                    if (StringUtils.isNotEmpty(etag)) {
                        this.replaceIfNotModifiedWithRetry(container, sinkOperationContext, etag);
                    } else {
                        this.createWithRetry(container, sinkOperationContext);
                    }
                    break;
                case ITEM_APPEND:
                    this.createWithRetry(container, sinkOperationContext);
                    break;
                case ITEM_DELETE:
                    this.deleteWithRetry(container, sinkOperationContext, false);
                    break;
                case ITEM_DELETE_IF_NOT_MODIFIED:
                    this.deleteWithRetry(container, sinkOperationContext, true);
                    break;
                default:
                    throw new IllegalArgumentException(this.writeConfig.getItemWriteStrategy() + " is not supported");
            }

            if (sinkOperationContext.getIsSucceeded()) {
                sinkWriteResponse.getSucceededRecords().add(sinkRecord);
            } else {
                sinkWriteResponse.getFailedRecordResponses().add(new SinkOperationFailedResponse(sinkRecord, sinkOperationContext.getException()));
                if (this.toleranceOnErrorLevel == ToleranceOnErrorLevel.NONE) {
                    // if there is no tolerance, then fail fast on the first exception
                    return sinkWriteResponse;
                }
            }
        }

        //We will only reach here is all the operations have succeeded or the ToleranceOnErrorLevel is ALL
        return sinkWriteResponse;
    }

    private void upsertWithRetry(CosmosAsyncContainer container, SinkOperationContext context) {
        executeWithRetry(
            (sinkOperationContext) -> container.upsertItem(context.getSinkRecord().value()).then(),
            (throwable) -> false, // no exceptions should be ignored
            context
        );
    }

    private void createWithRetry(CosmosAsyncContainer container, SinkOperationContext context) {
        executeWithRetry(
            (sinkOperationContext) -> container.createItem(context.getSinkRecord().value()).then(),
            (throwable) -> CosmosExceptionsHelper.isResourceExistsException(throwable),
            context
        );
    }

    private void replaceIfNotModifiedWithRetry(CosmosAsyncContainer container, SinkOperationContext context, String etag) {
        executeWithRetry(
            (sinkOperationContext) -> {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
                itemRequestOptions.setIfMatchETag(etag);

                return this.getPartitionKeyDefinition(container)
                        .flatMap(partitionKeyDefinition -> {
                            return container.replaceItem(
                                context.getSinkRecord().value(),
                                getId(context.getSinkRecord().value()),
                                getPartitionKeyValue(context.getSinkRecord().value(), partitionKeyDefinition),
                                itemRequestOptions).then();
                        });
            },
            (throwable) -> {
                return CosmosExceptionsHelper.isResourceExistsException(throwable) ||
                    CosmosExceptionsHelper.isNotFoundException(throwable) ||
                    CosmosExceptionsHelper.isPreconditionFailedException(throwable);
            },
            context
        );
    }

    private void deleteWithRetry(CosmosAsyncContainer container, SinkOperationContext context, boolean onlyIfModified) {
        executeWithRetry(
            (sinkOperationContext) -> {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
                if (onlyIfModified) {
                    String etag = this.getEtag(context.getSinkRecord().value());
                    if (StringUtils.isNotEmpty(etag)) {
                        itemRequestOptions.setIfMatchETag(etag);
                    }
                }

                return this.getPartitionKeyDefinition(container)
                    .flatMap(partitionKeyDefinition -> {
                        return container.deleteItem(
                            getId(context.getSinkRecord().value()),
                            getPartitionKeyValue(context.getSinkRecord().value(), partitionKeyDefinition),
                            itemRequestOptions
                        );
                    }).then();
            },
            (throwable) -> CosmosExceptionsHelper.isNotFoundException(throwable),
            context
        );
    }

    private void executeWithRetry(
        Function<SinkOperationContext, Mono<Void>> execution,
        Function<Throwable, Boolean> shouldIgnoreFunc,
        SinkOperationContext operationContext) {

        Mono.just(this)
            .flatMap(data -> execution.apply(operationContext))
            .doOnNext(response -> operationContext.setSucceeded())
            .onErrorResume(throwable -> {
                if (shouldIgnoreFunc.apply(throwable)) {
                    operationContext.setSucceeded();
                    operationContext.complete();
                    return Mono.empty();
                }

                operationContext.setException(throwable);
                if (shouldRetry(throwable, operationContext.getRetryCount(), this.writeConfig.getMaxRetryCount())) {
                    return Mono.empty();
                }

                operationContext.complete();
                return Mono.empty();
            })
            .repeat(() -> !operationContext.isCompleted())
            .then()
            .subscribeOn(Schedulers.boundedElastic())
            .block();// TODO: use customized schedulers
    }
}

