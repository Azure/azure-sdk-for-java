// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.encryption;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactory;
import com.azure.cosmos.implementation.encryption.CosmosResponseFactoryCore;
import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.encryption.EncryptionQueryRequestOption;
import com.azure.cosmos.implementation.encryption.EncryptionUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;
import java.util.function.Function;


// TODO: for now basic functionality is in. some APIs and some logic branch is not complete yet.
public class EncryptionCosmosAsyncContainer {
    private final Encryptor encryptor;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactoryCore();
    private final CosmosAsyncContainer container;

    EncryptionCosmosAsyncContainer(String id, CosmosAsyncDatabase database, Encryptor encryptor) {
        this.container = BridgeInternal.createCosmosAsyncContainer(id, database);
        this.encryptor = encryptor;
    }

    private Mono<CosmosItemResponse<byte[]>> createItemStream(byte[] payload,
                                                              PartitionKey partitionKey,
                                                              EncryptionItemRequestOptions encryptionItemRequestOptions) {
        Preconditions.checkNotNull(payload, "payload can't be null");

        // TODO: add diagnostics
        assert encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null;
        payload = EncryptionProcessor.encryptAsync(payload, encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        Mono<CosmosItemResponse<byte[]>> response = container.createItem(payload, partitionKey,
            encryptionItemRequestOptions);

        return response
            .publishOn(Schedulers.elastic())
            .map(rsp -> {
                    EncryptionModelBridgeInternal.setByteArrayContent(
                        rsp,
                        decryptResponseAsync(
                            EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                            encryptionItemRequestOptions.getDecryptionResultHandler()));
                    return rsp;
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
        payload = EncryptionProcessor.encryptAsync(payload, encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        Mono<CosmosItemResponse<byte[]>> response = container.replaceItem(payload,
            itemId,
            partitionKey,
            encryptionItemRequestOptions);

        return response
            .publishOn(Schedulers.elastic())
            .map(rsp -> {
                    EncryptionModelBridgeInternal.setByteArrayContent(
                        rsp,
                        decryptResponseAsync(
                            EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                            encryptionItemRequestOptions.getDecryptionResultHandler()));
                    return rsp;
                }
            );
    }

    private Mono<CosmosItemResponse<byte[]>> upsertItemStream(byte[] payload,
                                                              PartitionKey partitionKey,
                                                              EncryptionItemRequestOptions encryptionItemRequestOptions) {
        Preconditions.checkNotNull(payload, "payload can't be null");

        // TODO: add diagnostics
        assert encryptionItemRequestOptions != null && encryptionItemRequestOptions.getEncryptionOptions() != null;
        payload = EncryptionProcessor.encryptAsync(payload, encryptor,
            encryptionItemRequestOptions.getEncryptionOptions());

        Mono<CosmosItemResponse<byte[]>> response = container.upsertItem(payload, partitionKey,
            encryptionItemRequestOptions);

        return response
            .publishOn(Schedulers.elastic())
            .map(rsp -> {
                    EncryptionModelBridgeInternal.setByteArrayContent(
                        rsp,
                        decryptResponseAsync(
                            EncryptionModelBridgeInternal.getByteArrayContent(rsp),
                            encryptionItemRequestOptions.getDecryptionResultHandler()));
                    return rsp;
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

            byte[] payload = cosmosSerializerToStream(item);
            Mono<CosmosItemResponse<byte[]>> result = this.createItemStream(payload, partitionKey,
                encryptionItemRequestOptions);

            return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp,
                item.getClass()));

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

            byte[] payload = cosmosSerializerToStream(item);
            Mono<CosmosItemResponse<byte[]>> result = this.upsertItemStream(payload, partitionKey,
                encryptionItemRequestOptions);

            return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp,
                item.getClass()));

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

            byte[] payload = cosmosSerializerToStream(item);
            Mono<CosmosItemResponse<byte[]>> result = this.replaceItemStream(payload,
                itemId,
                partitionKey,
                encryptionItemRequestOptions);

            return result.map(rsp -> (CosmosItemResponse<T>) this.responseFactory.createItemResponse(rsp,
                item.getClass()));

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
            .publishOn(Schedulers.elastic())
            .map(
                responseMessage -> {
                    Consumer<DecryptionResult> decryptionErroHandler = null;
                    EncryptionItemRequestOptions encryptionItemRequestOptions = Utils.as(requestOptions,
                        EncryptionItemRequestOptions.class);

                    if (encryptionItemRequestOptions != null) {
                        decryptionErroHandler = encryptionItemRequestOptions.getDecryptionResultHandler();
                    }

                    EncryptionModelBridgeInternal.setByteArrayContent(responseMessage, this.decryptResponseAsync(
                        EncryptionModelBridgeInternal.getByteArrayContent(responseMessage), decryptionErroHandler));

                    return responseMessage;

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

        EncryptionQueryRequestOption encryptionQueryRequestOptions = Utils.as(options,
            EncryptionQueryRequestOption.class);

        Consumer<DecryptionResult> decryptionResultConsumer = null;
        if (encryptionQueryRequestOptions != null) {
            decryptionResultConsumer = encryptionQueryRequestOptions.getDecryptionResultHandler();
        }

        return CosmosBridgeInternal.queryItemsInternal(container, query, options, classType,
            createTransformer(decryptionResultConsumer), Schedulers.elastic());
    }

    private Function<Document, Document> createTransformer(Consumer<DecryptionResult> decryptionResultConsumer) {

        return document -> {
            try {
                byte[] contentAsByteArray = EncryptionUtils.toByteArray(document.serializeJsonToByteBuffer());
                byte[] result = decryptResponseAsync(contentAsByteArray, decryptionResultConsumer);
                return new Document(result);
            } catch (Exception e) {
                if (decryptionResultConsumer != null) {
                    decryptionResultConsumer.accept(DecryptionResult.createFailure(null, e));
                } else {
                    throw e;
                }
            }

            return document;
        };
    }

    private <T> byte[] cosmosSerializerToStream(T item) {
        // TODO:
        return EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), item);
    }

    ItemDeserializer getItemDeserializer() {
        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase()).getItemDeserializer();
    }

    private byte[] decryptResponseAsync(
        byte[] input,
        Consumer<DecryptionResult> decryptionResultHandler) {

        if (input == null) {
            return null;
        }

        try {
            return EncryptionProcessor.decryptAsync(
                input,
                this.encryptor);
        } catch (Exception exception) {
            if (decryptionResultHandler == null) {
                throw exception;
            }

            decryptionResultHandler.accept(DecryptionResult.createFailure(
                input,
                exception));

            decryptionResultHandler.accept(
                DecryptionResult.createFailure(
                    input,
                    exception));

            return input;
        }
    }
}

