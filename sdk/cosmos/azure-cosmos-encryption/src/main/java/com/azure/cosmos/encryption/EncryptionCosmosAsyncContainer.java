// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.encryption;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactory;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactoryCore;
import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.encryption.EncryptionUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionModelBridgeInternal;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


// TODO: for now basic functionality is in. some APIs and some logic branch is not complete yet.
public class EncryptionCosmosAsyncContainer {
    private final Scheduler encryptionScheduler;
    private final Encryptor encryptor;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactoryCore();
    private final CosmosAsyncContainer container;

    EncryptionCosmosAsyncContainer(String id, CosmosAsyncDatabase database, Encryptor encryptor) {
        this.container = BridgeInternal.createCosmosAsyncContainer(id, database);
        this.encryptor = encryptor;
        // TODO: moderakh once EncryptionKeyWrapProvider apis are made async this should change to Schedulers.parallel()
        this.encryptionScheduler = Schedulers.boundedElastic();
    }

    private Mono<CosmosItemResponse<byte[]>> createItemStream(byte[] payload,
                                                              PartitionKey partitionKey,
                                                              EncryptionItemRequestOptions encryptionItemRequestOptions) {
        Preconditions.checkNotNull(payload, "payload can't be null");

        // TODO: add diagnostics
        assert encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null;
        Mono<byte[]> encryptedPayloadMono = EncryptionProcessor.encryptAsync(payload, encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        return encryptedPayloadMono.flatMap(
            encryptedPayload -> {
                Mono<CosmosItemResponse<byte[]>> response = container.createItem(encryptedPayload, partitionKey,
                    encryptionItemRequestOptions);

                return response
                    .publishOn(encryptionScheduler)
                    .flatMap(rsp ->
                        setByteArrayContent(
                            rsp,
                            decryptResponseAsync(
                                EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                                encryptionItemRequestOptions.getDecryptionResultHandler()))
                    );
            }
        );
    }

    private Mono<CosmosItemResponse<byte[]>> replaceItemStream(byte[] payload,
                                                               String itemId,
                                                               PartitionKey partitionKey,
                                                               EncryptionItemRequestOptions encryptionItemRequestOptions) {
        Preconditions.checkNotNull(payload, "payload can't be null");

        // TODO: add diagnostics
        assert encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null;
        Mono<byte[]> encryptedPayloadMono = EncryptionProcessor.encryptAsync(payload, encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        return encryptedPayloadMono.flatMap(
            encryptedPayload -> {
                Mono<CosmosItemResponse<byte[]>> response = container.replaceItem(encryptedPayload,
                    itemId,
                    partitionKey,
                    encryptionItemRequestOptions);

                return response
                    .publishOn(encryptionScheduler)
                    .flatMap(rsp ->
                        setByteArrayContent(
                            rsp,
                            decryptResponseAsync(
                                EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                                encryptionItemRequestOptions.getDecryptionResultHandler()))

                    );
            }
        );
    }

    private Mono<CosmosItemResponse<byte[]>> upsertItemStream(byte[] payload,
                                                              PartitionKey partitionKey,
                                                              EncryptionItemRequestOptions encryptionItemRequestOptions) {
        Preconditions.checkNotNull(payload, "payload can't be null");

        // TODO: add diagnostics
        assert encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null;


        Mono<byte[]> encryptedPayloadMono = EncryptionProcessor.encryptAsync(payload, encryptor,
            encryptionItemRequestOptions.getEncryptionOptions()).subscribeOn(encryptionScheduler);

        return encryptedPayloadMono.flatMap(
            encryptedPayload -> {
                Mono<CosmosItemResponse<byte[]>> response = container.upsertItem(encryptedPayload, partitionKey,
                    encryptionItemRequestOptions);

                return response
                    .publishOn(encryptionScheduler)
                    .flatMap(rsp ->
                        setByteArrayContent(
                            rsp,
                            decryptResponseAsync(
                                EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                                encryptionItemRequestOptions.getDecryptionResultHandler()))
                    );
            }
        );
    }

    // TODO ensure all other apis call this guy

    /**
     * create item and encrypts the requested fields
     *
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey the partition key.
     * @param requestOptions request option
     * @param <T> serialization class type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(T item,
                                                      PartitionKey partitionKey,
                                                      CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
            EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null) {
            Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
                + "EncryptionContainer.");

            return Mono.defer(() -> {
                byte[] payload = cosmosSerializerToStream(item);
                Mono<CosmosItemResponse<byte[]>> result = this.createItemStream(payload, partitionKey,
                    encryptionItemRequestOptions);

                return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp,
                    item.getClass()));

            }).subscribeOn(encryptionScheduler);
        } else {
            return container.createItem(item, partitionKey, requestOptions);
        }
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the deleted item.
     *
     * @param itemId id of the item.
     * @param partitionKey partitionKey of the item.
     * @param options the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteItem(String itemId,
                                                       PartitionKey partitionKey,
                                                       CosmosItemRequestOptions options) {

        return container.deleteItem(itemId, partitionKey, options);
    }


    /**
     * upserts item and encrypts the requested fields
     *
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey the partition key.
     * @param requestOptions request option
     * @param <T> serialization class type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item,
                                                      PartitionKey partitionKey,
                                                      CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
            EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null) {
            Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
                + "EncryptionContainer.");

            return Mono.defer(() -> {
                byte[] payload = cosmosSerializerToStream(item);
                Mono<CosmosItemResponse<byte[]>> result = this.upsertItemStream(payload, partitionKey,
                    encryptionItemRequestOptions);

                return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp,
                    item.getClass()));

            }).subscribeOn(encryptionScheduler);

        } else {
            return container.upsertItem(item, partitionKey, requestOptions);
        }
    }

    /**
     * replaces item and encrypts the requested fields
     *
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param requestOptions request option
     * @param <T> serialization class type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> replaceItem(T item,
                                                       String itemId,
                                                       PartitionKey partitionKey,
                                                       CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
            EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null) {
            Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
                + "EncryptionContainer.");

            return Mono.defer(() -> {
                byte[] payload = cosmosSerializerToStream(item);
                Mono<CosmosItemResponse<byte[]>> result = this.replaceItemStream(payload,
                    itemId,
                    partitionKey,
                    encryptionItemRequestOptions);

                return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp,
                    item.getClass()));
            }).subscribeOn(encryptionScheduler);

        } else {
            return container.replaceItem(item, itemId, partitionKey, requestOptions);
        }
    }

    private Mono<CosmosItemResponse<byte[]>> readItemStream(String id,
                                                            PartitionKey partitionKey,
                                                            CosmosItemRequestOptions requestOptions) {


        Mono<CosmosItemResponse<byte[]>> responseMessageAsync = container.readItem(id, partitionKey, requestOptions,
            byte[].class);

        return responseMessageAsync
            .publishOn(encryptionScheduler)
            .flatMap(
                responseMessage -> {
                    Consumer<DecryptionResult> decryptionErroHandler = null;
                    EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
                        EncryptionItemRequestOptions.class);

                    if (encryptionItemRequestOptions != null) {
                        decryptionErroHandler = encryptionItemRequestOptions.getDecryptionResultHandler();
                    }

                    return setByteArrayContent(responseMessage, this.decryptResponseAsync(
                        EncryptionModelBridgeInternal.getByteArrayContent(responseMessage), decryptionErroHandler));
                }
            );
    }

    /**
     * Reads item and decrypt the encrypted fields
     *
     * @param id item id
     * @param partitionKey the partition key.
     * @param option request options
     * @param classType deserialization class type
     * @param <T> type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String id,
                                                    PartitionKey partitionKey,
                                                    CosmosItemRequestOptions option,
                                                    Class<T> classType) {
        Mono<CosmosItemResponse<byte[]>> asyncResult = readItemStream(id, partitionKey, option);

        return asyncResult.map(
            responseMessage -> this.responseFactory.createItemResponse(responseMessage, classType)
        );
    }

    /**
     * Query for items in the current container using a string.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will contain one or several feed
     * response of the obtained items. In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param options the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec query, CosmosQueryRequestOptions options,
                                          Class<T> classType) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        EncryptionQueryRequestOptions encryptionQueryRequestOptions = Utils.as(options,
            EncryptionQueryRequestOptions.class);

        Consumer<DecryptionResult> decryptionResultConsumer = null;
        if (encryptionQueryRequestOptions != null) {
            decryptionResultConsumer = encryptionQueryRequestOptions.getDecryptionResultHandler();
        }

        final Consumer<DecryptionResult> finalDecryptionResultConsumer = decryptionResultConsumer;

        return CosmosBridgeInternal.queryItemsInternal(container, query, options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, finalDecryptionResultConsumer, func);
                }
            });
    }

    private <T> byte[] cosmosSerializerToStream(T item) {
        // TODO:
        return EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), item);
    }

    ItemDeserializer getItemDeserializer() {
        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase()).getItemDeserializer();
    }

    private Mono<byte[]> decryptResponseAsync(
        byte[] input,
        Consumer<DecryptionResult> decryptionResultHandler) {

        if (input == null) {
            return Mono.empty();
        }

        return EncryptionProcessor.decryptAsync(
            input,
            this.encryptor).onErrorResume(
            throwable -> {
                Exception exception = Utils.as(throwable, Exception.class);

                    if (exception == null || decryptionResultHandler == null) {
                        return Mono.error(throwable);
                    }

                    decryptionResultHandler.accept(
                        DecryptionResult.createFailure(
                            input,
                            exception));

                    return Mono.just(input);
                }
        );
    }

    private Mono<CosmosItemResponse<byte[]>> setByteArrayContent(CosmosItemResponse<byte[]> rsp,
                                                                  Mono<byte[]> bytesMono) {
        return bytesMono.flatMap(
            bytes -> {
                EncryptionModelBridgeInternal.setByteArrayContent(rsp, bytes);
                return Mono.just(rsp);
            }
        );
    }

    private <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryDecryptionTransformer(Class<T> classType,
                                                                                                   Consumer<DecryptionResult> handler,
                                                                                                   Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
        return func.andThen(flux ->
            flux.publishOn(encryptionScheduler)
                .flatMap(
                    page -> {
                        List<Mono<byte[]>> byteArrayMonoList = page.getResults().stream()
                                .map(node -> cosmosSerializerToStream(node))
                                .map(bytes -> decryptResponseAsync(bytes, handler))
                                .collect(Collectors.toList());

                        return Flux.concat(byteArrayMonoList).map(
                            item -> {
                                return getItemDeserializer().parseFrom(classType, item);
                            }
                        ).collectList().map(itemList ->
                            ModelBridgeInternal.createFeedResponseWithQueryMetrics(itemList,
                                page.getResponseHeaders(),
                                BridgeInternal.queryMetricsFromFeedResponse(page),
                                ModelBridgeInternal.getQueryPlanDiagnosticsContext(page))
                        );
                    }
                )
        );
    }
}

