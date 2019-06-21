/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.HttpConstants;
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
         this.configs = builder.getConfigs();
         this.serviceEndpoint = builder.getServiceEndpoint();
        this.keyOrResourceToken = builder.getKeyOrResourceToken();
        this.connectionPolicy = builder.getConnectionPolicy();
        this.desiredConsistencyLevel = builder.getDesiredConsistencyLevel();
        this.permissions = builder.getPermissions();
        this.tokenResolver = builder.getTokenResolver();
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

    public static AsyncDocumentClient getContextClient(CosmosClient cosmosClient) {
        return cosmosClient.asyncDocumentClient;
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
    public String getServiceEndpoint() {
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
    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Gets the consistency level
     * @return the (@link ConsistencyLevel)
     */
    public ConsistencyLevel getDesiredConsistencyLevel() {
        return desiredConsistencyLevel;
    }

    /**
     * Gets the permission list
     * @return the permission list
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

    AsyncDocumentClient getDocClientWrapper(){
        return asyncDocumentClient;
    }

    /**
     * Gets the configs
     * @return the configs
     */
    public Configs getConfigs() {
        return configs;
    }

    /**
     * Gets the token resolver
     * @return the token resolver
     */
    public TokenResolver getTokenResolver() {
        return tokenResolver;
    }

    /**
     * CREATE a Database if it does not already exist on the service
     * 
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the 
     * created or existing database.
     * @param databaseSettings CosmosDatabaseSettings
     * @return a {@link Mono} containing the cosmos database response with the created or existing database or
     * an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(CosmosDatabaseSettings databaseSettings) {
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
                    return createDatabase(new CosmosDatabaseSettings(database.id()), new CosmosDatabaseRequestOptions());
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
     * @param databaseSettings {@link CosmosDatabaseSettings}
     * @param options {@link CosmosDatabaseRequestOptions}
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseSettings databaseSettings,
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
     * @param databaseSettings {@link CosmosDatabaseSettings}
     * @return an {@link Mono} containing the single cosmos database response with the created database or an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseSettings databaseSettings) {
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
        return createDatabase(new CosmosDatabaseSettings(id), new CosmosDatabaseRequestOptions());
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
    public Flux<FeedResponse<CosmosDatabaseSettings>> listDatabases(FeedOptions options) {
        return getDocClientWrapper().readDatabases(options)
                .map(response-> BridgeInternal.createFeedResponse(CosmosDatabaseSettings.getFromV2Results(response.results()),
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
    public Flux<FeedResponse<CosmosDatabaseSettings>> listDatabases() {
        return listDatabases(new FeedOptions());
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
    public Flux<FeedResponse<CosmosDatabaseSettings>> queryDatabases(String query, FeedOptions options){
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
    public Flux<FeedResponse<CosmosDatabaseSettings>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options){
        return getDocClientWrapper().queryDatabases(querySpec, options)
                .map(response-> BridgeInternal.createFeedResponse(
                        CosmosDatabaseSettings.getFromV2Results(response.results()),
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
