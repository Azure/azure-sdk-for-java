// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.implementation.Constants;
import com.azure.cosmos.encryption.implementation.CosmosResponseFactory;
import com.azure.cosmos.encryption.implementation.EncryptionProcessor;
import com.azure.cosmos.encryption.implementation.EncryptionUtils;
import com.azure.cosmos.encryption.models.EncryptionModelBridgeInternal;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.encryption.util.Beta;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.Utils.getEffectiveCosmosChangeFeedRequestOptions;
import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * CosmosAsyncContainer with encryption capabilities.
 */
public class CosmosEncryptionAsyncContainer {
    private final Scheduler encryptionScheduler;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactory();
    private final CosmosAsyncContainer container;
    private final EncryptionProcessor encryptionProcessor;

    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    ImplementationBridgeHelpers.CosmosItemResponseHelper.CosmosItemResponseBuilderAccessor cosmosItemResponseBuilderAccessor;
    ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.CosmosItemRequestOptionsAccessor cosmosItemRequestOptionsAccessor;
    ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor cosmosQueryRequestOptionsAccessor;
    ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.CosmosChangeFeedRequestOptionsAccessor cosmosChangeFeedRequestOptionsAccessor;
    ImplementationBridgeHelpers.CosmosAsyncContainerHelper.CosmosAsyncContainerAccessor cosmosAsyncContainerAccessor;
    ImplementationBridgeHelpers.CosmosBatchHelper.CosmosBatchAccessor cosmosBatchAccessor;
    ImplementationBridgeHelpers.CosmosBatchResponseHelper.CosmosBatchResponseAccessor cosmosBatchResponseAccessor;
    ImplementationBridgeHelpers.CosmosBatchOperationResultHelper.CosmosBatchOperationResultAccessor cosmosBatchOperationResultAccessor;
    ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.CosmosBatchRequestOptionsAccessor cosmosBatchRequestOptionsAccessor;

    CosmosEncryptionAsyncContainer(CosmosAsyncContainer container,
                                   CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        this.container = container;
        this.cosmosEncryptionAsyncClient = cosmosEncryptionAsyncClient;
        this.encryptionProcessor = new EncryptionProcessor(this.container, cosmosEncryptionAsyncClient);
        this.encryptionScheduler = Schedulers.parallel();
        this.cosmosItemResponseBuilderAccessor =
            ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor();
        this.cosmosItemRequestOptionsAccessor =
            ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.getCosmosItemRequestOptionsAccessor();
        this.cosmosQueryRequestOptionsAccessor =
            ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();
        this.cosmosChangeFeedRequestOptionsAccessor =
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor();
        this.cosmosAsyncContainerAccessor =
            ImplementationBridgeHelpers.CosmosAsyncContainerHelper.getCosmosAsyncContainerAccessor();
        this.cosmosBatchAccessor = ImplementationBridgeHelpers.CosmosBatchHelper.getCosmosBatchAccessor();
        this.cosmosBatchResponseAccessor = ImplementationBridgeHelpers.CosmosBatchResponseHelper.getCosmosBatchResponseAccessor();
        this.cosmosBatchOperationResultAccessor = ImplementationBridgeHelpers.CosmosBatchOperationResultHelper.getCosmosBatchOperationResultAccessor();
        this.cosmosBatchRequestOptionsAccessor = ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.getCosmosBatchRequestOptionsAccessor();
    }

    EncryptionProcessor getEncryptionProcessor() {
        return this.encryptionProcessor;
    }

    /**
     * create item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return a {@link Mono} containing the Cosmos item resource response.
     */
    @SuppressWarnings("unchecked")
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
        return  createItemHelper(streamPayload, partitionKey, requestOptions, (Class<T>) item.getClass(), false);
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
     * @return a {@link Mono} containing the Cosmos item resource response.
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
     * @return a {@link Mono} containing the Cosmos item resource response.
     */
    @SuppressWarnings("unchecked")
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
        return upsertItemHelper(streamPayload, partitionKey, requestOptions, (Class<T>) item.getClass(), false);
    }

    /**
     * replaces item and encrypts the requested fields
     *
     * @param item           the Cosmos item represented as a POJO or Cosmos item object.
     * @param itemId         the item id.
     * @param partitionKey   the partition key.
     * @param requestOptions request option
     * @param <T>            serialization class type
     * @return a {@link Mono} containing the Cosmos item resource response.
     */
    @SuppressWarnings("unchecked")
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
        return replaceItemHelper(streamPayload, itemId, partitionKey, requestOptions, (Class<T>) item.getClass(), false);
    }

    /**
     * Reads item and decrypt the encrypted fields
     *
     * @param id             item id
     * @param partitionKey   the partition key.
     * @param requestOptions request options
     * @param classType      deserialization class type
     * @param <T>            type
     * @return a {@link Mono} containing the Cosmos item resource response.
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String id,
                                                    PartitionKey partitionKey,
                                                    CosmosItemRequestOptions requestOptions,
                                                    Class<T> classType) {
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        Mono<CosmosItemResponse<byte[]>> responseMessageMono = this.readItemHelper(id, partitionKey, requestOptions, false);

        return responseMessageMono.publishOn(encryptionScheduler).flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
            this.encryptionProcessor.decrypt(this.cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
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
     * Query for items in the current container using a {@link SqlQuerySpec}.
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

        return queryItemsHelper(query, options, classType,false);
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpecWithEncryption}.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will contain one or several feed
     * response of the obtained items. In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T>                        the type parameter.
     * @param sqlQuerySpecWithEncryption the sqlQuerySpecWithEncryption.
     * @param options                    the query request options.
     * @param classType                  the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItemsOnEncryptedProperties(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption,
                                                                  CosmosQueryRequestOptions options,
                                                                  Class<T> classType) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        if (EncryptionModelBridgeInternal.getEncryptionParamMap(sqlQuerySpecWithEncryption).size() > 0) {
            List<Mono<Void>> encryptionSqlParameterMonoList = new ArrayList<>();
            for (Map.Entry<String, SqlParameter> entry :
                EncryptionModelBridgeInternal.getEncryptionParamMap(sqlQuerySpecWithEncryption).entrySet()) {
                encryptionSqlParameterMonoList.add(EncryptionModelBridgeInternal.addEncryptionParameterAsync(sqlQuerySpecWithEncryption, entry.getKey(), entry.getValue(), this));
            }
            Mono<List<Void>> listMono = Flux.mergeSequential(encryptionSqlParameterMonoList).collectList();
            Mono<SqlQuerySpec> sqlQuerySpecMono =
                listMono.flatMap(ignoreVoids -> Mono.just(EncryptionModelBridgeInternal.getSqlQuerySpec(sqlQuerySpecWithEncryption)));
            return queryItemsHelperWithMonoSqlQuerySpec(sqlQuerySpecMono, sqlQuerySpecWithEncryption, options, classType, false);
        } else {
            return queryItemsHelper(EncryptionModelBridgeInternal.getSqlQuerySpec(sqlQuerySpecWithEncryption),
                options, classType, false);
        }
    }

    @Beta(value = Beta.SinceVersion.V1, warningText =
        Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public <T> CosmosPagedFlux<T> queryChangeFeed(CosmosChangeFeedRequestOptions options, Class<T> classType) {
        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        return queryChangeFeedHelper(options, classType,false);
    }

    /**
     * Get the CosmosEncryptionAsyncClient
     *
     * @return encrypted cosmosAsyncClient
     */
    CosmosEncryptionAsyncClient getCosmosEncryptionAsyncClient() {
        return cosmosEncryptionAsyncClient;
    }

    /**
     * Gets the CosmosAsyncContainer
     *
     * @return cosmos container
     */
    public CosmosAsyncContainer getCosmosAsyncContainer() {
        return container;
    }

    <T> byte[] cosmosSerializerToStream(T item) {
        return EncryptionUtils.serializeJsonToByteArray(EncryptionUtils.getSimpleObjectMapper(), item);
    }

    ItemDeserializer getItemDeserializer() {
        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase()).getItemDeserializer();
    }

    Mono<JsonNode> decryptResponseNode(
        JsonNode jsonNode) {

        if (jsonNode == null) {
            return Mono.empty();
        }

        return this.encryptionProcessor.decryptJsonNode(
            jsonNode);
    }

    private Mono<CosmosItemResponse<byte[]>> setByteArrayContent(CosmosItemResponse<byte[]> rsp,
                                                                 Mono<byte[]> bytesMono) {
        return bytesMono.flatMap(
            bytes -> {
                this.cosmosItemResponseBuilderAccessor.setByteArrayContent(rsp, bytes);
                return Mono.just(rsp);
            }
        ).defaultIfEmpty(rsp);
    }

    private <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryDecryptionTransformer(Class<T> classType,
                                                                                                   boolean isChangeFeed,
                                                                                                   Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
        return func.andThen(flux ->
            flux.publishOn(encryptionScheduler)
                .flatMap(
                    page -> {
                        boolean useEtagAsContinuation = isChangeFeed;
                        boolean isNoChangesResponse = isChangeFeed ?
                            ModelBridgeInternal.getNoCHangesFromFeedResponse(page)
                            : false;
                        List<Mono<JsonNode>> jsonNodeArrayMonoList =
                            page.getResults().stream().map(jsonNode -> decryptResponseNode(jsonNode)).collect(Collectors.toList());
                        return Flux.concat(jsonNodeArrayMonoList).map(
                            item -> {
                                if (item.isValueNode()) {
                                    return (T)item;
                                } else {
//                                    return getItemDeserializer().parseFrom(classType, EncryptionUtils.serializeJsonToByteArray(EncryptionUtils.getSimpleObjectMapper(), item));
                                    return getItemDeserializer().convert(classType, (ObjectNode) item);
                                }
                            }
                        ).collectList().map(itemList -> BridgeInternal.createFeedResponseWithQueryMetrics(itemList,
                            page.getResponseHeaders(),
                            BridgeInternal.queryMetricsFromFeedResponse(page),
                            ModelBridgeInternal.getQueryPlanDiagnosticsContext(page),
                            useEtagAsContinuation,
                            isNoChangesResponse,
                            page.getCosmosDiagnostics())
                        );
                    }
                )
        );
    }

    private Mono<CosmosItemResponse<byte[]>> readItemHelper(String id,
                                                            PartitionKey partitionKey,
                                                            CosmosItemRequestOptions requestOptions,
                                                            boolean isRetry) {
        this.setRequestHeaders(requestOptions);
        Mono<CosmosItemResponse<byte[]>> responseMessageMono = this.container.readItem(
            id,
            partitionKey,
            requestOptions, byte[].class);
        return responseMessageMono.onErrorResume(exception -> {
            if (!isRetry && exception instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) exception;
                if (isIncorrectContainerRid(cosmosException)) {
                    this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                    return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then(Mono.defer(() -> readItemHelper(id, partitionKey, requestOptions, true)
                    ));
                }
            }
            return Mono.error(exception);
        });
    }

    private <T> Mono<CosmosItemResponse<T>> createItemHelper(byte[] streamPayload,
                                                             PartitionKey partitionKey,
                                                             CosmosItemRequestOptions requestOptions,
                                                             Class<T> itemClass,
                                                             boolean isRetry) {
        this.setRequestHeaders(requestOptions);
        return this.encryptionProcessor.encrypt(streamPayload)
            .flatMap(encryptedPayload -> this.container.createItem(
                encryptedPayload,
                partitionKey,
                requestOptions)
                .publishOn(encryptionScheduler)
                .flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                    this.encryptionProcessor.decrypt(this.cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
                    .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse,
                        itemClass))).onErrorResume(exception -> {
                    if (!isRetry && exception instanceof CosmosException) {
                        final CosmosException cosmosException = (CosmosException) exception;
                        if (isIncorrectContainerRid(cosmosException)) {
                            this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                            return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then
                                (Mono.defer(() -> createItemHelper(streamPayload, partitionKey, requestOptions,
                                    itemClass, true)));
                        }
                    }
                    return Mono.error(exception);
                }));
    }

    private <T> Mono<CosmosItemResponse<T>> upsertItemHelper(byte[] streamPayload,
                                                             PartitionKey partitionKey,
                                                             CosmosItemRequestOptions requestOptions,
                                                             Class<T> itemClass,
                                                             boolean isRetry) {
        this.setRequestHeaders(requestOptions);
        return this.encryptionProcessor.encrypt(streamPayload)
            .flatMap(encryptedPayload -> this.container.upsertItem(
                encryptedPayload,
                partitionKey,
                requestOptions)
                .publishOn(encryptionScheduler)
                .flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                    this.encryptionProcessor.decrypt(this.cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
                    .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, itemClass)))
                .onErrorResume(exception -> {
                    if (!isRetry && exception instanceof CosmosException) {
                        final CosmosException cosmosException = (CosmosException) exception;
                        if (isIncorrectContainerRid(cosmosException)) {
                            this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                            return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then
                                (Mono.defer(() -> upsertItemHelper(streamPayload, partitionKey, requestOptions,
                                    itemClass, true)));
                        }
                    }
                    return Mono.error(exception);
                }));
    }

    private <T> Mono<CosmosItemResponse<T>> replaceItemHelper(byte[] streamPayload,
                                                              String itemId,
                                                             PartitionKey partitionKey,
                                                             CosmosItemRequestOptions requestOptions,
                                                             Class<T> itemClass,
                                                             boolean isRetry) {
        this.setRequestHeaders(requestOptions);
        return this.encryptionProcessor.encrypt(streamPayload)
            .flatMap(encryptedPayload -> this.container.replaceItem(
                encryptedPayload,
                itemId,
                partitionKey,
                requestOptions)
                .publishOn(encryptionScheduler)
                .flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                    this.encryptionProcessor.decrypt(this.cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
                    .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, itemClass)))
                .onErrorResume(exception -> {
                    if (!isRetry && exception instanceof CosmosException) {
                        final CosmosException cosmosException = (CosmosException) exception;
                        if (isIncorrectContainerRid(cosmosException)) {
                            this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                            return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then
                                (Mono.defer(() -> replaceItemHelper(streamPayload, itemId, partitionKey, requestOptions,
                                    itemClass, true)));
                        }
                    }
                    return Mono.error(exception);
                }));
    }

    private <T> CosmosPagedFlux<T> queryItemsHelper(SqlQuerySpec sqlQuerySpec,
                                                    CosmosQueryRequestOptions options,
                                                    Class<T> classType,
                                                    boolean isRetry) {
        setRequestHeaders(options);
        CosmosQueryRequestOptions finalOptions = options;
        Flux<FeedResponse<T>>  tFlux = CosmosBridgeInternal.queryItemsInternal(container, sqlQuerySpec, options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, false, func);
                }
            }).byPage().onErrorResume(exception -> {
            if (exception instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) exception;
                if (!isRetry && isIncorrectContainerRid(cosmosException)) {
                    this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                    return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).thenMany(
                        (CosmosPagedFlux.defer(() -> queryItemsHelper(sqlQuerySpec,finalOptions, classType, true).byPage())));
                }
            }
            return Mono.error(exception);
        });


        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, finalOptions);
            return tFlux;
        });
    }

    private <T> CosmosPagedFlux<T> queryChangeFeedHelper(CosmosChangeFeedRequestOptions options,
                                                         Class<T> classType,
                                                         boolean isRetry) {
        setRequestHeaders(options);
        CosmosChangeFeedRequestOptions finalOptions = options;
        Flux<FeedResponse<T>> tFlux =
            UtilBridgeInternal.createCosmosPagedFlux(((Transformer<T>) func -> queryDecryptionTransformer(classType,
                true,
                func)).transform(cosmosAsyncContainerAccessor.queryChangeFeedInternalFunc(this.container, options,
                JsonNode.class))).byPage().onErrorResume(exception -> {
                if (exception instanceof CosmosException) {
                    final CosmosException cosmosException = (CosmosException) exception;
                    if (!isRetry && isIncorrectContainerRid(cosmosException)) {
                        this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                        return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).thenMany(
                            (CosmosPagedFlux.defer(() -> queryChangeFeedHelper(finalOptions, classType, true).byPage())));
                    }
                }
                return Mono.error(exception);
            });


        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            getEffectiveCosmosChangeFeedRequestOptions(pagedFluxOptions, finalOptions);
            return tFlux;
        });
    }


    private <T> CosmosPagedFlux<T> queryItemsHelperWithMonoSqlQuerySpec(Mono<SqlQuerySpec> sqlQuerySpecMono,
                                                                        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption,
                                                                        CosmosQueryRequestOptions options,
                                                                        Class<T> classType,
                                                                        boolean isRetry) {

        setRequestHeaders(options);
        CosmosQueryRequestOptions finalOptions = options;

        Flux<FeedResponse<T>>  tFlux = CosmosBridgeInternal.queryItemsInternal(container, sqlQuerySpecMono, options,
            new Transformer<T>() {
                @Override
                public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
                    return queryDecryptionTransformer(classType, false, func);
                }
            }).byPage().onErrorResume(exception -> {
            if (exception instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) exception;
                if (!isRetry && isIncorrectContainerRid(cosmosException)) {
                    this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                    return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).thenMany(
                        (CosmosPagedFlux.defer(() -> queryItemsHelper(EncryptionModelBridgeInternal.getSqlQuerySpec(sqlQuerySpecWithEncryption), finalOptions, classType, true).byPage())));
                }
            }
            return Mono.error(exception);
        });

        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, finalOptions);
            return tFlux;
        });
    }

    /**
     * Executes the encrypted transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     *
     * @return A Mono response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then the Mono will return error instead of CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    @Beta(value = Beta.SinceVersion.V1, warningText =
        Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Mono<CosmosBatchResponse> executeCosmosBatch(CosmosBatch cosmosBatch) {
        return this.executeCosmosBatch(cosmosBatch, new CosmosBatchRequestOptions());
    }

    /**
     * Executes the encrypted transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     * @param requestOptions Options that apply specifically to batch request.
     *
     * @return A Mono response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then the Mono will return error instead of CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    @Beta(value = Beta.SinceVersion.V1, warningText =
        Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Mono<CosmosBatchResponse> executeCosmosBatch(CosmosBatch cosmosBatch, CosmosBatchRequestOptions requestOptions) {
        if (requestOptions == null) {
            requestOptions = new CosmosBatchRequestOptions();
        }

        List<Mono<ItemBatchOperation<?>>> monoList = new ArrayList<>();
        for (ItemBatchOperation<?> itemBatchOperation : this.cosmosBatchAccessor.getOperationsInternal(cosmosBatch)) {
            Mono<ItemBatchOperation<?>> itemBatchOperationMono = null;
            if (itemBatchOperation.getItem() != null) {
                ObjectNode objectNode =
                    EncryptionUtils.getSimpleObjectMapper().valueToTree(itemBatchOperation.getItem());
                itemBatchOperationMono =
                    encryptionProcessor.encryptObjectNode(objectNode).map(encryptedItem -> {
                        return new ItemBatchOperation<>(
                            itemBatchOperation.getOperationType(),
                            itemBatchOperation.getId(),
                            itemBatchOperation.getPartitionKeyValue(),
                            itemBatchOperation.getRequestOptions(),
                            encryptedItem
                        );
                    });
            } else {
                itemBatchOperationMono =
                    Mono.just(
                        new ItemBatchOperation<>(
                            itemBatchOperation.getOperationType(),
                            itemBatchOperation.getId(),
                            itemBatchOperation.getPartitionKeyValue(),
                            itemBatchOperation.getRequestOptions(),
                            null
                        )
                    );
            }
            monoList.add(itemBatchOperationMono);
        }
        Mono<List<ItemBatchOperation<?>>> encryptedOperationListMono =
            Flux.mergeSequential(monoList).collectList();
        CosmosBatchRequestOptions finalRequestOptions = requestOptions;

        CosmosBatch encryptedCosmosBatch = CosmosBatch.createCosmosBatch(cosmosBatch.getPartitionKeyValue());

        return encryptedOperationListMono.flatMap(itemBatchOperations -> {
            this.cosmosBatchAccessor.getOperationsInternal(encryptedCosmosBatch).addAll(itemBatchOperations);
            return executeCosmosBatchHelper(encryptedCosmosBatch, finalRequestOptions, false);
        });
    }

    private Mono<CosmosBatchResponse> executeCosmosBatchHelper(CosmosBatch encryptedCosmosBatch,
                                                               CosmosBatchRequestOptions requestOptions,
                                                               boolean isRetry) {
        setRequestHeaders(requestOptions);
        return this.container.executeCosmosBatch(encryptedCosmosBatch, requestOptions).flatMap(cosmosBatchResponse -> {
            // TODO this should check for BadRequest StatusCode too, requires a service fix to return 400 instead of
            //  -1 which is currently returned inside the body.
            //  Once fixed from service below if condition can be removed, as this is already covered in onErrorResume.
            if (!isRetry && cosmosBatchResponse.getSubStatusCode() == 1024) {
                this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then
                    (Mono.defer(() -> executeCosmosBatchHelper(encryptedCosmosBatch, requestOptions, true)));
            }

            List<Mono<Void>> decryptMonoList = new ArrayList<>();
            for (CosmosBatchOperationResult cosmosBatchOperationResult :
                this.cosmosBatchResponseAccessor.getResults(cosmosBatchResponse)) {
                ObjectNode objectNode =
                    this.cosmosBatchOperationResultAccessor.getResourceObject(cosmosBatchOperationResult);
                if (objectNode != null) {
                    decryptMonoList.add(encryptionProcessor.decryptJsonNode(objectNode).flatMap(jsonNode -> {
                        this.cosmosBatchOperationResultAccessor.setResourceObject(cosmosBatchOperationResult, (ObjectNode) jsonNode);
                        return Mono.empty();
                    }));
                }
            }

            Mono<List<Void>> listMono = Flux.mergeSequential(decryptMonoList).collectList();
            return listMono.map(aVoid -> cosmosBatchResponse);
        }).onErrorResume(exception -> {
            if (!isRetry && exception instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) exception;
                if (isIncorrectContainerRid(cosmosException)) {
                    this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                    return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then
                        (Mono.defer(() -> executeCosmosBatchHelper(encryptedCosmosBatch, requestOptions, true)));
                }
            }
            return Mono.error(exception);
        });
    }

    private void setRequestHeaders(CosmosItemRequestOptions requestOptions) {
        this.cosmosItemRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        this.cosmosItemRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosQueryRequestOptions requestOptions) {
        this.cosmosQueryRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        this.cosmosQueryRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosChangeFeedRequestOptions requestOptions) {
        this.cosmosChangeFeedRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        this.cosmosChangeFeedRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosBatchRequestOptions requestOptions) {
        this.cosmosBatchRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        this.cosmosBatchRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    boolean isIncorrectContainerRid(CosmosException cosmosException) {
        return cosmosException.getStatusCode() == HttpConstants.StatusCodes.BADREQUEST &&
            cosmosException.getResponseHeaders().get(HttpConstants.HttpHeaders.SUB_STATUS)
                .equals(Constants.INCORRECT_CONTAINER_RID_SUB_STATUS);
    }
}
