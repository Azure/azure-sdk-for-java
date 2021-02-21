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
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactory;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactoryCore;
import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.encryption.EncryptionUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.models.EncryptionModelBridgeInternal.createEncryptionItemResponse;


// TODO: for now basic functionality is in. some APIs and some logic branch is not complete yet.
// TODO: should we test the apis for byte-array (streaming api replacement)?
public class EncryptionCosmosAsyncContainer {
    private final Scheduler encryptionScheduler;
    private final Encryptor encryptor;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactoryCore();
    private final CosmosAsyncContainer container;

    EncryptionCosmosAsyncContainer(String id, CosmosAsyncDatabase database, Encryptor encryptor) {
        this.container = BridgeInternal.createCosmosAsyncContainer(id, database);
        this.encryptor = encryptor;
        this.encryptionScheduler = Schedulers.parallel();
    }

    private Mono<CosmosItemResponse<byte[]>> createItemHelper(
        byte[] streamPayload,
        PartitionKey partitionKey,
        CosmosItemRequestOptions requestOptions,
        boolean decryptResponse) {

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
            EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions == null ||
            encryptionItemRequestOptions.getEncryptionOptions() == null) {
            return this.container.createItem(
                streamPayload,
                partitionKey,
                requestOptions);
        }

        Mono<byte[]> encryptedPayloadMono = EncryptionProcessor.encrypt(
            streamPayload,
            this.encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        return encryptedPayloadMono.flatMap(
            encryptedPayload ->
                this.container.createItem(
                    encryptedPayload,
                    partitionKey,
                    requestOptions)
                              .publishOn(encryptionScheduler)
        ).flatMap(
            rsp -> {
                if (decryptResponse) {
                    return setByteArrayContent(rsp,
                        EncryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                            this.encryptor).map(Pair::getLeft).publishOn(encryptionScheduler));
                }

                return Mono.just(rsp);
            }
        );
    }

    private Mono<CosmosItemResponse<byte[]>> upsertItemHelper(
        byte[] streamPayload,
        PartitionKey partitionKey,
        CosmosItemRequestOptions requestOptions,
        boolean decryptResponse) {

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
            EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions == null ||
            encryptionItemRequestOptions.getEncryptionOptions() == null) {
            return this.container.upsertItem(
                streamPayload,
                partitionKey,
                requestOptions);
        }

        Mono<byte[]> encryptedPayloadMono = EncryptionProcessor.encrypt(
            streamPayload,
            this.encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        return encryptedPayloadMono.flatMap(
            encryptedPayload ->
                this.container.upsertItem(
                    encryptedPayload,
                    partitionKey,
                    requestOptions)
                              .publishOn(encryptionScheduler)
        ).flatMap(
            rsp -> {
                if (decryptResponse) {
                    return setByteArrayContent(rsp,
                        EncryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                            this.encryptor).map(Pair::getLeft).publishOn(encryptionScheduler));
                }

                return Mono.just(rsp);
            }
        );
    }

    private Mono<CosmosItemResponse<byte[]>> replaceItemHelper(
        byte[] streamPayload,
        String itemId,
        PartitionKey partitionKey,
        CosmosItemRequestOptions requestOptions,
        boolean decryptResponse) {

        EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
            EncryptionItemRequestOptions.class);

        if (encryptionItemRequestOptions == null ||
            encryptionItemRequestOptions.getEncryptionOptions() == null) {
            return this.container.replaceItem(
                streamPayload,
                itemId,
                partitionKey,
                requestOptions);
        }

        Mono<byte[]> encryptedPayloadMono = EncryptionProcessor.encrypt(
            streamPayload,
            this.encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        return encryptedPayloadMono.flatMap(
            encryptedPayload ->
                this.container.replaceItem(
                    encryptedPayload,
                    itemId,
                    partitionKey,
                    requestOptions)
                              .publishOn(encryptionScheduler)
        ).flatMap(
            rsp -> {
                if (decryptResponse) {
                    return setByteArrayContent(rsp,
                        EncryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                            this.encryptor).map(Pair::getLeft).publishOn(encryptionScheduler));
                }

                return Mono.just(rsp);
            }
        );
    }

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

        if (encryptionItemRequestOptions == null || encryptionItemRequestOptions.getEncryptionOptions() == null) {
            return container.createItem(item, partitionKey, requestOptions);
        }

        Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
            + "EncryptionContainer.");


        EncryptableItem encryptableItem = Utils.as(item, EncryptableItem.class);
        if (encryptableItem != null) {
            byte[] streamPayload = encryptableItem.toStream(this.getItemDeserializer());

            Mono<CosmosItemResponse<byte[]>> rspMono = this.createItemHelper(streamPayload,
                partitionKey,
                requestOptions,
                false);


            return rspMono.map(
                rsp -> {
                    encryptableItem.setDecryptableItem(
                        // EncryptionProcessor.BaseSerializer.FromStream<JObject>(responseMessage.Content),
                        getItemDeserializer().parseFrom(ObjectNode.class,
                            EncryptionModelBridgeInternal.getByteArrayContent(rsp)),
                        this.encryptor,
                        this.getItemDeserializer()
                    );

                    return createEncryptionItemResponse(rsp, (T) encryptableItem);
                }
            ).subscribeOn(encryptionScheduler);

        } else {
            byte[] streamPayload = cosmosSerializerToStream(item);
            Mono<CosmosItemResponse<byte[]>> response = createItemHelper(
                streamPayload,
                partitionKey,
                requestOptions,
                true
            );

            return response
                .publishOn(encryptionScheduler)
                .map(
                    rsp -> {
                        return this.responseFactory.createItemResponse(rsp, (Class<T>) item.getClass());
                    }
                );
        }
    }


    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon successful completion will contain a
     * single Cosmos item response with the deleted item.
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

        if (encryptionItemRequestOptions == null || encryptionItemRequestOptions.getEncryptionOptions() == null) {
            return container.upsertItem(item, partitionKey, requestOptions);
        }

        Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
            + "EncryptionContainer.");

        EncryptableItem encryptableItem = Utils.as(item, EncryptableItem.class);
        if (encryptableItem != null) {

            // TODO: serialize or deserialize?
            //                     using (Stream streamPayload = encryptableItem.ToStream(this.CosmosSerializer))
            byte[] streamPayload = encryptableItem.toStream(this.getItemDeserializer());

            Mono<CosmosItemResponse<byte[]>> rspMono = this.upsertItemHelper(streamPayload,
                partitionKey,
                requestOptions,
                false);


            return rspMono.map(
                rsp -> {
                    encryptableItem.setDecryptableItem(
                        //EncryptionProcessor.BaseSerializer.FromStream<JObject>(responseMessage.Content),
                        getItemDeserializer().parseFrom(ObjectNode.class,
                            EncryptionModelBridgeInternal.getByteArrayContent(rsp)),
                        this.encryptor,
                        this.getItemDeserializer()
                    );

                    return createEncryptionItemResponse(rsp, (T) encryptableItem);
                }
            ).subscribeOn(encryptionScheduler);

        } else {
            byte[] streamPayload = cosmosSerializerToStream(item);
            Mono<CosmosItemResponse<byte[]>> response = upsertItemHelper(
                streamPayload,
                partitionKey,
                requestOptions,
                true
            );

            return response
                .publishOn(encryptionScheduler)
                .map(
                    rsp -> {
                        return this.responseFactory.createItemResponse(rsp, (Class<T>) item.getClass());
                    }
                );
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

        if (encryptionItemRequestOptions == null || encryptionItemRequestOptions.getEncryptionOptions() == null) {
            return container.replaceItem(item, itemId, partitionKey, requestOptions);
        }

        Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
            + "EncryptionContainer.");


        EncryptableItem encryptableItem = Utils.as(item, EncryptableItem.class);
        if (encryptableItem != null) {

            // using (Stream streamPayload = encryptableItem.ToStream(this.CosmosSerializer))
            byte[] streamPayload = encryptableItem.toStream(this.getItemDeserializer());

            Mono<CosmosItemResponse<byte[]>> rspMono = this.replaceItemHelper(streamPayload,
                itemId,
                partitionKey,
                requestOptions,
                false);


            return rspMono.map(
                rsp -> {
                    encryptableItem.setDecryptableItem(
                        //EncryptionProcessor.BaseSerializer.FromStream<JObject>(responseMessage.Content),
                        getItemDeserializer().parseFrom(ObjectNode.class,
                            EncryptionModelBridgeInternal.getByteArrayContent(rsp)),
                        this.encryptor,
                        this.getItemDeserializer()
                    );

                    return createEncryptionItemResponse(rsp, (T) encryptableItem);
                }
            ).subscribeOn(encryptionScheduler);

        } else {
            byte[] streamPayload = cosmosSerializerToStream(item);
            Mono<CosmosItemResponse<byte[]>> response = replaceItemHelper(
                streamPayload,
                itemId,
                partitionKey,
                requestOptions,
                true
            );

            return response
                .publishOn(encryptionScheduler)
                .map(
                    rsp -> {
                        return this.responseFactory.createItemResponse(rsp, (Class<T>) item.getClass());
                    }
                );
        }
    }

    private Mono<CosmosItemResponse<byte[]>> readItemHelper(
        String id,
        PartitionKey partitionKey,
        CosmosItemRequestOptions requestOptions,
        boolean decryptResponse) {
        Mono<CosmosItemResponse<byte[]>> responseMessageMono = this.container.readItem(
            id,
            partitionKey,
            requestOptions, byte[].class);


        return responseMessageMono
            .publishOn(encryptionScheduler)
            .flatMap(
                rsp -> {
                    if (decryptResponse) {
                        return setByteArrayContent(rsp,
                            EncryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                                this.encryptor).map(pair -> pair.getLeft()).publishOn(encryptionScheduler));
                    }

                    return Mono.just(rsp);
                }
            );
    }

    /**
     * Reads item and decrypt the encrypted fields
     *
     * @param id item id
     * @param partitionKey the partition key.
     * @param requestOptions request options
     * @param classType deserialization class type
     * @param <T> type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String id,
                                                    PartitionKey partitionKey,
                                                    CosmosItemRequestOptions requestOptions,
                                                    Class<T> classType) {

        if (DecryptableItem.class == classType) {

            Mono<CosmosItemResponse<byte[]>> responseMessageMono = this.readItemHelper(
                id,
                partitionKey,
                requestOptions,
                false);

            return responseMessageMono.map(
                responseMessage -> {

                    DecryptableItemCore decryptableItem = new DecryptableItemCore(
                        this.getItemDeserializer().parseFrom(ObjectNode.class, responseMessage.getItem()),
                        // TODO EncryptionProcessor.BaseSerializer.FromStream<JObject>(responseMessage.Content),
                        this.encryptor,
                        this.getItemDeserializer());

                    return createEncryptionItemResponse(
                        responseMessage,
                        (T) decryptableItem);

                }
            );
        }

        // else
        // stream? TODO: is this meaningful for java?
        Mono<CosmosItemResponse<byte[]>> responseMessageMono = this.readItemHelper(
            id,
            partitionKey,
            requestOptions,
            true);


        return responseMessageMono.map(
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

        return CosmosBridgeInternal.queryItemsInternal(container, query, options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, func, false);
                }
            });
    }

    /**
     * Query for items in the change feed of the current container using the {@link CosmosChangeFeedRequestOptions}.
     * <p>
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param options the change feed request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained
     * items or an error.
     */
    public <T> CosmosPagedFlux<T> queryChangeFeed(
        CosmosChangeFeedRequestOptions options,
        Class<T> classType) {

        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        return CosmosBridgeInternal.queryChangeFeedInternal(container, options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, func, true);
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

    private <T> Mono<byte[]> decryptResponse(
        byte[] input) {

        if (input == null) {
            return Mono.empty();
        }

        return EncryptionProcessor.decrypt(
            input,
            this.encryptor).map(pair -> pair.getLeft());
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

    private <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryDecryptionTransformer(
        Class<T> classType,
        Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func,
        boolean useEtagAsContinuation) {

        return func.andThen(flux ->
            flux.publishOn(encryptionScheduler)
                .flatMap(
                    page -> {
                        List<byte[]> byteArrayList = page.getResults().stream()
                                                         .map(node -> cosmosSerializerToStream(node))
                                                         .collect(Collectors.toList());

                        if (classType == DecryptableItem.class) {
                            List<T> itemList = new ArrayList<>(byteArrayList.size());

                            for (byte[] bytes : byteArrayList) {
                                // TODO: there is room for further optimizing this
                                DecryptableItemCore decryptableItem =
                                    new DecryptableItemCore(getItemDeserializer().parseFrom(JsonNode.class, bytes),
                                        encryptor, this.getItemDeserializer());
                                itemList.add((T) decryptableItem);
                            }

                            return Mono.just(ModelBridgeInternal.createFeedResponseWithQueryMetrics(itemList,
                                page.getResponseHeaders(),
                                BridgeInternal.queryMetricsFromFeedResponse(page),
                                ModelBridgeInternal.getQueryPlanDiagnosticsContext(page),
                                useEtagAsContinuation,
                                ModelBridgeInternal.noChanges(page)));
                        } else {
                            List<Mono<byte[]>> byteArrayMonoList =
                                byteArrayList.stream().map(bytes -> decryptResponse(bytes)).collect(Collectors.toList());
                            return Flux.concat(byteArrayMonoList).map(
                                item -> {
                                    return getItemDeserializer().parseFrom(classType, item);
                                }
                            ).collectList().map(itemList ->
                                ModelBridgeInternal.createFeedResponseWithQueryMetrics(itemList,
                                    page.getResponseHeaders(),
                                    BridgeInternal.queryMetricsFromFeedResponse(page),
                                    ModelBridgeInternal.getQueryPlanDiagnosticsContext(page),
                                    useEtagAsContinuation,
                                    ModelBridgeInternal.noChanges(page))
                            );
                        }
                    }
                )
        );
    }
}

