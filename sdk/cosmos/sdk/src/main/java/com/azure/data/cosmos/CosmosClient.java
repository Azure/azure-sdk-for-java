// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.DatabaseAccount;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Provides a client-side logical representation of the Azure Cosmos database service.
 * This asynchronous client is used to configure and execute requests
 * against the service.
 */
public class CosmosClient implements AutoCloseable {

    //Document client wrapper
    private final Configs configs;
    private final AsyncDocumentClient asyncDocumentClient;
    private final String serviceEndpoint;
    private final String keyOrResourceToken;
    private final ConnectionPolicy connectionPolicy;
    private final ConsistencyLevel desiredConsistencyLevel;
    private final List<Permission> permissions;
    private final TokenResolver tokenResolver;


     CosmosClient(CosmosClientBuilder builder) {
         this.configs = builder.configs();
         this.serviceEndpoint = builder.endpoint();
        this.keyOrResourceToken = builder.key();
        this.connectionPolicy = builder.connectionPolicy();
        this.desiredConsistencyLevel = builder.consistencyLevel();
        this.permissions = builder.permissions();
        this.tokenResolver = builder.tokenResolver();
        this.asyncDocumentClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(this.serviceEndpoint)
                .withMasterKeyOrResourceToken(this.keyOrResourceToken)
                .withConnectionPolicy(this.connectionPolicy)
                .withConsistencyLevel(this.desiredConsistencyLevel)
                .withConfigs(this.configs)
                .withTokenResolver(this.tokenResolver)
                .build();
    }

    AsyncDocumentClient getContextClient() {
        return this.asyncDocumentClient;
    }

    /**
     * Instantiate the cosmos client builder to build cosmos client
     * @return {@link CosmosClientBuilder}
     */
    public static CosmosClientBuilder builder(){
         return new CosmosClientBuilder();
    }

    /**
     * Get the service endpoint
     * @return the service endpoint
     */
    String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Gets the key or resource token
     * @return get the key or resource token
     */
    String getKeyOrResourceToken() {
        return keyOrResourceToken;
    }

    /**
     * Get the connection policy
     * @return {@link ConnectionPolicy}
     */
    ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Gets the consistency level
     * @return the (@link ConsistencyLevel)
     */
    ConsistencyLevel getDesiredConsistencyLevel() {
        return desiredConsistencyLevel;
    }

    /**
     * Gets the permission list
     * @return the permission list
     */
    List<Permission> getPermissions() {
        return permissions;
    }

    AsyncDocumentClient getDocClientWrapper(){
        return asyncDocumentClient;
    }

    /**
     * Gets the configs
     * @return the configs
     */
    Configs getConfigs() {
        return configs;
    }

    /**
     * Gets the token resolver
     * @return the token resolver
     */
    TokenResolver getTokenResolver() {
        return tokenResolver;
    }

    /**
     * CREATE a Database if it does not already exist on the service
     * 
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the 
     * created or existing database.
     * @param databaseSettings CosmosDatabaseProperties
     * @return a {@link Mono} containing the cosmos database response with the created or existing database or
     * an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(CosmosDatabaseProperties databaseSettings) {
        return createDatabaseIfNotExistsInternal(getDatabase(databaseSettings.id()));
    }

    /**
     * CREATE a Database if it does not already exist on the service
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the 
     * created or existing database.
     * @param id the id of the database
     * @return a {@link Mono} containing the cosmos database response with the created or existing database or
     * an error
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(String id) {
        return createDatabaseIfNotExistsInternal(getDatabase(id));
    }
    
    private Mono<CosmosDatabaseResponse> createDatabaseIfNotExistsInternal(CosmosDatabase database){
        return database.read().onErrorResume(exception -> {
            if (exception instanceof CosmosClientException) {
                CosmosClientException cosmosClientException = (CosmosClientException) exception;
                if (cosmosClientException.statusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    return createDatabase(new CosmosDatabaseProperties(database.id()), new CosmosDatabaseRequestOptions());
                }
            }
            return Mono.error(exception);
        });
    }

    /**
     * Creates a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single resource response with the 
     *      created database.
     * In case of failure the {@link Mono} will error.
     * 
     * @param databaseSettings {@link CosmosDatabaseProperties}
     * @param options {@link CosmosDatabaseRequestOptions}
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseSettings,
                                                       CosmosDatabaseRequestOptions options) {
        if (options == null) {
            options = new CosmosDatabaseRequestOptions();
        }
        Database wrappedDatabase = new Database();
        wrappedDatabase.id(databaseSettings.id());
        return asyncDocumentClient.createDatabase(wrappedDatabase, options.toRequestOptions()).map(databaseResourceResponse ->
                new CosmosDatabaseResponse(databaseResourceResponse, this)).single();
    }

    /**
     * Creates a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single resource response with the 
     *      created database.
     * In case of failure the {@link Mono} will error.
     * 
     * @param databaseSettings {@link CosmosDatabaseProperties}
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseSettings) {
        return createDatabase(databaseSettings, new CosmosDatabaseRequestOptions());
    }

    /**
     * Creates a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single resource response with the 
     *      created database.
     * In case of failure the {@link Mono} will error.
     * 
     * @param id id of the database
     * @return a {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(String id) {
        return createDatabase(new CosmosDatabaseProperties(id), new CosmosDatabaseRequestOptions());
    }

    /**
     * Creates a database.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     *      created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param databaseSettings {@link CosmosDatabaseProperties}
     * @param throughput the throughput for the database
     * @param options {@link CosmosDatabaseRequestOptions}
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseSettings,
                                                       int throughput,
                                                       CosmosDatabaseRequestOptions options) {
        if (options == null) {
            options = new CosmosDatabaseRequestOptions();
        }
        options.offerThroughput(throughput);
        Database wrappedDatabase = new Database();
        wrappedDatabase.id(databaseSettings.id());
        return asyncDocumentClient.createDatabase(wrappedDatabase, options.toRequestOptions()).map(databaseResourceResponse ->
                new CosmosDatabaseResponse(databaseResourceResponse, this)).single();
    }

    /**
     * Creates a database.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     *      created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param databaseSettings {@link CosmosDatabaseProperties}
     * @param throughput the throughput for the database
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseSettings, int throughput) {
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        options.offerThroughput(throughput);
        return createDatabase(databaseSettings, options);
    }

    /**
     * Creates a database.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the
     *      created database.
     * In case of failure the {@link Mono} will error.
     *
     * @param id id of the database
     * @param throughput the throughput for the database
     * @return a {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(String id, int throughput) {
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        options.offerThroughput(throughput);
        return createDatabase(new CosmosDatabaseProperties(id), options);
    }

    /**
     * Reads all databases.
     * 
     * After subscription the operation will be performed. 
     * The {@link Flux} will contain one or several feed response of the read databases.
     * In case of failure the {@link Flux} will error.
     * 
     * @param options {@link FeedOptions}
     * @return a {@link Flux} containing one or several feed response pages of read databases or an error.
     */
    public Flux<FeedResponse<CosmosDatabaseProperties>> readAllDatabases(FeedOptions options) {
        return getDocClientWrapper().readDatabases(options)
                .map(response-> BridgeInternal.createFeedResponse(CosmosDatabaseProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Reads all databases.
     * 
     * After subscription the operation will be performed. 
     * The {@link Flux} will contain one or several feed response of the read databases.
     * In case of failure the {@link Flux} will error.
     * 
     * @return a {@link Flux} containing one or several feed response pages of read databases or an error.
     */
    public Flux<FeedResponse<CosmosDatabaseProperties>> readAllDatabases() {
        return readAllDatabases(new FeedOptions());
    }


    /**
     * Query for databases.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read databases.
     * In case of failure the {@link Flux} will error.
     *
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of read databases or an error.
     */
    public Flux<FeedResponse<CosmosDatabaseProperties>> queryDatabases(String query, FeedOptions options){
        return queryDatabases(new SqlQuerySpec(query), options);
    }

    /**
     * Query for databases.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read databases.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec     the SQL query specification.
     * @param options       the feed options.
     * @return an {@link Flux} containing one or several feed response pages of read databases or an error.
     */
    public Flux<FeedResponse<CosmosDatabaseProperties>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options){
        return getDocClientWrapper().queryDatabases(querySpec, options)
                .map(response-> BridgeInternal.createFeedResponse(
                        CosmosDatabaseProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    Mono<DatabaseAccount> getDatabaseAccount() {
        return asyncDocumentClient.getDatabaseAccount().single();
    }

    /**
     * Gets a database object without making a service call.
     *
     * @param id name of the database
     * @return {@link CosmosDatabase}
     */
    public CosmosDatabase getDatabase(String id) {
        return new CosmosDatabase(id, this);
    }

    /**
     * Close this {@link CosmosClient} instance and cleans up the resources.
     */
    @Override
    public void close() {
        asyncDocumentClient.close();
    }
}
