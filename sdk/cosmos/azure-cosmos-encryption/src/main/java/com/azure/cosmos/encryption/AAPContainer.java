// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactory;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactoryCore;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.models.EncryptionModelBridgeInternal.createEncryptionItemResponse;
import static com.azure.cosmos.models.EncryptionModelBridgeInternal.getByteArrayContent;

// TODO: for now basic functionality is in. some APIs and some logic branch is not complete yet.
// TODO: should we test the apis for byte-array (streaming api replacement)?
public class AAPContainer {
    private final Scheduler encryptionScheduler;
    private final AlwaysEncrypted encryptor;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactoryCore();
    private final CosmosAsyncContainer container;

    public AAPContainer(CosmosAsyncContainer cosmosAsyncContainer,
                        AlwaysEncrypted alwaysEncrypted) {
        this.container = cosmosAsyncContainer;
        this.encryptor = alwaysEncrypted;
        this.encryptionScheduler = Schedulers.parallel();
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon successful completion will contain a
     * single Cosmos item response with the deleted item.
     *
     * @param itemId id of the item.
     * @param partitionKey partitionKey of the item.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public <T> Mono<CosmosItemResponse<Object>> deleteItem(String itemId,
                                                           PartitionKey partitionKey,
                                                           CosmosItemRequestOptions requestOptions) {
        return this.container.deleteItem(itemId, partitionKey, requestOptions);
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
        Preconditions.checkArgument(partitionKey != null,
            "partitionKey cannot be null for operations using EncryptionContainer.");

        byte[] streamPayload;

        EncryptableItem encryptableItem = Utils.as(item, EncryptableItem.class);
        if (encryptableItem != null) {
            streamPayload = encryptableItem.toStream(this.getItemDeserializer());
        } else {
            streamPayload = cosmosSerializerToStream(item);
        }

        boolean eagerDecryption = (encryptableItem == null);
        Mono<CosmosItemResponse<byte[]>> rspMono = createItemHelper(streamPayload, partitionKey, requestOptions,
            eagerDecryption);

        return parse(rspMono, item.getClass(), encryptableItem);
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
        Preconditions.checkArgument(partitionKey != null,
            "partitionKey cannot be null for operations using EncryptionContainer.");

        byte[] streamPayload;

        EncryptableItem encryptableItem = Utils.as(item, EncryptableItem.class);
        if (encryptableItem != null) {
            streamPayload = encryptableItem.toStream(this.getItemDeserializer());
        } else {
            streamPayload = cosmosSerializerToStream(item);
        }

        boolean eagerDecryption = (encryptableItem == null);
        Mono<CosmosItemResponse<byte[]>> rspMono = replaceItemHelper(streamPayload, itemId, partitionKey,
            requestOptions,
            eagerDecryption);

        return parse(rspMono, item.getClass(), encryptableItem);
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
        Preconditions.checkArgument(partitionKey != null,
            "partitionKey cannot be null for operations using EncryptionContainer.");

        byte[] streamPayload;

        EncryptableItem encryptableItem = Utils.as(item, EncryptableItem.class);
        if (encryptableItem != null) {
            streamPayload = encryptableItem.toStream(this.getItemDeserializer());
        } else {
            streamPayload = cosmosSerializerToStream(item);
        }

        boolean eagerDecryption = (encryptableItem == null);
        Mono<CosmosItemResponse<byte[]>> rspMono = upsertItemHelper(streamPayload, partitionKey, requestOptions,
            eagerDecryption);

        return parse(rspMono, item.getClass(), encryptableItem);
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
                        getByteArrayContent(responseMessage),
                        // TODO EncryptionProcessor.BaseSerializer.FromStream<JObject>(responseMessage.Content),
                        cipherText -> encryptor.decrypt(cipherText),
                        this.getItemDeserializer());

                    return createEncryptionItemResponse(
                        responseMessage,
                        (T) decryptableItem);
                }
            );
        }

        // else
        // stream? TODO: is this meaningful for java?
        return this.readItemHelper(
            id,
            partitionKey,
            requestOptions,
            true
        ).map(
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
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec query,
                                             CosmosQueryRequestOptions options,
                                             Class<T> classType) {

        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return CosmosBridgeInternal.queryItemsInternal(container, query, options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform
                    (Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, func);
                }
            });
    }

    private <T> Mono<CosmosItemResponse<T>> parse(Mono<CosmosItemResponse<byte[]>> rspMono, Class<T> classType,
                                                  EncryptableItem<T> encryptableItem) {
        return rspMono.map(rsp -> {
            if (encryptableItem == null) {
                return this.responseFactory.createItemResponse(rsp, classType);
            } else {
                encryptableItem.setDecryptableItem(
                    // EncryptionProcessor.BaseSerializer.FromStream<JObject>(responseMessage.Content),
                    getByteArrayContent(rsp),
                    cipherText -> encryptor.decrypt(cipherText),
                    this.getItemDeserializer()
                );

                return createEncryptionItemResponse(rsp, (T) encryptableItem);
            }
        });
    }

    private Mono<CosmosItemResponse<byte[]>> replaceItemHelper(byte[] payloadBytes,
                                                               String id,
                                                               PartitionKey partitionKey,
                                                               CosmosItemRequestOptions requestOptions,
                                                               boolean decryptResponse) {
        Preconditions.checkNotNull(payloadBytes, "payloadBytes");
        Preconditions.checkNotNull(partitionKey, "partitionKey");

        Mono<byte[]> encryptedPayloadBytesMono = this.encryptor.encrypt(payloadBytes);
        return encryptedPayloadBytesMono.flatMap(
            encryptedByteArray ->
                this.container.replaceItem(
                    encryptedByteArray,
                    id,
                    partitionKey,
                    requestOptions)
        ).flatMap(
            rawCosmosItemResponse -> {
                if (decryptResponse) {
                    return setByteArrayContent(
                        rawCosmosItemResponse,
                        encryptor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rawCosmosItemResponse)));
                }

                return Mono.just(rawCosmosItemResponse);
            }
        );
    }


    private Mono<CosmosItemResponse<byte[]>> readItemHelper(String id,
                                                            PartitionKey partitionKey,
                                                            CosmosItemRequestOptions requestOptions,
                                                            boolean decryptResponse) {
        Mono<CosmosItemResponse<byte[]>> serviceByteArrayResponse = this.container.readItem(
            id,
            partitionKey,
            requestOptions,
            byte[].class);

        return serviceByteArrayResponse
            .flatMap(
                rsp -> {
                    if (decryptResponse) {
                        return setByteArrayContent(rsp,
                            this.encryptor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rsp)));
                    }

                    return Mono.just(rsp);
                }
            );
    }


    private Mono<CosmosItemResponse<byte[]>> upsertItemHelper(byte[] payloadBytes,
                                                              PartitionKey partitionKey,
                                                              CosmosItemRequestOptions requestOptions,
                                                              boolean decryptResponse) {
        Preconditions.checkNotNull(payloadBytes, "payloadBytes");
        Preconditions.checkNotNull(partitionKey, "partitionKey");

        Mono<byte[]> encryptedPayloadBytesMono = this.encryptor.encrypt(payloadBytes);
        return encryptedPayloadBytesMono.flatMap(
            encryptedByteArray ->
                this.container.upsertItem(
                    encryptedByteArray,
                    partitionKey,
                    requestOptions)
        ).flatMap(
            rawCosmosItemResponse -> {
                if (decryptResponse) {
                    return setByteArrayContent(
                        rawCosmosItemResponse,
                        encryptor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rawCosmosItemResponse)));
                }

                return Mono.just(rawCosmosItemResponse);
            }
        );
    }

    private Mono<CosmosItemResponse<byte[]>> createItemHelper(byte[] payloadBytes,
                                                              PartitionKey partitionKey,
                                                              CosmosItemRequestOptions requestOptions,
                                                              boolean decryptResponse) {
        Preconditions.checkNotNull(payloadBytes, "payloadBytes");
        Preconditions.checkNotNull(partitionKey, "partitionKey");

        Mono<byte[]> encryptedPayloadBytesMono = this.encryptor.encrypt(payloadBytes);
        return encryptedPayloadBytesMono.flatMap(
            encryptedByteArray ->
                this.container.createItem(
                    encryptedByteArray,
                    partitionKey,
                    requestOptions)
        ).flatMap(
            rawCosmosItemResponse -> {
                if (decryptResponse) {
                    return setByteArrayContent(
                        rawCosmosItemResponse,
                        encryptor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(rawCosmosItemResponse)));
                }

                return Mono.just(rawCosmosItemResponse);
            }
        );
    }


    //
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

        return encryptor.decrypt(input);
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
                                                                                                   Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
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
                                    new DecryptableItemCore(bytes,
                                        cipherText -> encryptor.decrypt(cipherText),
                                        this.getItemDeserializer());
                                itemList.add((T) decryptableItem);
                            }

                            return Mono.just(ModelBridgeInternal.createFeedResponseWithQueryMetrics(itemList,
                                page.getResponseHeaders(),
                                BridgeInternal.queryMetricsFromFeedResponse(page),
                                ModelBridgeInternal.getQueryPlanDiagnosticsContext(page)));
                        } else {
                            List<Mono<byte[]>> byteArrayMonoList =
                                byteArrayList.stream().map(bytes -> decryptResponse(bytes)).collect(Collectors
                                    .toList());
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
                    }
                )
        );
    }
}

