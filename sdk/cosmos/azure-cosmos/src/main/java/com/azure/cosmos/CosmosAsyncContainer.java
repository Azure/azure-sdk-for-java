// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.Beta;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

/**
 * Provides methods for reading, deleting, and replacing existing Containers.
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosAsyncContainer {

    private final CosmosAsyncDatabase database;
    private final String id;
    private final String link;
    private final String replaceContainerSpanName;
    private final String deleteContainerSpanName;
    private final String replaceThroughputSpanName;
    private final String readThroughputSpanName;
    private final String readContainerSpanName;
    private final String readItemSpanName;
    private final String upsertItemSpanName;
    private final String deleteItemSpanName;
    private final String replaceItemSpanName;
    private final String createItemSpanName;
    private final String readAllItemsSpanName;
    private final String queryItemsSpanName;
    private final String readAllConflictsSpanName;
    private final String queryConflictsSpanName;
    private CosmosAsyncScripts scripts;

    CosmosAsyncContainer(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
        this.link = getParentLink() + "/" + getURIPathSegment() + "/" + getId();
        this.replaceContainerSpanName = "replaceContainer." + this.id;
        this.deleteContainerSpanName = "deleteContainer." + this.id;
        this.replaceThroughputSpanName = "replaceThroughput." + this.id;
        this.readThroughputSpanName = "readThroughput." + this.id;
        this.readContainerSpanName = "readContainer." + this.id;
        this.readItemSpanName = "readItem." + this.id;
        this.upsertItemSpanName = "upsertItem." + this.id;
        this.deleteItemSpanName = "deleteItem." + this.id;
        this.replaceItemSpanName = "replaceItem." + this.id;
        this.createItemSpanName = "createItem." + this.id;
        this.readAllItemsSpanName = "readAllItems." + this.id;
        this.queryItemsSpanName = "queryItems." + this.id;
        this.readAllConflictsSpanName = "readAllConflicts." + this.id;
        this.queryConflictsSpanName = "queryConflicts." + this.id;
    }

    /**
     * Get the id of the {@link CosmosAsyncContainer}.
     *
     * @return the id of the {@link CosmosAsyncContainer}.
     */
    public String getId() {
        return id;
    }

    /**
     * Reads the current container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single Cosmos container response with
     * the read container or an error.
     */
    public Mono<CosmosContainerResponse> read() {
        return read(new CosmosContainerRequestOptions());
    }

    /**
     * Reads the current container while specifying additional options such as If-Match.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @param options the Cosmos container request options.
     * @return an {@link Mono} containing the single Cosmos container response with
     * the read container or an error.
     */
    public Mono<CosmosContainerResponse> read(CosmosContainerRequestOptions options) {
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> read(requestOptions, context));
    }

    /**
     * Deletes the container
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response for the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single Cosmos container response for
     * the deleted database or an error.
     */
    public Mono<CosmosContainerResponse> delete(CosmosContainerRequestOptions options) {
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> deleteInternal(requestOptions, context));
    }

    /**
     * Deletes the current container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response for the
     * deleted container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single Cosmos container response for
     * the deleted container or an error.
     */
    public Mono<CosmosContainerResponse> delete() {
        return delete(new CosmosContainerRequestOptions());
    }

    /**
     * Replaces the current container's properties.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the replaced container properties. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties
     * @return an {@link Mono} containing the single Cosmos container response with
     * the replaced container properties or an error.
     */
    public Mono<CosmosContainerResponse> replace(CosmosContainerProperties containerProperties) {
        return replace(containerProperties, null);
    }

    /**
     * Replaces the current container properties while using non-default request options.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the replaced container properties. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties
     * @param options the Cosmos container request options.
     * @return an {@link Mono} containing the single Cosmos container response with
     * the replaced container properties or an error.
     */
    public Mono<CosmosContainerResponse> replace(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> replaceInternal(containerProperties, requestOptions, context));
    }

    /* CosmosAsyncItem operations */

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
    public <T> Mono<CosmosItemResponse<T>> createItem(T item) {
        return createItem(item, new CosmosItemRequestOptions());
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
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the created Cosmos item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(
        T item,
        PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        return createItem(item, options);
    }

    /**
     * Creates a Cosmos item.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param options the item request options.
     * @return an {@link Mono} containing the single resource response with the created Cosmos item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(T item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }

        final CosmosItemRequestOptions requestOptions = options;
        return withContext(context -> createItemInternal(item, requestOptions, context));
    }

    private <T> Mono<CosmosItemResponse<T>> createItemInternal(T item, CosmosItemRequestOptions options, Context context) {
        Mono<CosmosItemResponse<T>> responseMono = createItemInternal(item, options);
        return database.getClient().getTracerProvider().traceEnabledCosmosItemResponsePublisher(responseMono,
            context,
            this.createItemSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private <T> Mono<CosmosItemResponse<T>> createItemInternal(T item, CosmosItemRequestOptions options) {
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return database.getDocClientWrapper()
                   .createDocument(getLink(),
                                   item,
                                   requestOptions,
                                   true)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
                   .single();
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
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item, CosmosItemRequestOptions options) {
        final CosmosItemRequestOptions requestOptions = options == null ? new CosmosItemRequestOptions() : options;
        return withContext(context -> upsertItemInternal(item, requestOptions, context));
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
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item, PartitionKey partitionKey, CosmosItemRequestOptions options) {
        final CosmosItemRequestOptions requestOptions = options == null ? new CosmosItemRequestOptions() : options;
        ModelBridgeInternal.setPartitionKey(requestOptions, partitionKey);
        return withContext(context -> upsertItemInternal(item, requestOptions, context));
    }

    /**
     * Reads all the items in the current container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read Cosmos items or an
     * error.
     */
    <T> CosmosPagedFlux<T> readAllItems(Class<T> classType) {
        return readAllItems(new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Reads all the items in the current container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param options the feed options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read Cosmos items or an
     * error.
     */
    <T> CosmosPagedFlux<T> readAllItems(CosmosQueryRequestOptions options, Class<T> classType) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(),
                this.readAllItemsSpanName,
                this.getDatabase().getClient().getServiceEndpoint(), database.getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper().readDocuments(getLink(), options).map(
                response -> prepareFeedResponse(response, classType));
        });
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
        return queryItemsInternal(new SqlQuerySpec(query), new CosmosQueryRequestOptions(), classType);
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
     * @param options the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, CosmosQueryRequestOptions options, Class<T> classType) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryItemsInternal(new SqlQuerySpec(query), options, classType);
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
        return queryItemsInternal(querySpec, new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpec} and {@link CosmosQueryRequestOptions}.
     * <p>
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> classType) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryItemsInternal(querySpec, options, classType);
    }

    <T> CosmosPagedFlux<T> queryItemsInternal(
        SqlQuerySpec sqlQuerySpec, CosmosQueryRequestOptions cosmosQueryRequestOptions, Class<T> classType) {
        return UtilBridgeInternal.createCosmosPagedFlux(queryItemsInternalFunc(sqlQuerySpec, cosmosQueryRequestOptions, classType));
    }

    <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFunc(
        SqlQuerySpec sqlQuerySpec, CosmosQueryRequestOptions cosmosQueryRequestOptions, Class<T> classType) {
        Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> pagedFluxOptionsFluxFunction = (pagedFluxOptions -> {
            String spanName = this.queryItemsSpanName;
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(), spanName,
                this.getDatabase().getClient().getServiceEndpoint(), database.getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, cosmosQueryRequestOptions);

                return getDatabase().getDocClientWrapper()
                             .queryDocuments(CosmosAsyncContainer.this.getLink(), sqlQuerySpec, cosmosQueryRequestOptions)
                             .map(response -> prepareFeedResponse(response, classType));
        });

        return pagedFluxOptionsFluxFunction;
    }

    private <T> FeedResponse<T> prepareFeedResponse(FeedResponse<Document> response, Class<T> classType) {
        QueryInfo queryInfo = ModelBridgeInternal.getQueryInfoFromFeedResponse(response);
        if (queryInfo != null && queryInfo.hasSelectValue()) {
            List<T> transformedResults = response.getResults()
                                                 .stream()
                                                 .map(d -> d.has(Constants.Properties.VALUE) ?
                                                     transform(d.get(Constants.Properties.VALUE), classType) :
                                                     ModelBridgeInternal.toObjectFromJsonSerializable(d, classType))
                                                 .collect(Collectors.toList());

            return BridgeInternal.createFeedResponseWithQueryMetrics(transformedResults,
                response.getResponseHeaders(),
                ModelBridgeInternal.queryMetrics(response),
                ModelBridgeInternal.getQueryPlanDiagnosticsContext(response));

        }
        return BridgeInternal.createFeedResponseWithQueryMetrics(
            (response.getResults().stream().map(document -> ModelBridgeInternal.toObjectFromJsonSerializable(document,
                classType))
                     .collect(Collectors.toList())), response.getResponseHeaders(),
            ModelBridgeInternal.queryMetrics(response),
            ModelBridgeInternal.getQueryPlanDiagnosticsContext(response));
    }

    private <T> T transform(Object object, Class<T> classType) {
        return Utils.getSimpleObjectMapper().convertValue(object, classType);
    }

    /**
     * Reads an item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain an item response with the read item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param itemType the item type.
     * @return an {@link Mono} containing the Cosmos item response with the read item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) {
        return readItem(itemId, partitionKey, ModelBridgeInternal.createCosmosItemRequestOptions(partitionKey), itemType);
    }

    /**
     * Reads an item using a configured {@link CosmosItemRequestOptions}.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a Cosmos item response with the read item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the request {@link CosmosItemRequestOptions}.
     * @param itemType the item type.
     * @return an {@link Mono} containing the Cosmos item response with the read item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options, Class<T> itemType) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }

        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return withContext(context -> readItemInternal(itemId, requestOptions, itemType, context));
    }

    /**
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param itemKeyList document id and partition key pair that needs to be read
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> Mono<FeedResponse<T>> readMany(
        List<Pair<String, PartitionKey>> itemKeyList,
        Class<T> classType) {

        return this.readMany(itemKeyList, null, classType);
    }

    /**
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param itemKeyList document id and partition key pair that needs to be read
     * @param sessionToken the optional Session token - null if the read can be made without specific session token
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> Mono<FeedResponse<T>> readMany(
        List<Pair<String, PartitionKey>> itemKeyList,
        String sessionToken,
        Class<T> classType) {

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (!StringUtils.isNotEmpty(sessionToken)) {
            options = options.setSessionToken(sessionToken);
        }

        options.setMaxDegreeOfParallelism(-1);
        return CosmosBridgeInternal
            .getAsyncDocumentClient(this.getDatabase())
            .readMany(itemKeyList, BridgeInternal.getLink(this), options, classType);
    }

    /**
     * Reads all the items of a logical partition
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed responses of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages
     * of the read Cosmos items or an error.
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> CosmosPagedFlux<T> readAllItems(
        PartitionKey partitionKey,
        Class<T> classType) {

        return this.readAllItems(partitionKey, new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Reads all the items of a logical partition
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed responses of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param options the feed options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages 
     * of the read Cosmos items or an error.
     */
    @Beta(Beta.SinceVersion.V4_4_0)
    public <T> CosmosPagedFlux<T> readAllItems(
        PartitionKey partitionKey,
        CosmosQueryRequestOptions options,
        Class<T> classType) {
            
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(),
                this.readAllItemsSpanName,
                this.getDatabase().getClient().getServiceEndpoint(), database.getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase()
                .getDocClientWrapper()
                .readAllDocuments(getLink(), partitionKey, options)
                .map(response -> prepareFeedResponse(response, classType));
        });
    }

    /**
     * Replaces an item with the passed in item.
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
     * Replaces an item with the passed in item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the replaced item.
     *
     * @param <T> the type parameter.
     * @param item the item to replace (containing the item id).
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the request comosItemRequestOptions.
     * @return an {@link Mono} containing the Cosmos item resource response with the replaced item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> replaceItem(
        T item, String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        Document doc = InternalObjectNode.fromObject(item);
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        final CosmosItemRequestOptions requestOptions = options;
        return withContext(context -> replaceItemInternal(itemType, itemId, doc, requestOptions, context));
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
     * @param options the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return withContext(context -> deleteItemInternal(itemId, null, requestOptions, context));
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     *
     * @param <T> the type parameter.
     * @param item item to be deleted.
     * @param options the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public <T> Mono<CosmosItemResponse<Object>> deleteItem(T item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        InternalObjectNode internalObjectNode = InternalObjectNode.fromObjectToInternalObjectNode(item);
        return withContext(context -> deleteItemInternal(internalObjectNode.getId(), internalObjectNode, requestOptions, context));
    }

    private String getItemLink(String itemId) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getLink());
        builder.append("/");
        builder.append(Paths.DOCUMENTS_PATH_SEGMENT);
        builder.append("/");
        builder.append(itemId);
        return builder.toString();
    }

    /**
     * Gets a {@link CosmosAsyncScripts} using the current container as context.
     * <p>
     * This can be further used to perform various operations on Cosmos scripts.
     *
     * @return the {@link CosmosAsyncScripts}.
     */
    public CosmosAsyncScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosAsyncScripts(this);
        }
        return this.scripts;
    }

    /**
     * Lists all the conflicts in the current container.
     *
     * @param options the query request options
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> readAllConflicts(CosmosQueryRequestOptions options) {
        CosmosQueryRequestOptions requestOptions = options == null ? new CosmosQueryRequestOptions() : options;
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(),
                this.readAllConflictsSpanName,
                this.getDatabase().getClient().getServiceEndpoint(), database.getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, requestOptions);
            return database.getDocClientWrapper().readConflicts(getLink(), requestOptions)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosConflictPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    /**
     * Queries all the conflicts in the current container.
     *
     * @param query the query.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> queryConflicts(String query) {
        return queryConflicts(query, new CosmosQueryRequestOptions());
    }

    /**
     * Queries all the conflicts in the current container.
     *
     * @param query the query.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> queryConflicts(String query, CosmosQueryRequestOptions options) {
        final CosmosQueryRequestOptions requestOptions = options == null ? new CosmosQueryRequestOptions() : options;
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(),
                this.queryConflictsSpanName,
                this.getDatabase().getClient().getServiceEndpoint(), database.getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, requestOptions);
            return database.getDocClientWrapper().queryConflicts(getLink(), query, requestOptions)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosConflictPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    /**
     * Gets a {@link CosmosAsyncConflict} object using current container for context.
     *
     * @param id the id of the Cosmos conflict.
     * @return a Cosmos conflict.
     */
    public CosmosAsyncConflict getConflict(String id) {
        return new CosmosAsyncConflict(id, this);
    }

    /**
     * Replace the throughput.
     *
     * @param throughputProperties the throughput properties.
     * @return the mono containing throughput response.
     */
    public Mono<ThroughputResponse> replaceThroughput(ThroughputProperties throughputProperties) {
        return withContext(context -> replaceThroughputInternal(throughputProperties, context));
    }

    /**
     * Read the throughput provisioned for the current container.
     *
     * @return the mono containing throughput response.
     */
    public Mono<ThroughputResponse> readThroughput() {
        return withContext(context -> readThroughputInternal(context));
    }

    /**
     * Gets the parent {@link CosmosAsyncDatabase} for the current container.
     *
     * @return the {@link CosmosAsyncDatabase}.
     */
    public CosmosAsyncDatabase getDatabase() {
        return database;
    }

    String getURIPathSegment() {
        return Paths.COLLECTIONS_PATH_SEGMENT;
    }

    String getParentLink() {
        return database.getLink();
    }

    String getLink() {
        return this.link;
    }

    private Mono<CosmosItemResponse<Object>> deleteItemInternal(
        String itemId,
        InternalObjectNode internalObjectNode,
        RequestOptions requestOptions,
        Context context) {
        Mono<CosmosItemResponse<Object>> responseMono = this.getDatabase()
            .getDocClientWrapper()
            .deleteDocument(getItemLink(itemId), internalObjectNode, requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponseWithObjectType(response))
            .single();
        return database.getClient().getTracerProvider().traceEnabledCosmosItemResponsePublisher(responseMono,
            context,
            this.deleteItemSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private <T> Mono<CosmosItemResponse<T>> replaceItemInternal(
        Class<T> itemType,
        String itemId,
        Document doc,
        CosmosItemRequestOptions options,
        Context context) {
        Mono<CosmosItemResponse<T>> responseMono = this.getDatabase()
            .getDocClientWrapper()
            .replaceDocument(getItemLink(itemId), doc, ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
            .single();
        return database.getClient().getTracerProvider().traceEnabledCosmosItemResponsePublisher(responseMono,
            context, this.replaceItemSpanName, database.getId(), database.getClient().getServiceEndpoint());
    }

    private <T> Mono<CosmosItemResponse<T>> upsertItemInternal(T item, CosmosItemRequestOptions options, Context context) {
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        Mono<CosmosItemResponse<T>> responseMono = this.getDatabase().getDocClientWrapper()
            .upsertDocument(this.getLink(), item,
                ModelBridgeInternal.toRequestOptions(options),
                true)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
            .single();
        return database.getClient().getTracerProvider().traceEnabledCosmosItemResponsePublisher(responseMono,
            context,
            this.upsertItemSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private <T> Mono<CosmosItemResponse<T>> readItemInternal(
        String itemId,
        RequestOptions requestOptions, Class<T> itemType,
        Context context) {
        Mono<CosmosItemResponse<T>> responseMono = this.getDatabase().getDocClientWrapper()
            .readDocument(getItemLink(itemId), requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
            .single();
        return database.getClient().getTracerProvider().traceEnabledCosmosItemResponsePublisher(responseMono,
            context,
            this.readItemSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    Mono<CosmosContainerResponse> read(CosmosContainerRequestOptions options, Context context) {
        Mono<CosmosContainerResponse> responseMono = database.getDocClientWrapper().readCollection(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            this.readContainerSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosContainerResponse> deleteInternal(CosmosContainerRequestOptions options, Context context) {
        Mono<CosmosContainerResponse> responseMono = database.getDocClientWrapper().deleteCollection(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            this.deleteContainerSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosContainerResponse> replaceInternal(CosmosContainerProperties containerProperties,
                                                               CosmosContainerRequestOptions options,
                                                               Context context) {
        Mono<CosmosContainerResponse> responseMono = database.getDocClientWrapper()
            .replaceCollection(ModelBridgeInternal.getV2Collection(containerProperties),
                ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            this.replaceContainerSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<ThroughputResponse> readThroughputInternal(Context context) {
        Context nestedContext = context.addData(TracerProvider.COSMOS_CALL_DEPTH, TracerProvider.COSMOS_CALL_DEPTH_VAL);
        Mono<ThroughputResponse> responseMono = readThroughputInternal(this.read(new CosmosContainerRequestOptions(),
            nestedContext));
        return this.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            this.readThroughputSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<ThroughputResponse> readThroughputInternal(Mono<CosmosContainerResponse> responseMono) {
        return responseMono
            .flatMap(response -> this.database.getDocClientWrapper()
                .queryOffers(database.getOfferQuerySpecFromResourceId(response.getProperties()
                        .getResourceId())
                    , new CosmosQueryRequestOptions())
                .single()
                .flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.getResults().isEmpty()) {
                        return Mono.error(BridgeInternal
                            .createCosmosException(
                                HttpConstants.StatusCodes.BADREQUEST,
                                "No offers found for the resource "
                                    + this.getId()));
                    }
                    return this.database.getDocClientWrapper()
                        .readOffer(offerFeedResponse.getResults()
                            .get(0)
                            .getSelfLink())
                        .single();
                })
                .map(ModelBridgeInternal::createThroughputRespose));
    }

    private Mono<ThroughputResponse> replaceThroughputInternal(ThroughputProperties throughputProperties,
                                                               Context context) {
        Context nestedContext = context.addData(TracerProvider.COSMOS_CALL_DEPTH, TracerProvider.COSMOS_CALL_DEPTH_VAL);
        Mono<ThroughputResponse> responseMono =
            replaceThroughputInternal(this.read(new CosmosContainerRequestOptions(), nestedContext),
                throughputProperties);
        return this.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            this.replaceThroughputSpanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<ThroughputResponse> replaceThroughputInternal(Mono<CosmosContainerResponse> responseMono,
                                                               ThroughputProperties throughputProperties) {
        return responseMono
            .flatMap(response -> this.database.getDocClientWrapper()
                .queryOffers(database.getOfferQuerySpecFromResourceId(response.getProperties()
                        .getResourceId())
                    , new CosmosQueryRequestOptions())
                .single()
                .flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.getResults().isEmpty()) {
                        return Mono.error(BridgeInternal
                            .createCosmosException(
                                HttpConstants.StatusCodes.BADREQUEST,
                                "No offers found for the " +
                                    "resource " + this.getId()));
                    }

                    Offer existingOffer = offerFeedResponse.getResults().get(0);
                    Offer updatedOffer =
                        ModelBridgeInternal.updateOfferFromProperties(existingOffer,
                            throughputProperties);
                    return this.database.getDocClientWrapper()
                        .replaceOffer(updatedOffer)
                        .single();
                }).map(ModelBridgeInternal::createThroughputRespose));
    }

    ItemDeserializer getItemDeserializer() {
        return getDatabase().getDocClientWrapper().getItemDeserializer();
    }
}
