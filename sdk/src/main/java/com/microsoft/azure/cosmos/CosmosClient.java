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
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides a client-side logical representation of the Azure Cosmos database service.
 * This asynchronous client is used to configure and execute requests
 * against the service.
 */
 public class CosmosClient {

    //Document client wrapper
    private AsyncDocumentClient asyncDocumentClient;

    /**
     * Creates a cosmos client with given cosmosConfiguration
     * @param cosmosConfiguration the cosmosConfiguration
     * @return cosmos client
     */
    public static CosmosClient create(CosmosConfiguration cosmosConfiguration) {
        return new CosmosClient(cosmosConfiguration);
    }

    /**
     * Creates a cosmos client with given endpoint and key
     * @param endpoint the service end point
     * @param key the key
     * @return cosmos clients
     */
    public static CosmosClient create(String endpoint, String key) {
        CosmosConfiguration cosmosConfiguration = new CosmosConfiguration.Builder()
                .withServiceEndpoint(endpoint)
                .withKeyOrResourceToken(key).build();
        return create(cosmosConfiguration);
    }

    /**
     * Creates a cosmos client with given cosmos configuration
     * @param cosmosConfiguration the cosmos configuration
     */
    private CosmosClient(CosmosConfiguration cosmosConfiguration) {
        this.asyncDocumentClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(cosmosConfiguration.getServiceEndpoint().toString())
                .withMasterKeyOrResourceToken(cosmosConfiguration.getKeyOrResourceToken())
                .withConnectionPolicy(cosmosConfiguration.getConnectionPolicy())
                .withConsistencyLevel(cosmosConfiguration.getDesiredConsistencyLevel())
                .build();
    }

    AsyncDocumentClient getDocClientWrapper(){
        return asyncDocumentClient;
    }

    /**
     * Create a Database if it does not already exist on the service
     * 
     * The {@link Mono} upon successful completion will contain a single cosmos database response with the 
     * created or existing database.
     * @param databaseSettings CosmosDatabaseSettings
     * @return a {@link Mono} containing the cosmos database response with the created or existing database or
     * an error.
     */
    public Mono<CosmosDatabaseResponse> createDatabaseIfNotExists(CosmosDatabaseSettings databaseSettings) {
        return createDatabaseIfNotExistsInternal(getDatabase(databaseSettings.getId()));
    }

    /**
     * Create a Database if it does not already exist on the service
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
            if (exception instanceof DocumentClientException) {
                DocumentClientException documentClientException = (DocumentClientException) exception;
                if (documentClientException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    return createDatabase(new CosmosDatabaseSettings(database.getId()), new CosmosDatabaseRequestOptions());
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
        wrappedDatabase.setId(databaseSettings.getId());
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(asyncDocumentClient.createDatabase(wrappedDatabase, options.toRequestOptions()).map(databaseResourceResponse ->
                new CosmosDatabaseResponse(databaseResourceResponse, this)).toSingle()));
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
        return RxJava2Adapter.flowableToFlux(RxJavaInterop.toV2Flowable(getDocClientWrapper().readDatabases(options)
                .map(response-> BridgeInternal.createFeedResponse(CosmosDatabaseSettings.getFromV2Results(response.getResults()),
                        response.getResponseHeaders()))));
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
        return RxJava2Adapter.flowableToFlux(RxJavaInterop.toV2Flowable(getDocClientWrapper().queryDatabases(querySpec, options)
                .map(response-> BridgeInternal.createFeedResponse(
                        CosmosDatabaseSettings.getFromV2Results(response.getResults()),
                        response.getResponseHeaders()))));
    }

    /**
     * Gets a database object without making a service call.
     * @param id name of the database
     * @return
     */
    public CosmosDatabase getDatabase(String id) {
        return new CosmosDatabase(id, this);
    }

    /**
     * Close this {@link CosmosClient} instance and cleans up the resources.
     */
    public void close() {
        asyncDocumentClient.close();
    }
}
