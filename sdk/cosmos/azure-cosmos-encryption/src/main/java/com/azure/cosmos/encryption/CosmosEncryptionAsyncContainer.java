// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.implementation.Constants;
import com.azure.cosmos.encryption.implementation.CosmosResponseFactory;
import com.azure.cosmos.encryption.implementation.EncryptionImplementationBridgeHelpers;
import com.azure.cosmos.encryption.implementation.EncryptionUtils;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.batch.ItemBulkOperation;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.patch.PatchOperation;
import com.azure.cosmos.implementation.patch.PatchOperationCore;
import com.azure.cosmos.implementation.patch.PatchOperationType;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import com.azure.cosmos.encryption.implementation.EncryptionProcessor;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
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
public final class CosmosEncryptionAsyncContainer {
    private final Scheduler encryptionScheduler;
    private final CosmosResponseFactory responseFactory = new CosmosResponseFactory();
    private final CosmosAsyncContainer container;
    private final EncryptionProcessor encryptionProcessor;

    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private final static ImplementationBridgeHelpers.CosmosItemResponseHelper.CosmosItemResponseBuilderAccessor cosmosItemResponseBuilderAccessor = ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor();
    private final static ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.CosmosItemRequestOptionsAccessor cosmosItemRequestOptionsAccessor = ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.getCosmosItemRequestOptionsAccessor();
    private final static ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor cosmosQueryRequestOptionsAccessor = ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();
    private final static ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.CosmosChangeFeedRequestOptionsAccessor cosmosChangeFeedRequestOptionsAccessor = ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor();
    private final static ImplementationBridgeHelpers.CosmosAsyncContainerHelper.CosmosAsyncContainerAccessor cosmosAsyncContainerAccessor = ImplementationBridgeHelpers.CosmosAsyncContainerHelper.getCosmosAsyncContainerAccessor();
    private final static ImplementationBridgeHelpers.CosmosBatchHelper.CosmosBatchAccessor cosmosBatchAccessor = ImplementationBridgeHelpers.CosmosBatchHelper.getCosmosBatchAccessor();
    private final static ImplementationBridgeHelpers.CosmosBatchResponseHelper.CosmosBatchResponseAccessor cosmosBatchResponseAccessor = ImplementationBridgeHelpers.CosmosBatchResponseHelper.getCosmosBatchResponseAccessor();
    private final static ImplementationBridgeHelpers.CosmosBatchOperationResultHelper.CosmosBatchOperationResultAccessor cosmosBatchOperationResultAccessor = ImplementationBridgeHelpers.CosmosBatchOperationResultHelper.getCosmosBatchOperationResultAccessor();
    private final static ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.CosmosBatchRequestOptionsAccessor cosmosBatchRequestOptionsAccessor = ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.getCosmosBatchRequestOptionsAccessor();
    private final static ImplementationBridgeHelpers.CosmosPatchOperationsHelper.CosmosPatchOperationsAccessor cosmosPatchOperationsAccessor = ImplementationBridgeHelpers.CosmosPatchOperationsHelper.getCosmosPatchOperationsAccessor();
    private final static ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.CosmosBulkExecutionOptionsAccessor cosmosBulkExecutionOptionsAccessor = ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.getCosmosBulkExecutionOptionsAccessor();
    private final static ImplementationBridgeHelpers.CosmosBulkItemResponseHelper.CosmosBulkItemResponseAccessor cosmosBulkItemResponseAccessor = ImplementationBridgeHelpers.CosmosBulkItemResponseHelper.getCosmosBulkItemResponseAccessor();
    private final static EncryptionImplementationBridgeHelpers.SqlQuerySpecWithEncryptionHelper.SqlQuerySpecWithEncryptionAccessor specWithEncryptionAccessor = EncryptionImplementationBridgeHelpers.SqlQuerySpecWithEncryptionHelper.getSqlQuerySpecWithEncryptionAccessor();

    CosmosEncryptionAsyncContainer(CosmosAsyncContainer container,
                                   CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        this.container = container;
        this.cosmosEncryptionAsyncClient = cosmosEncryptionAsyncClient;
        this.encryptionProcessor = new EncryptionProcessor(this.container, cosmosEncryptionAsyncClient);
        this.encryptionScheduler = Schedulers.parallel();
    }

    EncryptionProcessor getEncryptionProcessor() {
        return this.encryptionProcessor;
    }

    /**
     * Creates an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created Cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @return an {@link Mono} containing the single resource response with the
     * created Cosmos item or an error.
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<CosmosItemResponse<T>> createItem(T item) {
        return createItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Creates a Cosmos item.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param requestOptions the item request options.
     * @return an {@link Mono} containing the single resource response with the created Cosmos item or an error.
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<CosmosItemResponse<T>> createItem(T item,
                                                      CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }
        byte[] streamPayload = cosmosSerializerToStream(item);
        return createItemHelper(streamPayload, null, requestOptions,(Class<T>) item.getClass(), false );

    }

    /**
     * Creates an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created Cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey the partition key.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the single resource response with the created Cosmos item or an error.
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
     * Deletes an item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     *
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteItem(String itemId, PartitionKey partitionKey) {
        return deleteItem(itemId, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     *
     * @param itemId id of the item.
     * @param partitionKey partitionKey of the item.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteItem(String itemId,
                                                       PartitionKey partitionKey,
                                                       CosmosItemRequestOptions requestOptions) {

        return container.deleteItem(itemId, partitionKey, requestOptions);
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     *
     * @param <T> the type parameter.
     * @param item item to be deleted.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public <T> Mono<CosmosItemResponse<Object>> deleteItem(T item, CosmosItemRequestOptions requestOptions) {
        return container.deleteItem(item, requestOptions);
    }

    /**
     * Deletes all items in the Container with the specified partitionKey value.
     * Starts an asynchronous Cosmos DB background operation which deletes all items in the Container with the specified value.
     * The asynchronous Cosmos DB background operation runs using a percentage of user RUs.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for all the deleted items.
     *
     * @param partitionKey partitionKey of the item.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    // TODO Make this api public once it is GA in cosmos core library
    Mono<CosmosItemResponse<Object>> deleteAllItemsByPartitionKey(PartitionKey partitionKey, CosmosItemRequestOptions requestOptions) {
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }
        return container.deleteAllItemsByPartitionKey(partitionKey, requestOptions);
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the item represented as a POJO or Item object to upsert.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item) {
        return upsertItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the item represented as a POJO or Item object to upsert.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item, CosmosItemRequestOptions requestOptions) {
        Preconditions.checkNotNull(item, "item");
        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        byte[] streamPayload = cosmosSerializerToStream(item);
        return upsertItemHelper(streamPayload, null, requestOptions, (Class<T>) item.getClass(), false);
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the item represented as a POJO or Item object to upsert.
     * @param partitionKey the partition key.
     * @param requestOptions the request options.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
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
     * Replaces an item with the passed in item  and encrypts the requested fields.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the replaced item.
     *
     * @param <T> the type parameter.
     * @param item the item to replace (containing the item id).
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @return an {@link Mono} containing the Cosmos item resource response with the replaced item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> replaceItem(T item, String itemId, PartitionKey partitionKey) {
        return replaceItem(item, itemId, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Replaces an item with the passed in item  and encrypts the requested fields.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the replaced item.
     *
     * @param <T> the type parameter.
     * @param item the item to replace (containing the item id).
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param requestOptions the request comosItemRequestOptions.
     * @return an {@link Mono} containing the Cosmos item resource response with the replaced item or an error.
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
     * Reads an item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain an item response with the read item.
     *
     * @param <T> the type parameter.
     * @param id the item id.
     * @param partitionKey the partition key.
     * @param classType the item type.
     * @return an {@link Mono} containing the Cosmos item response with the read item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String id, PartitionKey partitionKey, Class<T> classType) {
        return readItem(id, partitionKey, ModelBridgeInternal.createCosmosItemRequestOptions(partitionKey), classType);
    }

    /**
     * Reads an item using a configured {@link CosmosItemRequestOptions}.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a Cosmos item response with the read item.
     *
     * @param <T> the type parameter.
     * @param id the item id.
     * @param partitionKey the partition key.
     * @param requestOptions the request {@link CosmosItemRequestOptions}.
     * @param classType the item type.
     * @return an {@link Mono} containing the Cosmos item response with the read item or an error.
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
            this.encryptionProcessor.decrypt(cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
            .map(bytes -> this.responseFactory.createItemResponse(cosmosItemResponse, classType)));
    }

    /**
     * Query for items in the current container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, Class<T> classType) {
        return this.queryItems(new SqlQuerySpec(query), classType);
    }

    /**
     * Query for items in the current container using a string.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param requestOptions the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, CosmosQueryRequestOptions requestOptions,
                                             Class<T> classType) {
        if (requestOptions == null) {
            requestOptions = new CosmosQueryRequestOptions();
        }

        return this.queryItems(new SqlQuerySpec(query), requestOptions, classType);
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpec}.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param querySpec the SQL query specification.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, Class<T> classType) {
        return queryItemsHelper(querySpec, new CosmosQueryRequestOptions(), classType, false);
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpec} and {@link CosmosQueryRequestOptions}.
     * <p>
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param query the SQL query specification.
     * @param requestOptions the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec query, CosmosQueryRequestOptions requestOptions,
                                             Class<T> classType) {
        if (requestOptions == null) {
            requestOptions = new CosmosQueryRequestOptions();
        }

        return queryItemsHelper(query, requestOptions, classType,false);
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

        if (specWithEncryptionAccessor.getEncryptionParamMap(sqlQuerySpecWithEncryption).size() > 0) {
            List<Mono<Void>> encryptionSqlParameterMonoList = new ArrayList<>();
            for (Map.Entry<String, SqlParameter> entry :
                specWithEncryptionAccessor.getEncryptionParamMap(sqlQuerySpecWithEncryption).entrySet()) {
                encryptionSqlParameterMonoList.add(specWithEncryptionAccessor.addEncryptionParameterAsync(sqlQuerySpecWithEncryption, entry.getKey(), entry.getValue(), this));
            }
            Mono<List<Void>> listMono = Flux.mergeSequential(encryptionSqlParameterMonoList).collectList();
            Mono<SqlQuerySpec> sqlQuerySpecMono =
                listMono.flatMap(ignoreVoids -> Mono.just(specWithEncryptionAccessor.getSqlQuerySpec(sqlQuerySpecWithEncryption)));
            return queryItemsHelperWithMonoSqlQuerySpec(sqlQuerySpecMono, sqlQuerySpecWithEncryption, options, classType, false);
        } else {
            return queryItemsHelper(specWithEncryptionAccessor.getSqlQuerySpec(sqlQuerySpecWithEncryption),
                options, classType, false);
        }
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
    // TODO Make this api public once it is GA in cosmos core library
    <T> CosmosPagedFlux<T> queryChangeFeed(CosmosChangeFeedRequestOptions options, Class<T> classType) {
        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        return queryChangeFeedHelper(options, classType,false);
    }

    /**
     * Run patch operations on an Item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the patched item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param options the request options.
     * @param itemType the item type.
     *
     * @return an {@link Mono} containing the Cosmos item resource response with the patched item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchItemRequestOptions options,
        Class<T> itemType) {

        checkNotNull(itemId, "expected non-null itemId");
        checkNotNull(partitionKey, "expected non-null partitionKey for patchItem");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        if (options == null) {
            options = new CosmosPatchItemRequestOptions();
        }

        return patchItemHelper(itemId, partitionKey, cosmosPatchOperations, options, itemType);
    }

    private <T> Mono<CosmosItemResponse<T>> patchItemHelper(String itemId,
                                                           PartitionKey partitionKey,
                                                           CosmosPatchOperations cosmosPatchOperations,
                                                           CosmosPatchItemRequestOptions options,
                                                           Class<T> itemType) {
        this.setRequestHeaders(options);
        List<Mono<PatchOperation>> monoList = new ArrayList<>();
        List<PatchOperation> operations = cosmosPatchOperationsAccessor.getPatchOperations(cosmosPatchOperations);
        List<PatchOperation> operationsSnapshot;
        synchronized (operations) {
            operationsSnapshot = new ArrayList<>(operations);
        }

        for (PatchOperation patchOperation : operationsSnapshot) {
            Mono<PatchOperation> itemPatchOperationMono = null;
            if (patchOperation.getOperationType() == PatchOperationType.REMOVE) {
                itemPatchOperationMono = Mono.just(patchOperation);
            }
            else if (patchOperation.getOperationType() == PatchOperationType.INCREMENT) {
                throw new IllegalArgumentException("Increment patch operation is not allowed for encrypted path");
            }
            else if (patchOperation instanceof PatchOperationCore) {
                JsonNode objectNode = EncryptionUtils.getSimpleObjectMapper().valueToTree(((PatchOperationCore)patchOperation).getResource());
                itemPatchOperationMono =
                    encryptionProcessor.encryptPatchNode(objectNode, ((PatchOperationCore)patchOperation).getPath()).map(encryptedObjectNode -> {
                        return new PatchOperationCore<>(
                            patchOperation.getOperationType(),
                            ((PatchOperationCore)patchOperation).getPath(),
                            encryptedObjectNode
                        );
                    });
                }
            monoList.add(itemPatchOperationMono);
        }
        Mono<List<PatchOperation>> encryptedPatchOperationsListMono =
            Flux.mergeSequential(monoList).collectList();
        CosmosPatchItemRequestOptions finalRequestOptions = options;

        CosmosPatchOperations encryptedCosmosPatchOperations = CosmosPatchOperations.create();

        return encryptedPatchOperationsListMono.flatMap(patchOperations -> {
            List<PatchOperation> snapshot =
                cosmosPatchOperationsAccessor.getPatchOperations(encryptedCosmosPatchOperations);

            synchronized(snapshot) {
                snapshot.addAll(patchOperations);
            }

            return patchItemInternalHelper(
                itemId, partitionKey, encryptedCosmosPatchOperations, finalRequestOptions,itemType, false);
        });
    }

    @SuppressWarnings("unchecked") // Casting cosmosItemResponse to CosmosItemResponse<byte[]> from CosmosItemResponse<T>
    private <T> Mono<CosmosItemResponse<T>> patchItemInternalHelper(String itemId,
                                                                    PartitionKey partitionKey,
                                                                    CosmosPatchOperations encryptedCosmosPatchOperations,
                                                                    CosmosPatchItemRequestOptions requestOptions,
                                                                    Class<T> itemType,
                                                                    boolean isRetry) {

        setRequestHeaders(requestOptions);
        return this.container.patchItem(itemId, partitionKey, encryptedCosmosPatchOperations, requestOptions, itemType).publishOn(encryptionScheduler).
            flatMap(cosmosItemResponse -> setByteArrayContent((CosmosItemResponse<byte[]>) cosmosItemResponse,
                this.encryptionProcessor.decrypt(cosmosItemResponseBuilderAccessor.getByteArrayContent((CosmosItemResponse<byte[]>) cosmosItemResponse)))
                .map(bytes -> this.responseFactory.createItemResponse((CosmosItemResponse<byte[]>) cosmosItemResponse,
                    itemType))).onErrorResume(exception -> {
                if (!isRetry && exception instanceof CosmosException) {
                    final CosmosException cosmosException = (CosmosException) exception;
                    if (isIncorrectContainerRid(cosmosException)) {
                        this.encryptionProcessor.getIsEncryptionSettingsInitDone().set(false);
                        return this.encryptionProcessor.initializeEncryptionSettingsAsync(true).then
                            (Mono.defer(() -> patchItemInternalHelper(itemId, partitionKey, encryptedCosmosPatchOperations, requestOptions, itemType, true)));
                    }
                }
                return Mono.error(exception);
            });
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
                cosmosItemResponseBuilderAccessor.setByteArrayContent(rsp, bytes);
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
                            ModelBridgeInternal.getNoChangesFromFeedResponse(page)
                            : false;
                        List<Mono<JsonNode>> jsonNodeArrayMonoList =
                            page.getResults().stream().map(jsonNode -> decryptResponseNode(jsonNode)).collect(Collectors.toList());
                        return Flux.concat(jsonNodeArrayMonoList).map(
                            item -> getItemDeserializer().convert(classType, item)
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
            .flatMap(encryptedPayload -> createItemHelper(
                encryptedPayload,
                partitionKey,
                requestOptions)
                .publishOn(encryptionScheduler)
                .flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                    this.encryptionProcessor.decrypt(cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
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

    private <T> Mono<CosmosItemResponse<byte[]>> createItemHelper(byte[] encryptedPayload,
                                                                  PartitionKey partitionKey,
                                                                  CosmosItemRequestOptions requestOptions) {
        return partitionKey != null
            ? this.container.createItem(encryptedPayload, partitionKey, requestOptions)
            : this.container.createItem(encryptedPayload, requestOptions);
    }

    private <T> Mono<CosmosItemResponse<T>> upsertItemHelper(byte[] streamPayload,
                                                             PartitionKey partitionKey,
                                                             CosmosItemRequestOptions requestOptions,
                                                             Class<T> itemClass,
                                                             boolean isRetry) {
        this.setRequestHeaders(requestOptions);
        return this.encryptionProcessor.encrypt(streamPayload)
            .flatMap(encryptedPayload -> upsertItemHelper(
                encryptedPayload,
                partitionKey,
                requestOptions)
                .publishOn(encryptionScheduler)
                .flatMap(cosmosItemResponse -> setByteArrayContent(cosmosItemResponse,
                    this.encryptionProcessor.decrypt(cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
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

    private <T> Mono<CosmosItemResponse<byte[]>> upsertItemHelper(byte[] encryptedPayload,
                                                             PartitionKey partitionKey,
                                                             CosmosItemRequestOptions requestOptions) {
        return partitionKey != null
            ? this.container.upsertItem(encryptedPayload, partitionKey, requestOptions)
            : this.container.upsertItem(encryptedPayload, requestOptions);
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
                    this.encryptionProcessor.decrypt(cosmosItemResponseBuilderAccessor.getByteArrayContent(cosmosItemResponse)))
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
                        (CosmosPagedFlux.defer(() -> queryItemsHelper(specWithEncryptionAccessor.getSqlQuerySpec(sqlQuerySpecWithEncryption), finalOptions, classType, true).byPage())));
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
    public Mono<CosmosBatchResponse> executeCosmosBatch(CosmosBatch cosmosBatch, CosmosBatchRequestOptions requestOptions) {
        if (requestOptions == null) {
            requestOptions = new CosmosBatchRequestOptions();
        }

        List<Mono<ItemBatchOperation<?>>> monoList = new ArrayList<>();
        for (ItemBatchOperation<?> itemBatchOperation : cosmosBatchAccessor.getOperationsInternal(cosmosBatch)) {
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
            cosmosBatchAccessor.getOperationsInternal(encryptedCosmosBatch).addAll(itemBatchOperations);
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
                cosmosBatchResponseAccessor.getResults(cosmosBatchResponse)) {
                ObjectNode objectNode =
                    cosmosBatchOperationResultAccessor.getResourceObject(cosmosBatchOperationResult);
                if (objectNode != null) {
                    decryptMonoList.add(encryptionProcessor.decryptJsonNode(objectNode).flatMap(jsonNode -> {
                        cosmosBatchOperationResultAccessor.setResourceObject(cosmosBatchOperationResult, (ObjectNode) jsonNode);
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

    /**
     * Executes flux of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     * @param operations Flux of operation which will be executed by this container.
     *
     * @return A Flux of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Flux<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Flux<CosmosItemOperation> operations) {

        return this.executeBulkOperations(operations, new CosmosBulkExecutionOptions());
    }

    /**
     * Executes flux of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     *
     * @param operations Flux of operation which will be executed by this container.
     * @param bulkOptions Options that apply for this Bulk request which specifies options regarding execution like
     *                    concurrency, batching size, interval and context.
     *
     * @return A Flux of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    @SuppressWarnings("unchecked")
    public <TContext> Flux<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Flux<CosmosItemOperation> operations,
        CosmosBulkExecutionOptions bulkOptions) {
        if (bulkOptions == null) {
            bulkOptions = new CosmosBulkExecutionOptions();
        }

        final CosmosBulkExecutionOptions cosmosBulkExecutionOptions = bulkOptions;
        Flux<CosmosItemOperation> operationFlux = operations.flatMap(cosmosItemOperation -> {
            Mono<CosmosItemOperation> cosmosItemOperationMono;
            if (cosmosItemOperation.getItem() != null) {
                ObjectNode objectNode =
                    EncryptionUtils.getSimpleObjectMapper().valueToTree(cosmosItemOperation.getItem());
                assert cosmosItemOperation instanceof ItemBulkOperation;
                cosmosItemOperationMono =
                    this.encryptionProcessor.encryptObjectNode(objectNode).map(encryptedItem -> new ItemBulkOperation<>(
                        cosmosItemOperation.getOperationType(),
                        cosmosItemOperation.getId(),
                        cosmosItemOperation.getPartitionKeyValue(),
                        ((ItemBulkOperation<JsonNode, TContext>) cosmosItemOperation).getRequestOptions(),
                        encryptedItem,
                        cosmosItemOperation.getContext()
                    ));
            } else {
                cosmosItemOperationMono = Mono.just(
                    new ItemBulkOperation<>(
                        cosmosItemOperation.getOperationType(),
                        cosmosItemOperation.getId(),
                        cosmosItemOperation.getPartitionKeyValue(),
                        ((ItemBulkOperation<JsonNode, TContext>) cosmosItemOperation).getRequestOptions(),
                        null,
                        cosmosItemOperation.getContext()
                    )
                );
            }
            return cosmosItemOperationMono;
        });

        Mono<List<CosmosItemOperation>> listMono = operationFlux.collectList();
        setRequestHeaders(cosmosBulkExecutionOptions);
        operationFlux = listMono.flatMapMany(Flux::fromIterable);
        return executeBulkOperationsHelper(operationFlux, cosmosBulkExecutionOptions, false);
    }

    @SuppressWarnings("unchecked")
    private <TContext> Flux<CosmosBulkOperationResponse<TContext>> executeBulkOperationsHelper(Flux<CosmosItemOperation> operations,
                                                                                               CosmosBulkExecutionOptions bulkOptions,
                                                                                               boolean isRetry) {
        return this.container.executeBulkOperations(operations, bulkOptions).flatMap(cosmosBulkOperationResponse -> {

            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            ObjectNode objectNode = cosmosBulkItemResponseAccessor.getResourceObject(cosmosBulkItemResponse);

            if(objectNode != null) {
                Mono<JsonNode> jsonNodeMono = encryptionProcessor.decryptJsonNode(objectNode).flatMap(jsonNode -> {
                    cosmosBulkItemResponseAccessor.setResourceObject(cosmosBulkItemResponse,
                        (ObjectNode) jsonNode);
                    return Mono.just(jsonNode);
                });
                return jsonNodeMono.flux().flatMap(jsonNode -> Flux.just((CosmosBulkOperationResponse<TContext>) cosmosBulkOperationResponse));
            }
            return Mono.just((CosmosBulkOperationResponse<TContext>) cosmosBulkOperationResponse);
        });
    }


    private void setRequestHeaders(CosmosItemRequestOptions requestOptions) {
        cosmosItemRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        cosmosItemRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosQueryRequestOptions requestOptions) {
        cosmosQueryRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        cosmosQueryRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosChangeFeedRequestOptions requestOptions) {
        cosmosChangeFeedRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        cosmosChangeFeedRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosBatchRequestOptions requestOptions) {
        cosmosBatchRequestOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        cosmosBatchRequestOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    private void setRequestHeaders(CosmosBulkExecutionOptions requestOptions) {
        cosmosBulkExecutionOptionsAccessor.setHeader(requestOptions, Constants.IS_CLIENT_ENCRYPTED_HEADER, "true");
        cosmosBulkExecutionOptionsAccessor.setHeader(requestOptions, Constants.INTENDED_COLLECTION_RID_HEADER, this.encryptionProcessor.getContainerRid());
    }

    boolean isIncorrectContainerRid(CosmosException cosmosException) {
        return cosmosException.getStatusCode() == HttpConstants.StatusCodes.BADREQUEST &&
            cosmosException.getResponseHeaders().get(HttpConstants.HttpHeaders.SUB_STATUS)
                .equals(Constants.INCORRECT_CONTAINER_RID_SUB_STATUS);
    }

    static {
        EncryptionImplementationBridgeHelpers.CosmosEncryptionAsyncContainerHelper.setCosmosEncryptionAsyncContainerAccessor(new EncryptionImplementationBridgeHelpers.CosmosEncryptionAsyncContainerHelper.CosmosEncryptionAsyncContainerAccessor() {
            @Override
            public EncryptionProcessor getEncryptionProcessor(CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) {
                return cosmosEncryptionAsyncContainer.getEncryptionProcessor();
            }
        });
    }
}
