// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.Offer;
import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.RequestOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.data.cosmos.Resource.validateResource;

/**
 * Provides methods for reading, deleting, and replacing existing Containers.
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosContainer {

    private CosmosDatabase database;
    private String id;
    private CosmosScripts scripts;

    CosmosContainer(String id, CosmosDatabase database) {
        this.id = id;
        this.database = database;
    }

    /**
     * Get the id of the {@link CosmosContainer}
     * 
     * @return the id of the {@link CosmosContainer}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosContainer}
     * 
     * @param id the id of the {@link CosmosContainer}
     * @return the same {@link CosmosContainer} that had the id set
     */
    CosmosContainer id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads the document container
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos container response with
     *         the read container or an error.
     */
    public Mono<CosmosContainerResponse> read() {
        return read(new CosmosContainerRequestOptions());
    }

    /**
     * Reads the document container by the container link.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @param options The cosmos container request options.
     * @return an {@link Mono} containing the single cosmos container response with
     *         the read container or an error.
     */
    public Mono<CosmosContainerResponse> read(CosmosContainerRequestOptions options) {
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return database.getDocClientWrapper().readCollection(getLink(), options.toRequestOptions())
                .map(response -> new CosmosContainerResponse(response, database)).single();
    }

    /**
     * Deletes the item container
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response for the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single cosmos container response for
     *         the deleted database or an error.
     */
    public Mono<CosmosContainerResponse> delete(CosmosContainerRequestOptions options) {
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return database.getDocClientWrapper().deleteCollection(getLink(), options.toRequestOptions())
                .map(response -> new CosmosContainerResponse(response, database)).single();
    }

    /**
     * Deletes the item container
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response for the
     * deleted container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos container response for
     *         the deleted container or an error.
     */
    public Mono<CosmosContainerResponse> delete() {
        return delete(new CosmosContainerRequestOptions());
    }

    /**
     * Replaces a document container.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the replaced document container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerSettings the item container properties
     * @return an {@link Mono} containing the single cosmos container response with
     *         the replaced document container or an error.
     */
    public Mono<CosmosContainerResponse> replace(CosmosContainerProperties containerSettings) {
        return replace(containerSettings, null);
    }

    /**
     * Replaces a document container.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos container response with
     * the replaced document container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerSettings the item container properties
     * @param options           the cosmos container request options.
     * @return an {@link Mono} containing the single cosmos container response with
     *         the replaced document container or an error.
     */
    public Mono<CosmosContainerResponse> replace(CosmosContainerProperties containerSettings,
            CosmosContainerRequestOptions options) {
        validateResource(containerSettings);
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return database.getDocClientWrapper()
                .replaceCollection(containerSettings.getV2Collection(), options.toRequestOptions())
                .map(response -> new CosmosContainerResponse(response, database)).single();
    }

    /* CosmosItem operations */

    /**
     * Creates a cosmos item.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param item the cosmos item represented as a POJO or cosmos item object.
     * @return an {@link Mono} containing the single resource response with the
     *         created cosmos item or an error.
     */
    public Mono<CosmosItemResponse> createItem(Object item) {
        return createItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Creates a cosmos item.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param item    the cosmos item represented as a POJO or cosmos item object.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the
     *         created cosmos item or an error.
     */
    public Mono<CosmosItemResponse> createItem(Object item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return database.getDocClientWrapper()
                .createDocument(getLink(), CosmosItemProperties.fromObject(item), requestOptions, true)
                .map(response -> new CosmosItemResponse(response, requestOptions.getPartitionKey(), this)).single();
    }

    /**
     * Upserts an item.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param item the item represented as a POJO or Item object to upsert.
     * @return an {@link Mono} containing the single resource response with the
     *         upserted document or an error.
     */
    public Mono<CosmosItemResponse> upsertItem(Object item) {
        return upsertItem(item, null);
    }

    /**
     * Upserts a cosmos item.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param item    the item represented as a POJO or Item object to upsert.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the
     *         upserted document or an error.
     */
    public Mono<CosmosItemResponse> upsertItem(Object item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();

        return this.getDatabase().getDocClientWrapper()
                .upsertDocument(this.getLink(), CosmosItemProperties.fromObject(item), options.toRequestOptions(), true)
                .map(response -> new CosmosItemResponse(response, requestOptions.getPartitionKey(), this)).single();
    }

    /**
     * Reads all cosmos items in the container.
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the read cosmos items. In case of
     * failure the {@link Flux} will error.
     *
     * @return an {@link Flux} containing one or several feed response pages of the
     *         read cosmos items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> readAllItems() {
        return readAllItems(new FeedOptions());
    }

    /**
     * Reads all cosmos items in a container.
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the read cosmos items. In case of
     * failure the {@link Flux} will error.
     *
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the
     *         read cosmos items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> readAllItems(FeedOptions options) {
        return getDatabase().getDocClientWrapper().readDocuments(getLink(), options).map(
                response -> BridgeInternal.createFeedResponse(CosmosItemProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Query for documents in a items in a container
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link Flux} will error.
     *
     * @param query   the query.
     * @return an {@link Flux} containing one or several feed response pages of the
     *         obtained items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> queryItems(String query) {
        return queryItems(new SqlQuerySpec(query), null);
    }

    /**
     * Query for documents in a items in a container
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link Flux} will error.
     *
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the
     *         obtained items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> queryItems(String query, FeedOptions options) {
        return queryItems(new SqlQuerySpec(query), options);
    }

    /**
     * Query for documents in a items in a container
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link Flux} will error.
     *
     * @param querySpec the SQL query specification.
     * @return an {@link Flux} containing one or several feed response pages of the
     *         obtained items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> queryItems(SqlQuerySpec querySpec) {
        return queryItems(querySpec, null);
    }

    /**
     * Query for documents in a items in a container
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link Flux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options   the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the
     *         obtained items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> queryItems(SqlQuerySpec querySpec, FeedOptions options) {
        return getDatabase().getDocClientWrapper().queryDocuments(getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponseWithQueryMetrics(
                        CosmosItemProperties.getFromV2Results(response.results()), response.responseHeaders(),
                        response.queryMetrics()));
    }

    /**
     * Query for documents in a items in a container
     *
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link Flux} will error.
     *
     * @param changeFeedOptions the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the
     *         obtained items or an error.
     */
    public Flux<FeedResponse<CosmosItemProperties>> queryChangeFeedItems(ChangeFeedOptions changeFeedOptions) {
        return getDatabase().getDocClientWrapper().queryDocumentChangeFeed(getLink(), changeFeedOptions)
                .map(response -> new FeedResponse<CosmosItemProperties>(
                        CosmosItemProperties.getFromV2Results(response.results()), response.responseHeaders(), false));
    }

    /**
     * Gets a CosmosItem object without making a service call
     *
     * @param id           id of the item
     * @param partitionKey the partition key
     * @return a cosmos item
     */
    public CosmosItem getItem(String id, Object partitionKey) {
        return new CosmosItem(id, partitionKey, this);
    }

    public CosmosScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosScripts(this);
        }
        return this.scripts;
    }

    /**
     * Lists all the conflicts in the container
     *
     * @param options the feed options
     * @return a {@link Flux} containing one or several feed response pages of the
     *         obtained conflicts or an error.
     */
    public Flux<FeedResponse<CosmosConflictProperties>> readAllConflicts(FeedOptions options) {
        return database.getDocClientWrapper().readConflicts(getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(
                        CosmosConflictProperties.getFromV2Results(response.results()), response.responseHeaders()));
    }

    /**
     * Queries all the conflicts in the container
     *
     * @param query   the query
     * @return a {@link Flux} containing one or several feed response pages of the
     *         obtained conflicts or an error.
     */
    public Flux<FeedResponse<CosmosConflictProperties>> queryConflicts(String query) {
        return queryConflicts(query, null);
    }

    /**
     * Queries all the conflicts in the container
     *
     * @param query   the query
     * @param options the feed options
     * @return a {@link Flux} containing one or several feed response pages of the
     *         obtained conflicts or an error.
     */
    public Flux<FeedResponse<CosmosConflictProperties>> queryConflicts(String query, FeedOptions options) {
        return database.getDocClientWrapper().queryConflicts(getLink(), query, options)
                .map(response -> BridgeInternal.createFeedResponse(
                        CosmosConflictProperties.getFromV2Results(response.results()), response.responseHeaders()));
    }

    /**
     * Gets a CosmosConflict object without making a service call
     * 
     * @param id id of the cosmos conflict
     * @return a cosmos conflict
     */
    public CosmosConflict getConflict(String id) {
        return new CosmosConflict(id, this);
    }

    /**
     * Gets the throughput of the container
     *
     * @return a {@link Mono} containing throughput or an error.
     */
    public Mono<Integer> readProvisionedThroughput() {
        return this.read().flatMap(cosmosContainerResponse -> database.getDocClientWrapper()
                .queryOffers("select * from c where c.offerResourceId = '"
                        + cosmosContainerResponse.resourceSettings().resourceId() + "'", new FeedOptions())
                .single()).flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.results().isEmpty()) {
                        return Mono.error(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
                                "No offers found for the resource"));
                    }
                    return database.getDocClientWrapper().readOffer(offerFeedResponse.results().get(0).selfLink())
                            .single();
                }).map(cosmosOfferResponse -> cosmosOfferResponse.getResource().getThroughput());
    }

    /**
     * Sets throughput provisioned for a container in measurement of
     * Requests-per-Unit in the Azure Cosmos service.
     *
     * @param requestUnitsPerSecond the cosmos container throughput, expressed in
     *                              Request Units per second
     * @return a {@link Mono} containing throughput or an error.
     */
    public Mono<Integer> replaceProvisionedThroughput(int requestUnitsPerSecond) {
        return this.read().flatMap(cosmosContainerResponse -> database.getDocClientWrapper()
                .queryOffers("select * from c where c.offerResourceId = '"
                        + cosmosContainerResponse.resourceSettings().resourceId() + "'", new FeedOptions())
                .single()).flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.results().isEmpty()) {
                        return Mono.error(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
                                "No offers found for the resource"));
                    }
                    Offer offer = offerFeedResponse.results().get(0);
                    offer.setThroughput(requestUnitsPerSecond);
                    return database.getDocClientWrapper().replaceOffer(offer).single();
                }).map(offerResourceResponse -> offerResourceResponse.getResource().getThroughput());
    }

    /**
     * Gets the parent Database
     *
     * @return the {@link CosmosDatabase}
     */
    public CosmosDatabase getDatabase() {
        return database;
    }

    String URIPathSegment() {
        return Paths.COLLECTIONS_PATH_SEGMENT;
    }

    String parentLink() {
        return database.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(parentLink());
        builder.append("/");
        builder.append(URIPathSegment());
        builder.append("/");
        builder.append(id());
        return builder.toString();
    }

}
