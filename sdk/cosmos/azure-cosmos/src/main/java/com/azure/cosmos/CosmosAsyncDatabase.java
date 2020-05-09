// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.models.CosmosAsyncContainerResponse;
import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.models.CosmosAsyncUserResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

/**
 * Perform read and delete databases, update database throughput, and perform operations on child resources
 */
public class CosmosAsyncDatabase {
    private final CosmosAsyncClient client;
    private final String id;
    private final String link;

    CosmosAsyncDatabase(String id, CosmosAsyncClient client) {
        this.id = id;
        this.client = client;
        this.link = getParentLink() + "/" + getURIPathSegment() + "/" + getId();
    }

    /**
     * Get the id of the CosmosAsyncDatabase
     *
     * @return the id of the CosmosAsyncDatabase
     */
    public String getId() {
        return id;
    }

    /**
     * Reads a database.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single cosmos database respone with the
     * read database. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos database respone with
     * the read database or an error.
     */
    public Mono<CosmosAsyncDatabaseResponse> read() {
        return read(new CosmosDatabaseRequestOptions());
    }

    /**
     * Reads a database.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos cosmos database respone with the
     * read database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single cosmos database response with
     * the read database or an error.
     */
    public Mono<CosmosAsyncDatabaseResponse> read(CosmosDatabaseRequestOptions options) {
        if (options == null) {
            options = new CosmosDatabaseRequestOptions();
        }
        return getDocClientWrapper().readDatabase(getLink(), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncDatabaseResponse(response, getClient())).single();
    }

    /**
     * Deletes a database.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos database response with the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos database response
     */
    public Mono<CosmosAsyncDatabaseResponse> delete() {
        return delete(new CosmosDatabaseRequestOptions());
    }

    /**
     * Deletes a database.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos database response with the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options
     * @return an {@link Mono} containing the single cosmos database response
     */
    public Mono<CosmosAsyncDatabaseResponse> delete(CosmosDatabaseRequestOptions options) {
        if (options == null) {
            options = new CosmosDatabaseRequestOptions();
        }
        return getDocClientWrapper().deleteDatabase(getLink(), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncDatabaseResponse(response, getClient())).single();
    }

    /* CosmosAsyncContainer operations */

    /**
     * Creates a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the container properties.
     * @return a {@link Mono} containing the single cosmos container response with
     * the created container or an error.
     * @throws IllegalArgumentException containerProperties cannot be null
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(CosmosContainerProperties containerProperties) {
        return createContainer(containerProperties, new CosmosContainerRequestOptions());
    }

    /**
     * Creates a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the container properties.
     * @param throughput the throughput for the container
     * @return a {@link Mono} containing the single cosmos container response with
     * the created container or an error.
     * @throws IllegalArgumentException thown if containerProerties are null
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        int throughput) {
        if (containerProperties == null) {
            throw new IllegalArgumentException("containerProperties");
        }
        ModelBridgeInternal.validateResource(ModelBridgeInternal.getResourceFromResourceWrapper(containerProperties));
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setOfferThroughput(options, throughput);
        return createContainer(containerProperties, options);
    }

    /**
     * Creates a container.
     *
     * @param containerProperties the container properties
     * @param throughputProperties the throughput properties
     * @param options the request options
     * @return the mono
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties,
        CosmosContainerRequestOptions options){
        ModelBridgeInternal.setOfferProperties(options, throughputProperties);
        return createContainer(containerProperties, options);
    }

    /**
     * Creates a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the containerProperties.
     * @param options the cosmos container request options
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     * @throws IllegalArgumentException containerProperties can not be null
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        if (containerProperties == null) {
            throw new IllegalArgumentException("containerProperties");
        }
        ModelBridgeInternal.validateResource(ModelBridgeInternal.getResourceFromResourceWrapper(containerProperties));
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        return getDocClientWrapper()
                   .createCollection(this.getLink(), ModelBridgeInternal.getV2Collection(containerProperties),
                       ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncContainerResponse(response, this)).single();
    }

    /**
     * Creates a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the containerProperties.
     * @param throughput the throughput for the container
     * @param options the cosmos container request options
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     * @throws IllegalArgumentException containerProperties cannot be null
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        int throughput,
        CosmosContainerRequestOptions options) {
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        ModelBridgeInternal.setOfferThroughput(options, throughput);
        return createContainer(containerProperties, options);
    }

    /**
     * Creates a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id
     * @param partitionKeyPath the partition key path
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(String id, String partitionKeyPath) {
        return createContainer(new CosmosContainerProperties(id, partitionKeyPath));
    }

    /**
     * Creates a document container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id
     * @param partitionKeyPath the partition key path
     * @param throughput the throughput for the container
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> createContainer(String id, String partitionKeyPath, int throughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setOfferThroughput(options, throughput);
        return createContainer(new CosmosContainerProperties(id, partitionKeyPath), options);
    }

    /**
     * Creates a document container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created or existing container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties
     * @return a {@link Mono} containing the cosmos container response with the
     * created or existing container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> createContainerIfNotExists(
        CosmosContainerProperties containerProperties) {
        CosmosAsyncContainer container = getContainer(containerProperties.getId());
        return createContainerIfNotExistsInternal(containerProperties, container, null);
    }

    /**
     * Creates a document container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created or existing container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties
     * @param throughput the throughput for the container
     * @return a {@link Mono} containing the cosmos container response with the
     * created or existing container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        int throughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setOfferThroughput(options, throughput);
        CosmosAsyncContainer container = getContainer(containerProperties.getId());
        return createContainerIfNotExistsInternal(containerProperties, container, options);
    }

    /**
     * Creates a document container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id
     * @param partitionKeyPath the partition key path
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> createContainerIfNotExists(String id, String partitionKeyPath) {
        CosmosAsyncContainer container = getContainer(id);
        return createContainerIfNotExistsInternal(new CosmosContainerProperties(id, partitionKeyPath),
                                                  container,
                                                  null);
    }

    /**
     * Creates a document container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id
     * @param partitionKeyPath the partition key path
     * @param throughput the throughput for the container
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosAsyncContainerResponse> createContainerIfNotExists(
        String id, String partitionKeyPath,
        int throughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setOfferThroughput(options, throughput);
        CosmosAsyncContainer container = getContainer(id);
        return createContainerIfNotExistsInternal(new CosmosContainerProperties(id, partitionKeyPath), container,
                                                  options);
    }

    private Mono<CosmosAsyncContainerResponse> createContainerIfNotExistsInternal(
        CosmosContainerProperties containerProperties, CosmosAsyncContainer container,
        CosmosContainerRequestOptions options) {
        return container.read(options).onErrorResume(exception -> {
            final Throwable unwrappedException = Exceptions.unwrap(exception);
            if (unwrappedException instanceof CosmosClientException) {
                final CosmosClientException cosmosClientException = (CosmosClientException) unwrappedException;
                if (cosmosClientException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    return createContainer(containerProperties, options);
                }
            }
            return Mono.error(unwrappedException);
        });
    }

    /**
     * Reads all cosmos containers.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param options {@link FeedOptions}
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read
     * containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> readAllContainers(FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().readCollections(getLink(), options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosContainerPropertiesFromV2Results(response.getResults()),
                           response.getResponseHeaders()));
        });
    }

    /**
     * Reads all cosmos containers.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read
     * containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> readAllContainers() {
        return readAllContainers(new FeedOptions());
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(String query) {
        return queryContainers(new SqlQuerySpec(query));
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(String query, FeedOptions options) {
        return queryContainers(new SqlQuerySpec(query), options);
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(SqlQuerySpec querySpec) {
        return queryContainers(querySpec, new FeedOptions());
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(SqlQuerySpec querySpec, FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().queryCollections(getLink(), querySpec, options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosContainerPropertiesFromV2Results(response.getResults()),
                           response.getResponseHeaders()));
        });
    }

    /**
     * Gets a CosmosAsyncContainer object without making a service call
     *
     * @param id id of the container
     * @return Cosmos Container
     */
    public CosmosAsyncContainer getContainer(String id) {
        return new CosmosAsyncContainer(id, this);
    }

    /** User operations **/

    /**
     * Creates a user After subscription the operation will be performed. The
     * {@link Mono} upon successful completion will contain a single resource
     * response with the created user. In case of failure the {@link Mono} will
     * error.
     *
     * @param userProperties the cosmos user properties
     * @return an {@link Mono} containing the single resource response with the
     * created cosmos user or an error.
     */
    public Mono<CosmosAsyncUserResponse> createUser(CosmosUserProperties userProperties) {
        return getDocClientWrapper().createUser(this.getLink(), ModelBridgeInternal.getV2User(userProperties), null)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncUserResponse(response, this)).single();
    }


    /**
     * Upsert a user. Upsert will create a new user if it doesn't exist, or replace
     * the existing one if it does. After subscription the operation will be
     * performed. The {@link Mono} upon successful completion will contain a single
     * resource response with the created user. In case of failure the {@link Mono}
     * will error.
     *
     * @param userProperties the cosmos user properties
     * @return an {@link Mono} containing the single resource response with the
     * upserted user or an error.
     */
    public Mono<CosmosAsyncUserResponse> upsertUser(CosmosUserProperties userProperties) {
        return getDocClientWrapper().upsertUser(this.getLink(), ModelBridgeInternal.getV2User(userProperties), null)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncUserResponse(response, this)).single();
    }

    /**
     * Reads all cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read cosmos users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * read cosmos users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> readAllUsers() {
        return readAllUsers(new FeedOptions());
    }

    /**
     * Reads all cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read cosmos users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * read cosmos users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> readAllUsers(FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().readUsers(getLink(), options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosUserPropertiesFromV2Results(response.getResults()), response
                                                                                             .getResponseHeaders()));
        });
    }

    /**
     * Query for cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query query as string
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(String query) {
        return queryUsers(query, new FeedOptions());
    }

    /**
     * Query for cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query query as string
     * @param options the feed options
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(String query, FeedOptions options) {
        return queryUsers(new SqlQuerySpec(query), options);
    }

    /**
     * Query for cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec) {
        return queryUsers(querySpec, new FeedOptions());
    }

    /**
     * Query for cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec, FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().queryUsers(getLink(), querySpec, options)
                       .map(response -> BridgeInternal.createFeedResponseWithQueryMetrics(
                           ModelBridgeInternal.getCosmosUserPropertiesFromV2Results(response.getResults()), response.getResponseHeaders(),
                           ModelBridgeInternal.queryMetrics(response)));
        });
    }

    /**
     * Gets user.
     *
     * @param id the id
     * @return the user
     */
    public CosmosAsyncUser getUser(String id) {
        return new CosmosAsyncUser(id, this);
    }

    /**
     * Gets the throughput of the database
     *
     * @return a {@link Mono} containing throughput or an error.
     */
    public Mono<Integer> readProvisionedThroughput() {
        return this.read()
                   .flatMap(cosmosDatabaseResponse -> getDocClientWrapper()
                                                          .queryOffers("select * from c where c.offerResourceId = '"
                                                                           + cosmosDatabaseResponse.getProperties()
                                                                                 .getResourceId() + "'",
                                                                       new FeedOptions())
                                                          .single()
                                                          .flatMap(offerFeedResponse -> {
                                                              if (offerFeedResponse.getResults().isEmpty()) {
                                                                  return Mono.error(BridgeInternal
                                                                            .createCosmosClientException(
                                                                                HttpConstants.StatusCodes.BADREQUEST,
                                                                                "No offers found for the resource"));
                                                              }
                                                              return getDocClientWrapper()
                                                                         .readOffer(offerFeedResponse.getResults()
                                                                                        .get(0)
                                                                                        .getSelfLink())
                                                                         .single();
                                                          }).map(cosmosContainerResponse1 -> cosmosContainerResponse1
                                                                                                 .getResource()
                                                                                                 .getThroughput()));
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
                   .flatMap(cosmosDatabaseResponse -> this.getDocClientWrapper()
                                                          .queryOffers("select * from c where c.offerResourceId = '"
                                                                           + cosmosDatabaseResponse.getProperties()
                                                                                 .getResourceId()
                                                                           + "'", new FeedOptions())
                                                          .single()
                                                          .flatMap(offerFeedResponse -> {
                                                              if (offerFeedResponse.getResults().isEmpty()) {
                                                                  return Mono.error(BridgeInternal
                                                                            .createCosmosClientException(
                                                                                HttpConstants.StatusCodes.BADREQUEST,
                                                                                "No offers found for the resource"));
                                                              }
                                                              Offer offer = offerFeedResponse.getResults().get(0);
                                                              offer.setThroughput(requestUnitsPerSecond);
                                                              return this.getDocClientWrapper().replaceOffer(offer)
                                                                         .single();
                                                          }).map(offerResourceResponse -> offerResourceResponse
                                                                                              .getResource()
                                                                                              .getThroughput()));
    }

    /**
     * Sets throughput provisioned for a container in measurement of
     * Requests-per-Unit in the Azure Cosmos service.
     *
     * @param throughputProperties the throughput properties
     * @return the mono
     */
    public Mono<ThroughputResponse> replaceThroughput(ThroughputProperties throughputProperties) {
        return this.read()
                   .flatMap(response -> this.getDocClientWrapper()
                                            .queryOffers(getOfferQuerySpecFromResourceId(response.getProperties()
                                                                                             .getResourceId()),
                                                         new FeedOptions())
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

                                                return this.getDocClientWrapper()
                                                           .replaceOffer(updatedOffer)
                                                           .single();
                                            })
                                            .map(ModelBridgeInternal::createThroughputRespose));
    }

    /**
     * Gets the throughput of the database
     *
     * @return the mono containing throughput response
     */
    public Mono<ThroughputResponse> readThroughput() {
        return this.read()
                   .flatMap(response -> getDocClientWrapper()
                                            .queryOffers(getOfferQuerySpecFromResourceId(response.getProperties()
                                                                                            .getResourceId()),
                                                         new FeedOptions())
                                            .single()
                                            .flatMap(offerFeedResponse -> {
                                                if (offerFeedResponse.getResults().isEmpty()) {
                                                    return Mono.error(BridgeInternal
                                                                          .createCosmosClientException(
                                                                              HttpConstants.StatusCodes.BADREQUEST,
                                                                              "No offers found for the " +
                                                                                  "resource " + this.getId()));
                                                }
                                                return getDocClientWrapper()
                                                           .readOffer(offerFeedResponse.getResults()
                                                                          .get(0)
                                                                          .getSelfLink())
                                                           .single();
                                            })
                                            .map(ModelBridgeInternal::createThroughputRespose));
    }

    SqlQuerySpec getOfferQuerySpecFromResourceId(String resourceId) {
        String queryText = "select * from c where c.offerResourceId = @resourceId";
        SqlQuerySpec querySpec = new SqlQuerySpec(queryText);
        List<SqlParameter> parameters = Collections
                                            .singletonList(new SqlParameter("@resourceId", resourceId));
        querySpec.setParameters(parameters);
        return querySpec;
    }

    CosmosAsyncClient getClient() {
        return client;
    }

    AsyncDocumentClient getDocClientWrapper() {
        return client.getDocClientWrapper();
    }

    String getURIPathSegment() {
        return Paths.DATABASES_PATH_SEGMENT;
    }

    String getParentLink() {
        return StringUtils.EMPTY;
    }

    String getLink() {
        return this.link;
    }

}
