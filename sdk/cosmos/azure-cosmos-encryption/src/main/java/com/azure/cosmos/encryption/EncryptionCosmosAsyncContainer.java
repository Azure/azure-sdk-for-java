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
import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.encryption.EncryptionUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionModelBridgeInternal;
import com.azure.cosmos.models.EncryptionSqlQuerySpec;
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
import java.util.function.Function;
import java.util.stream.Collectors;


// TODO: for now basic functionality is in. some APIs and some logic branch is not complete yet.
// TODO: should we test the apis for byte-array (streaming api replacement)?
public class EncryptionCosmosAsyncContainer {
    private final Scheduler encryptionScheduler;
    private final Encryptor encryptor;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactoryCore();
    private final CosmosAsyncContainer container;
    private EncryptionProcessor encryptionProcessor;
    private EncryptionCosmosAsyncClient encryptionCosmosAsyncClient;


    EncryptionCosmosAsyncContainer(CosmosAsyncContainer container,
                                   EncryptionCosmosAsyncClient encryptionCosmosAsyncClient) {
        this.container = container;
        this.encryptionCosmosAsyncClient = encryptionCosmosAsyncClient;
        this.encryptionProcessor = new EncryptionProcessor(this.container, encryptionCosmosAsyncClient);
        this.encryptionScheduler = Schedulers.parallel();
        encryptor = null;
    }

    public EncryptionProcessor getEncryptionProcessor() {
        return this.encryptionProcessor;
    }

    /**
     * create item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(T item,
                                                      PartitionKey partitionKey,
                                                      CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
            + "EncryptionContainer.");


        byte[] streamPayload = cosmosSerializerToStream(item);
        CosmosItemRequestOptions finalRequestOptions = requestOptions;
        return this.encryptionProcessor.encrypt(streamPayload).flatMap(encryptedPayload -> this.container.createItem(
            encryptedPayload,
            partitionKey,
            finalRequestOptions)
            .publishOn(encryptionScheduler).flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                this.encryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(cosmosItemResponse)))
                .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, (Class<T>) item.getClass()))));
    }


    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon successful completion will contain a
     * single Cosmos item response with the deleted item.
     *
     * @param itemId       id of the item.
     * @param partitionKey partitionKey of the item.
     * @param options      the request options.
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
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item,
                                                      PartitionKey partitionKey,
                                                      CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
            + "EncryptionContainer.");


        byte[] streamPayload = cosmosSerializerToStream(item);
        CosmosItemRequestOptions finalRequestOptions = requestOptions;
        return this.encryptionProcessor.encrypt(streamPayload).flatMap(encryptedPayload -> this.container.upsertItem(
            encryptedPayload,
            partitionKey,
            finalRequestOptions)
            .publishOn(encryptionScheduler).flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                this.encryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(cosmosItemResponse)))
                .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, (Class<T>) item.getClass()))));
    }

    /**
     * replaces item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param itemId         the item id.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
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

        Preconditions.checkArgument(partitionKey != null, "partitionKey cannot be null for operations using "
            + "EncryptionContainer.");


        byte[] streamPayload = cosmosSerializerToStream(item);
        CosmosItemRequestOptions finalRequestOptions = requestOptions;
        return this.encryptionProcessor.encrypt(streamPayload).flatMap(encryptedPayload -> this.container.replaceItem(
            encryptedPayload,
            itemId,
            partitionKey,
            finalRequestOptions)
            .publishOn(encryptionScheduler).flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                this.encryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(cosmosItemResponse)))
                .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, (Class<T>) item.getClass()))));
    }

    /**
     * Reads item and decrypt the encrypted fields
     *
     * @param id             item id
     * @param partitionKey   the partition key.
     * @param requestOptions request options
     * @param classType      deserialization class type
     * @param <T>            type
     * @return result
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String id,
                                                    PartitionKey partitionKey,
                                                    CosmosItemRequestOptions requestOptions,
                                                    Class<T> classType) {


        Mono<CosmosItemResponse<byte[]>> responseMessageMono = this.container.readItem(
            id,
            partitionKey,
            requestOptions, byte[].class);

        return responseMessageMono.publishOn(encryptionScheduler).flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
            this.encryptionProcessor.decrypt(EncryptionModelBridgeInternal.getByteArrayContent(cosmosItemResponse)))
            .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, classType)));
    }

    /**
     * Query for items in the current container using a string.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will contain one or several feed
     * response of the obtained items. In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T>       the type parameter.
     * @param query     the query text.
     * @param options   the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, CosmosQueryRequestOptions options,
                                             Class<T> classType) {

        return this.queryItems(new SqlQuerySpec(query), new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Query for items in the current container using a string.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will contain one or several feed
     * response of the obtained items. In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T>       the type parameter.
     * @param query     the query.
     * @param options   the query request options.
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
                    return queryDecryptionTransformer(classType, func);
                }
            });
    }

    /**
     * Query for items in the current container using a string.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will contain one or several feed
     * response of the obtained items. In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T>       the type parameter.
     * @param query     the query.
     * @param options   the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItemsOnEncryptedProperties(EncryptionSqlQuerySpec query, CosmosQueryRequestOptions options,
                                             Class<T> classType) {

        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return CosmosBridgeInternal.queryItemsInternal(container, query.getSqlQuerySpec(), options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, func);
                }
            });
    }

    public CosmosAsyncContainer getCosmosAsyncContainer() {
        return container;
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

        return this.encryptionProcessor.decrypt(
            input);
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

                        List<Mono<byte[]>> byteArrayMonoList =
                            byteArrayList.stream().map(bytes -> decryptResponse(bytes)).collect(Collectors.toList());
                        return Flux.concat(byteArrayMonoList).map(
                            item -> getItemDeserializer().parseFrom(classType, item)
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
