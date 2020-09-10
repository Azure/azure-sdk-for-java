// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static com.azure.core.util.FluxUtil.withContext;
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
     * Get the id of the CosmosAsyncDatabase.
     *
     * @return the id of the CosmosAsyncDatabase.
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
    public Mono<CosmosDatabaseResponse> read() {
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
    public Mono<CosmosDatabaseResponse> read(CosmosDatabaseRequestOptions options) {
        final CosmosDatabaseRequestOptions requestOptions = options == null ? new CosmosDatabaseRequestOptions() : options;
        return withContext(context -> readInternal(requestOptions, context));
    }

    /**
     * Deletes a database.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos database response with the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos database response.
     */
    public Mono<CosmosDatabaseResponse> delete() {
        return delete(new CosmosDatabaseRequestOptions());
    }

    /**
     * Deletes a database.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos database response with the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single cosmos database response.
     */
    public Mono<CosmosDatabaseResponse> delete(CosmosDatabaseRequestOptions options) {
        final CosmosDatabaseRequestOptions requestOptions = options == null ? new CosmosDatabaseRequestOptions() : options;
        return withContext(context -> deleteInternal(requestOptions, context));
    }

    /* CosmosAsyncContainer operations */

    /**
     * Creates a Cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the container properties.
     * @return a {@link Mono} containing the single cosmos container response with
     * the created container or an error.
     * @throws IllegalArgumentException containerProperties cannot be null.
     */
    public Mono<CosmosContainerResponse> createContainer(CosmosContainerProperties containerProperties) {
        return createContainer(containerProperties, new CosmosContainerRequestOptions());
    }

    /**
     * Creates a Cosmos container with custom throughput properties.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the container properties.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the single cosmos container response with
     * the created container or an error.
     * @throws IllegalArgumentException thown if containerProerties are null.
     */
    public Mono<CosmosContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties) {
        if (containerProperties == null) {
            throw new IllegalArgumentException("containerProperties");
        }
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        return createContainer(containerProperties, options);
    }

    /**
     * Creates a container.
     *
     * @param containerProperties the container properties.
     * @param throughputProperties the throughput properties.
     * @param options the request options.
     * @return the mono.
     */
    public Mono<CosmosContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties,
        CosmosContainerRequestOptions options){
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        return createContainer(containerProperties, options);
    }

    /**
     * Creates a Cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the containerProperties.
     * @param options the cosmos container request options.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     * @throws IllegalArgumentException containerProperties can not be null.
     */
    public Mono<CosmosContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        if (containerProperties == null) {
            throw new IllegalArgumentException("containerProperties");
        }

        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> createContainerInternal(containerProperties, requestOptions, context));
    }

    /**
     * Creates a Cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the containerProperties.
     * @param throughput the throughput for the container.
     * @param options the cosmos container request options.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     * @throws IllegalArgumentException containerProperties cannot be null.
     */
    Mono<CosmosContainerResponse> createContainer(
        CosmosContainerProperties containerProperties,
        int throughput,
        CosmosContainerRequestOptions options) {
        if (options == null) {
            options = new CosmosContainerRequestOptions();
        }
        ModelBridgeInternal.setThroughputProperties(options, ThroughputProperties.createManualThroughput(throughput));
        return createContainer(containerProperties, options);
    }

    /**
     * Creates a Cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainer(String id, String partitionKeyPath) {
        return createContainer(new CosmosContainerProperties(id, partitionKeyPath));
    }

    /**
     * Creates a Cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainer(String id, String partitionKeyPath, ThroughputProperties throughputProperties) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        return createContainer(new CosmosContainerProperties(id, partitionKeyPath), options);
    }

    /**
     * Creates a Cosmos container if it does not exist on the service.
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
    public Mono<CosmosContainerResponse> createContainerIfNotExists(
        CosmosContainerProperties containerProperties) {
        CosmosAsyncContainer container = getContainer(containerProperties.getId());
        return withContext(context -> createContainerIfNotExistsInternal(containerProperties, container, null,
            context));
    }

    /**
     * Creates a Cosmos container if it does not exist on the service.
     * <p>
     * The throughput setting will only be used if the specified container
     * does not exist and therefore a new container will be created.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created or existing container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties.
     * @param throughput the throughput for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created or existing container or an error.
     */
    Mono<CosmosContainerResponse> createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        int throughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, ThroughputProperties.createManualThroughput(throughput));
        CosmosAsyncContainer container = getContainer(containerProperties.getId());
        return withContext(context -> createContainerIfNotExistsInternal(containerProperties, container, options,
            context));
    }

    /**
     * Creates a Cosmos container if it does not exist on the service.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created or existing container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created or existing container or an error.
     */
    public Mono<CosmosContainerResponse> createContainerIfNotExists(
        CosmosContainerProperties containerProperties,
        ThroughputProperties throughputProperties) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        CosmosAsyncContainer container = getContainer(containerProperties.getId());
        return withContext(context -> createContainerIfNotExistsInternal(containerProperties, container, options,
            context));
    }

    /**
     * Creates a Cosmos container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainerIfNotExists(String id, String partitionKeyPath) {
        CosmosAsyncContainer container = getContainer(id);
        return withContext(context -> createContainerIfNotExistsInternal(new CosmosContainerProperties(id,
                partitionKeyPath), container, null,
            context));
    }

    /**
     * Creates a Cosmos container if it does not exist on the service.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainerIfNotExists(
        String id, String partitionKeyPath,
        ThroughputProperties throughputProperties) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, throughputProperties);
        CosmosAsyncContainer container = getContainer(id);
        return withContext(context -> createContainerIfNotExistsInternal(new CosmosContainerProperties(id,
            partitionKeyPath), container, options, context));
    }

    /**
     * Creates a Cosmos container if it does not exist on the service.
     * <p>
     * The throughput setting will only be used if the specified container
     * does not exist and a new container will be created.
     *
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @param throughput the throughput for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    Mono<CosmosContainerResponse> createContainerIfNotExists(
        String id, String partitionKeyPath,
        int throughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        ModelBridgeInternal.setThroughputProperties(options, ThroughputProperties.createManualThroughput(throughput));
        CosmosAsyncContainer container = getContainer(id);
        return withContext(context -> createContainerIfNotExistsInternal(new CosmosContainerProperties(id,
            partitionKeyPath), container, options, context));
    }

    /**
     * Reads all cosmos containers.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param options {@link CosmosQueryRequestOptions}
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of read
     * containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> readAllContainers(CosmosQueryRequestOptions options) {
        CosmosQueryRequestOptions requestOptions = options == null ? new CosmosQueryRequestOptions() : options;
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllContainers." + this.getId();
            pagedFluxOptions.setTracerInformation(this.getClient().getTracerProvider(), spanName,
                this.getClient().getServiceEndpoint(), getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, requestOptions);
            return getDocClientWrapper().readCollections(getLink(), requestOptions)
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
        return readAllContainers(new CosmosQueryRequestOptions());
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(String query) {
        return queryContainersInternal(new SqlQuerySpec(query), new CosmosQueryRequestOptions());
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(String query, CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryContainersInternal(new SqlQuerySpec(query), options);
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
        return queryContainersInternal(querySpec, new CosmosQueryRequestOptions());
    }

    /**
     * Query for cosmos containers in a cosmos database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained containers. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained containers or an error.
     */
    public CosmosPagedFlux<CosmosContainerProperties> queryContainers(SqlQuerySpec querySpec
        , CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryContainersInternal(querySpec, options);
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
    public Mono<CosmosUserResponse> createUser(CosmosUserProperties userProperties) {
        return withContext(context -> createUserInternal(userProperties, context));
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
    public Mono<CosmosUserResponse> upsertUser(CosmosUserProperties userProperties) {
        return withContext(context -> upsertUserInternal(userProperties, context));
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
        return readAllUsers(new CosmosQueryRequestOptions());
    }

    /**
     * Reads all cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read cosmos users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * read cosmos users or an error.
     */
    CosmosPagedFlux<CosmosUserProperties> readAllUsers(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllUsers." + this.getId();
            pagedFluxOptions.setTracerInformation(this.getClient().getTracerProvider(), spanName,
                this.getClient().getServiceEndpoint(), getId());
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
     * @param query query as string.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(String query) {
        return queryUsers(query, new CosmosQueryRequestOptions());
    }

    /**
     * Query for cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param query query as string.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(String query, CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryUsersInternal(new SqlQuerySpec(query), options);
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
        return queryUsersInternal(querySpec, new CosmosQueryRequestOptions());
    }

    /**
     * Query for cosmos users in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained users. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained users or an error.
     */
    public CosmosPagedFlux<CosmosUserProperties> queryUsers(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryUsersInternal(querySpec, options);
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
     * Sets throughput provisioned for a container in measurement of
     * Requests-per-Unit in the Azure Cosmos service.
     *
     * @param throughputProperties the throughput properties.
     * @return the mono.
     */
    public Mono<ThroughputResponse> replaceThroughput(ThroughputProperties throughputProperties) {
       return withContext(context -> replaceThroughputInternal(throughputProperties, context));
    }

    /**
     * Gets the throughput of the database.
     *
     * @return the mono containing throughput response.
     */
    public Mono<ThroughputResponse> readThroughput() {
        return withContext(context -> readThroughputInternal(context));
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

    private CosmosPagedFlux<CosmosContainerProperties> queryContainersInternal(SqlQuerySpec querySpec
        , CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "queryContainers." + this.getId();
            pagedFluxOptions.setTracerInformation(this.getClient().getTracerProvider(), spanName,
                this.getClient().getServiceEndpoint(), getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().queryCollections(getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosContainerPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    private CosmosPagedFlux<CosmosUserProperties> queryUsersInternal(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "queryUsers." + this.getId();
            pagedFluxOptions.setTracerInformation(this.getClient().getTracerProvider(), spanName,
                this.getClient().getServiceEndpoint(), getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDocClientWrapper().queryUsers(getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponseWithQueryMetrics(
                    ModelBridgeInternal.getCosmosUserPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders(),
                    ModelBridgeInternal.queryMetrics(response),
                    ModelBridgeInternal.getQueryPlanDiagnosticsContext(response)));
        });
    }

    private Mono<CosmosContainerResponse> createContainerIfNotExistsInternal(
        CosmosContainerProperties containerProperties,
        CosmosAsyncContainer container,
        CosmosContainerRequestOptions options,
        Context context) {
        String spanName = "createContainerIfNotExists." + containerProperties.getId();
        Context nestedContext = context.addData(TracerProvider.COSMOS_CALL_DEPTH, TracerProvider.COSMOS_CALL_DEPTH_VAL);
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() :
            options;
        Mono<CosmosContainerResponse> responseMono =
            container.read(requestOptions, nestedContext).onErrorResume(exception -> {
            final Throwable unwrappedException = Exceptions.unwrap(exception);
            if (unwrappedException instanceof CosmosException) {
                final CosmosException cosmosException = (CosmosException) unwrappedException;
                if (cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    return createContainerInternal(containerProperties, requestOptions, nestedContext);
                }
            }
            return Mono.error(unwrappedException);
        });
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    private Mono<CosmosContainerResponse> createContainerInternal(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options,
        Context context) {
        String spanName = "createContainer." + containerProperties.getId();
        Mono<CosmosContainerResponse> responseMono = getDocClientWrapper()
            .createCollection(this.getLink(), ModelBridgeInternal.getV2Collection(containerProperties),
                ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    Mono<CosmosDatabaseResponse> readInternal(CosmosDatabaseRequestOptions options, Context context) {
        String spanName = "readDatabase." + this.getId();
        Mono<CosmosDatabaseResponse> responseMono = getDocClientWrapper().readDatabase(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosDatabaseResponse(response)).single();
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    private Mono<CosmosDatabaseResponse> deleteInternal(CosmosDatabaseRequestOptions options, Context context) {
        String spanName = "deleteDatabase." + this.getId();
        Mono<CosmosDatabaseResponse> responseMono = getDocClientWrapper().deleteDatabase(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosDatabaseResponse(response)).single();
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    private Mono<CosmosUserResponse> createUserInternal(CosmosUserProperties userProperties, Context context) {
        String spanName = "createUser." + this.getId();
        Mono<CosmosUserResponse> responseMono = getDocClientWrapper().createUser(this.getLink(), ModelBridgeInternal.getV2User(userProperties), null)
            .map(response -> ModelBridgeInternal.createCosmosUserResponse(response)).single();
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    private Mono<CosmosUserResponse> upsertUserInternal(CosmosUserProperties userProperties, Context context) {
        String spanName = "upsertUser." + this.getId();
        Mono<CosmosUserResponse> responseMono = getDocClientWrapper().upsertUser(this.getLink(), ModelBridgeInternal.getV2User(userProperties), null)
            .map(response -> ModelBridgeInternal.createCosmosUserResponse(response)).single();
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName, getId(), getClient().getServiceEndpoint());
    }

    private Mono<ThroughputResponse> replaceThroughputInternal(ThroughputProperties throughputProperties, Context context){
        String spanName = "replaceThroughput." + this.getId();
        Context nestedContext = context.addData(TracerProvider.COSMOS_CALL_DEPTH, TracerProvider.COSMOS_CALL_DEPTH_VAL);
        Mono<ThroughputResponse> responseMono = replaceThroughputInternal(this.readInternal(new CosmosDatabaseRequestOptions(), nestedContext), throughputProperties);
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    private Mono<ThroughputResponse> replaceThroughputInternal(Mono<CosmosDatabaseResponse> responseMono, ThroughputProperties throughputProperties) {
        return responseMono
            .flatMap(response -> this.getDocClientWrapper()
                .queryOffers(getOfferQuerySpecFromResourceId(response.getProperties().getResourceId()),
                    new CosmosQueryRequestOptions())
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

                    return this.getDocClientWrapper()
                        .replaceOffer(updatedOffer)
                        .single();
                })
                .map(ModelBridgeInternal::createThroughputRespose));
    }

    private Mono<ThroughputResponse> readThroughputInternal(Context context){
        String spanName = "readThroughput." + this.getId();
        Context nestedContext = context.addData(TracerProvider.COSMOS_CALL_DEPTH, TracerProvider.COSMOS_CALL_DEPTH_VAL);
        Mono<ThroughputResponse> responseMono = readThroughputInternal(this.readInternal(new CosmosDatabaseRequestOptions(), nestedContext));
        return this.client.getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            getId(),
            getClient().getServiceEndpoint());
    }

    private Mono<ThroughputResponse> readThroughputInternal(Mono<CosmosDatabaseResponse> responseMono) {
        return responseMono
            .flatMap(response -> getDocClientWrapper()
                .queryOffers(getOfferQuerySpecFromResourceId(response.getProperties().getResourceId()),
                    new CosmosQueryRequestOptions())
                .single()
                .flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.getResults().isEmpty()) {
                        return Mono.error(BridgeInternal
                            .createCosmosException(
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
}
