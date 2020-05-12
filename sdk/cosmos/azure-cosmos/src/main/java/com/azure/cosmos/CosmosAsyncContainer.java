// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosAsyncContainerResponse;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

/**
 * Provides methods for reading, deleting, and replacing existing Containers.
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosAsyncContainer {

    private final CosmosAsyncDatabase database;
    private final String id;
    private final String link;
    private CosmosAsyncScripts scripts;

    CosmosAsyncContainer(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
        this.link = getParentLink() + "/" + getURIPathSegment() + "/" + getId();
    }

    /**
     * Get the id of the {@link CosmosAsyncContainer}
     *
     * @return the id of the {@link CosmosAsyncContainer}
     */
    public String getId() {
        return id;
    }

    /**
     * Reads the document container
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos container response with
     * the read container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> read() {
        return read(new CosmosContainerRequestOptions());
    }

    /**
     * Reads the document container by the container link.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @param options The cosmos container request options.
     * @return an {@link Mono} containing the single cosmos container response with
     * the read container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> read(CosmosContainerRequestOptions options) {
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return database.getDocClientWrapper().readCollection(getLink(), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncContainerResponse(response, database)).single();
    }

    /**
     * Deletes the item container
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response for the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single cosmos container response for
     * the deleted database or an error.
     */
    public Mono<CosmosAsyncContainerResponse> delete(CosmosContainerRequestOptions options) {
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return database.getDocClientWrapper().deleteCollection(getLink(), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncContainerResponse(response, database)).single();
    }

    /**
     * Deletes the item container
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response for the
     * deleted container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos container response for
     * the deleted container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> delete() {
        return delete(new CosmosContainerRequestOptions());
    }

    /**
     * Replaces a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the replaced document container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the item container properties
     * @return an {@link Mono} containing the single cosmos container response with
     * the replaced document container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> replace(CosmosContainerProperties containerProperties) {
        return replace(containerProperties, null);
    }

    /**
     * Replaces a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the replaced document container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the item container properties
     * @param options the cosmos container request options.
     * @return an {@link Mono} containing the single cosmos container response with
     * the replaced document container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> replace(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        ModelBridgeInternal.validateResource(ModelBridgeInternal.getResourceFromResourceWrapper(containerProperties));
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return database.getDocClientWrapper()
                   .replaceCollection(ModelBridgeInternal.getV2Collection(containerProperties), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncContainerResponse(response, database)).single();
    }

    /* CosmosAsyncItem operations */

    /**
     * Creates a cosmos item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param item the cosmos item represented as a POJO or cosmos item object.
     * @return an {@link Mono} containing the single resource response with the
     * created cosmos item or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> createItem(T item) {
        return createItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Creates a cosmos item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param item the cosmos item represented as a POJO or cosmos item object.
     * @param partitionKey the partition key
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the created cosmos item or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> createItem(
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
     * Create an item.
     *
     * @param <T> the type parameter
     * @param item the item
     * @param options the item request options
     * @return an {@link Mono} containing the single resource response with the created cosmos item or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> createItem(T item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return database.getDocClientWrapper()
                   .createDocument(getLink(),
                                   item,
                                   requestOptions,
                                   true)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType))
                   .single();
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param item the item represented as a POJO or Item object to upsert.
     * @return an {@link Mono} containing the single resource response with the upserted document or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> upsertItem(T item) {
        return upsertItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Upserts a cosmos item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param item the item represented as a POJO or Item object to upsert.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted document or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> upsertItem(T item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        return this.getDatabase().getDocClientWrapper()
                   .upsertDocument(this.getLink(), item,
                       ModelBridgeInternal.toRequestOptions(options),
                                   true)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType))
                   .single();
    }

    /**
     * Reads all cosmos items in the container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter
     * @param classType the class type
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read cosmos items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> readAllItems(Class<T> classType) {
        return readAllItems(new FeedOptions(), classType);
    }

    /**
     * Reads all cosmos items in a container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter
     * @param options the feed options.
     * @param classType the class type
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read cosmos items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> readAllItems(FeedOptions options, Class<T> classType) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper().readDocuments(getLink(), options).map(
                response -> prepareFeedResponse(response, classType));
        });
    }

    /**
     * Query for documents in a items in a container
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter
     * @param query the query.
     * @param classType the class type
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, Class<T> classType) {
        return queryItems(new SqlQuerySpec(query), classType);
    }

    /**
     * Query for documents in a items in a container
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter
     * @param query the query.
     * @param options the feed options.
     * @param classType the class type
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, FeedOptions options, Class<T> classType) {
        return queryItems(new SqlQuerySpec(query), options, classType);
    }

    /**
     * Query for documents in a items in a container
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter
     * @param querySpec the SQL query specification.
     * @param classType the class type
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, Class<T> classType) {
        return queryItems(querySpec, new FeedOptions(), classType);
    }

    /**
     * Query for documents in a items in a container
     * <p>
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter
     * @param querySpec the SQL query specification.
     * @param options the feed options.
     * @param classType the class type
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, FeedOptions options, Class<T> classType) {
        return queryItemsInternal(querySpec, options, classType);
    }

    private <T> CosmosPagedFlux<T> queryItemsInternal(
        SqlQuerySpec sqlQuerySpec, FeedOptions feedOptions, Class<T> classType) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, feedOptions);
            return getDatabase().getDocClientWrapper()
                       .queryDocuments(CosmosAsyncContainer.this.getLink(), sqlQuerySpec, feedOptions)
                       .map(response ->
                                prepareFeedResponse(response, classType));
        });
    }

    private <T> FeedResponse<T> prepareFeedResponse(FeedResponse<Document> response, Class<T> classType) {
        return BridgeInternal.createFeedResponseWithQueryMetrics(
            (response.getResults().stream().map(document -> ModelBridgeInternal.toObjectFromJsonSerializable(document, classType))
                 .collect(Collectors.toList())), response.getResponseHeaders(),
            ModelBridgeInternal.queryMetrics(response));
    }


    /**
     * Reads an item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a cosmos item response with the read item
     * In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param itemType the item type
     * @return an {@link Mono} containing the cosmos item response with the read item or an error
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) {
        return readItem(itemId, partitionKey, ModelBridgeInternal.createCosmosItemRequestOptions(partitionKey), itemType);
    }

    /**
     * Reads an item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a cosmos item response with the read item
     * In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param options the request cosmosItemRequestOptions
     * @param itemType the item type
     * @return an {@link Mono} containing the cosmos item response with the read item or an error
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> readItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options, Class<T> itemType) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return this.getDatabase().getDocClientWrapper()
                   .readDocument(getItemLink(itemId), requestOptions)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType))
                   .single();
    }

    /**
     * Replaces an item with the passed in item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param item the item to replace (containing the document id).
     * @param itemId the item id
     * @param partitionKey the partition key
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> replaceItem(T item, String itemId, PartitionKey partitionKey) {
        return replaceItem(item, itemId, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Replaces an item with the passed in item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter
     * @param item the item to replace (containing the document id).
     * @param itemId the item id
     * @param partitionKey the partition key
     * @param options the request comosItemRequestOptions
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public <T> Mono<CosmosAsyncItemResponse<T>> replaceItem(
        T item, String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        Document doc = CosmosItemProperties.fromObject(item);
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        return this.getDatabase()
                   .getDocClientWrapper()
                   .replaceDocument(getItemLink(itemId), doc, ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType))
                   .single();
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param itemId the item id
     * @param partitionKey the partition key
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosAsyncItemResponse<Object>> deleteItem(String itemId, PartitionKey partitionKey) {
        return deleteItem(itemId, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param itemId id of the item
     * @param partitionKey partitionKey of the item
     * @param options the request options
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosAsyncItemResponse<Object>> deleteItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return this.getDatabase()
                   .getDocClientWrapper()
                   .deleteDocument(getItemLink(itemId), requestOptions)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponseWithObjectType(response))
                   .single();
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
     * Gets scripts. This can be used to perform various operations on cosmos scripts
     *
     * @return the {@link CosmosAsyncScripts}
     */
    public CosmosAsyncScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosAsyncScripts(this);
        }
        return this.scripts;
    }

    /**
     * Lists all the conflicts in the container
     *
     * @param options the feed options
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> readAllConflicts(FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper().readConflicts(getLink(), options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosConflictPropertiesFromV2Results(response.getResults()),
                           response.getResponseHeaders()));
        });
    }

    /**
     * Queries all the conflicts in the container
     *
     * @param query the query
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> queryConflicts(String query) {
        return queryConflicts(query, new FeedOptions());
    }

    /**
     * Queries all the conflicts in the container
     *
     * @param query the query
     * @param options the feed options
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> queryConflicts(String query, FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper().queryConflicts(getLink(), query, options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosConflictPropertiesFromV2Results(response.getResults()),
                           response.getResponseHeaders()));
        });
    }

    /**
     * Gets a CosmosAsyncConflict object without making a service call
     *
     * @param id id of the cosmos conflict
     * @return a cosmos conflict
     */
    public CosmosAsyncConflict getConflict(String id) {
        return new CosmosAsyncConflict(id, this);
    }

    /**
     * Gets the throughput of the container
     *
     * @return a {@link Mono} containing throughput or an error.
     */
    public Mono<Integer> readProvisionedThroughput() {
        return this.read()
                   .flatMap(cosmosContainerResponse ->
                                database.getDocClientWrapper()
                                    .queryOffers("select * from c where c.offerResourceId = '"
                                                     + cosmosContainerResponse.getProperties()
                                                           .getResourceId() + "'", new FeedOptions())
                                    .single())
                   .flatMap(offerFeedResponse -> {
                       if (offerFeedResponse.getResults().isEmpty()) {
                           return Mono.error(
                               BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
                                                                          "No offers found for the resource"));
                       }
                       return database.getDocClientWrapper()
                                  .readOffer(offerFeedResponse.getResults().get(0).getSelfLink())
                                  .single();
                   }).map(cosmosOfferResponse -> cosmosOfferResponse.getResource().getThroughput());
    }

    /**
     * Sets throughput provisioned for a container in measurement of
     * Requests-per-Unit in the Azure Cosmos service.
     *
     * @param requestUnitsPerSecond the cosmos container throughput, expressed in
     * Request Units per second
     * @return a {@link Mono} containing throughput or an error.
     */
    public Mono<Integer> replaceProvisionedThroughput(int requestUnitsPerSecond) {
        return this.read()
                   .flatMap(cosmosContainerResponse ->
                                database.getDocClientWrapper()
                                    .queryOffers("select * from c where c.offerResourceId = '"
                                                     + cosmosContainerResponse.getProperties()
                                                           .getResourceId() + "'", new FeedOptions())
                                    .single())
                   .flatMap(offerFeedResponse -> {
                       if (offerFeedResponse.getResults().isEmpty()) {
                           return Mono.error(
                               BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
                                                                          "No offers found for the resource"));
                       }
                       Offer offer = offerFeedResponse.getResults().get(0);
                       offer.setThroughput(requestUnitsPerSecond);
                       return database.getDocClientWrapper().replaceOffer(offer).single();
                   }).map(offerResourceResponse -> offerResourceResponse.getResource().getThroughput());
    }

    /**
     * Replace the throughput .
     *
     * @param throughputProperties the throughput properties
     * @return the mono containing throughput response
     */
    public Mono<ThroughputResponse> replaceThroughput(ThroughputProperties throughputProperties) {
        return this.read()
                   .flatMap(response -> this.database.getDocClientWrapper()
                                            .queryOffers(database.getOfferQuerySpecFromResourceId(response
                                                                                                      .getProperties()
                                                                                                      .getResourceId())
                                                , new FeedOptions())
                                            .single()
                                            .flatMap(offerFeedResponse -> {
                                                if (offerFeedResponse.getResults().isEmpty()) {
                                                    return Mono.error(BridgeInternal
                                                                          .createCosmosClientException(
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

    /**
     * Read the throughput throughput .
     *
     * @return the mono containing throughput response
     */
    public Mono<ThroughputResponse> readThroughput() {
        return this.read()
                   .flatMap(response -> this.database.getDocClientWrapper()
                                            .queryOffers(database.getOfferQuerySpecFromResourceId(response
                                                                                                      .getProperties()
                                                                                                      .getResourceId())
                                                , new FeedOptions())
                                            .single()
                                            .flatMap(offerFeedResponse -> {
                                                if (offerFeedResponse.getResults().isEmpty()) {
                                                    return Mono.error(BridgeInternal
                                                                          .createCosmosClientException(
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


    /**
     * Gets the parent Database
     *
     * @return the {@link CosmosAsyncDatabase}
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
}
