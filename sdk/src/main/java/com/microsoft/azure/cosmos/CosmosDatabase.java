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
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.Paths;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import org.apache.commons.lang3.StringUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Cosmos Database
 */
public class CosmosDatabase extends CosmosResource {
    private CosmosClient client;

    CosmosDatabase(String id, CosmosClient client) {
        super(id);
        this.client = client;
    }

    /**
     * Reads a database.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos database respone with the read database.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos database respone with the read database or an error.
     */
    public Mono<CosmosDatabaseResponse> read(){
        return read(new CosmosDatabaseRequestOptions());
    }

    /**
     * Reads a database.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos cosmos database respone with the read database.
     * In case of failure the {@link Mono} will error.
     *
     * @param options      the request options.
     * @return an {@link Mono} containing the single cosmos database response with the read database or an error.
     */
    public Mono<CosmosDatabaseResponse> read(CosmosDatabaseRequestOptions options) {
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(getDocClientWrapper().readDatabase(getLink(),
                                                                                          options.toRequestOptions())
                .map(response -> new CosmosDatabaseResponse(response, getClient())).toSingle()));
    }

    /**
     * Deletes a database.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos database response with the deleted database.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single cosmos database response
     */
    public Mono<CosmosDatabaseResponse> delete() {
        return delete(new CosmosRequestOptions());
    }

    /**
     * Deletes a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a cosmos database response with the deleted database.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options
     * @return an {@link Mono} containing the single cosmos database response
     */
    public Mono<CosmosDatabaseResponse> delete(CosmosRequestOptions options) {
        return RxJava2Adapter.singleToMono(
                RxJavaInterop.toV2Single(getDocClientWrapper()
                                                 .deleteDatabase(getLink(), options.toRequestOptions())
                                                 .map(response -> new CosmosDatabaseResponse(response, getClient()))
                                                 .toSingle()));
    }

    /* CosmosContainer operations */

    /**
     * Creates a document container.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos container response with the created 
     * collection.
     * In case of failure the {@link Mono} will error.
     *
     * @param containerSettings   the container settings.
     * @return an {@link Flux} containing the single cosmos container response with the created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainer(CosmosContainerSettings containerSettings) {
        validateResource(containerSettings);
        return createContainer(containerSettings, new CosmosContainerRequestOptions());
    }

    /**
     * Creates a document container.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos container response with the created 
     * collection.
     * In case of failure the {@link Mono} will error.
     *
     * @param containerSettings   the containerSettings.
     * @param options the cosmos container request options                           
     * @return an {@link Flux} containing the cosmos container response with the created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainer(CosmosContainerSettings containerSettings,
            CosmosContainerRequestOptions options) {
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(getDocClientWrapper().createCollection(this.getLink(),
                containerSettings.getV2Collection(), options.toRequestOptions()).map(response ->
                new CosmosContainerResponse(response, this)).toSingle()));
    }

    /**
     * Creates a document container.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos container response with the created 
     * collection.
     * In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id
     * @param partitionKeyPath the partition key path
     * @return an {@link Flux} containing the cosmos container response with the created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainer(String id, String partitionKeyPath) {
        return createContainer(new CosmosContainerSettings(id, partitionKeyPath));
    }

    /**
     * Creates a document container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a cosmos container response with the created
     * or existing collection.
     * In case of failure the {@link Mono} will error.
     *
     * @param containerSettings the container settings
     * @return a {@link Mono} containing the cosmos container response with the created or existing container or
     * an error.
     */
    public Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosContainerSettings containerSettings) {
        CosmosContainer container = getContainer(containerSettings.getId());
        return createContainerIfNotExistsInternal(containerSettings, container);
    }

    /**
     * Creates a document container if it does not exist on the service.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos container response with the created
     * collection.
     * In case of failure the {@link Mono} will error.
     *
     * @param id the cosmos container id
     * @param partitionKeyPath the partition key path
     * @return an {@link Flux} containing the cosmos container response with the created container or an error.
     */
    public Mono<CosmosContainerResponse> createContainerIfNotExists(String id, String partitionKeyPath) {
        CosmosContainer container = getContainer(id);
        return createContainerIfNotExistsInternal(new CosmosContainerSettings(id, partitionKeyPath), container);
    }


    private Mono<CosmosContainerResponse> createContainerIfNotExistsInternal(CosmosContainerSettings containerSettings, CosmosContainer container) {
        return container.read().onErrorResume(exception -> {
            if (exception instanceof DocumentClientException) {
                DocumentClientException documentClientException = (DocumentClientException) exception;
                if (documentClientException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    return createContainer(containerSettings);
                }
            }
            return Mono.error(exception);
        });
    }


    /**
     * Reads all cosmos containers.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read containers.
     * In case of failure the {@link Flux} will error.
     *
     * @param options {@link FeedOptions}
     * @return a {@link Flux} containing one or several feed response pages of read containers or an error.
     */
    public Flux<FeedResponse<CosmosContainerSettings>> listContainers(FeedOptions options) {
        //TODO:
        return RxJava2Adapter.flowableToFlux(RxJavaInterop.toV2Flowable(getDocClientWrapper().readCollections(getLink(), options)
                .map(response-> BridgeInternal.createFeedResponse(CosmosContainerSettings.getFromV2Results(response.getResults()),
                        response.getResponseHeaders()))));
    }

    /**
     * Reads all cosmos containers.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read containers.
     * In case of failure the {@link Flux} will error.
     *
     * @return a {@link Flux} containing one or several feed response pages of read containers or an error.
     */
    public Flux<FeedResponse<CosmosContainerSettings>> listContainers() {
        return listContainers(new FeedOptions());
    }

    /**
     * Query for cosmos containers in a cosmos database.
     *
     * After subscription the operation will be performed. 
     * The {@link Flux} will contain one or several feed response of the obtained containers.
     * In case of failure the {@link Flux} will error.
     *
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained containers or an error.
     */
    public Flux<FeedResponse<CosmosContainerSettings>> queryContainers(String query, FeedOptions options){
        return queryContainers(new SqlQuerySpec(query), options);
    }

    /**
     * Query for cosmos containers in a cosmos database.
     *
     * After subscription the operation will be performed. 
     * The {@link Flux} will contain one or several feed response of the obtained containers.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained containers or an error.
     */
    public Flux<FeedResponse<CosmosContainerSettings>> queryContainers(SqlQuerySpec querySpec, FeedOptions options){
        return RxJava2Adapter.flowableToFlux(RxJavaInterop.toV2Flowable(getDocClientWrapper().queryCollections(getLink(), querySpec,
                                                                                                options)
                .map(response-> BridgeInternal.createFeedResponse(
                        CosmosContainerSettings.getFromV2Results(response.getResults()),
                        response.getResponseHeaders()))));
    }

    /**
     * Gets a CosmosContainer object without making a service call
     * @param id id of the container
     * @return Cosmos Container
     */
    public CosmosContainer getContainer(String id) {
        return new CosmosContainer(id, this);
    }
    
    /** User operations **/

    /**
     * Creates a user
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the created user.
     * In case of failure the {@link Mono} will error.
     * 
     * @param settings the cosmos user settings
     * @param options the request options
     * @return an {@link Mono} containing the single resource response with the created cosmos user or an error.
     */
    public Mono<CosmosUserResponse> createUser(CosmosUserSettings settings, RequestOptions options){
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(getDocClientWrapper().createUser(this.getLink(),
                settings.getV2User(), options).map(response ->
                new CosmosUserResponse(response, this)).toSingle())); 
    }

    /**
     * Upsert a user
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the created user.
     * In case of failure the {@link Mono} will error.
     *  
     * @param settings the cosmos user settings
     * @param options the request options
     * @return an {@link Mono} containing the single resource response with the upserted user or an error.
     */
    public Mono<CosmosUserResponse> upsertUser(CosmosUserSettings settings, RequestOptions options){
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(getDocClientWrapper().upsertUser(this.getLink(),
                settings.getV2User(), options).map(response ->
                new CosmosUserResponse(response, this)).toSingle()));
    }

    /**
     * Reads all cosmos users in a database.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read cosmos users.
     * In case of failure the {@link Flux} will error.
     *
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read cosmos users or an error.
     */
    public Flux<FeedResponse<CosmosUserSettings>> listUsers(FeedOptions options){
        return RxJava2Adapter.flowableToFlux(
                RxJavaInterop.toV2Flowable(getDocClientWrapper()
                        .readUsers(getLink(), options)
                        .map(response-> BridgeInternal.createFeedResponse(CosmosUserSettings.getFromV2Results(response.getResults(),this),
                                response.getResponseHeaders()))));
    }

    /**
     * Query for cosmos users in a database.
     *
     * After subscription the operation will be performed. 
     * The {@link Flux} will contain one or several feed response of the obtained users.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained users or an error.
     */
    public Flux<FeedResponse<CosmosUserSettings>> queryUsers(SqlQuerySpec querySpec, FeedOptions options){
        return RxJava2Adapter.flowableToFlux(
                RxJavaInterop.toV2Flowable(getDocClientWrapper()
                        .queryUsers(getLink(), querySpec, options)
                        .map(response-> BridgeInternal.createFeedResponseWithQueryMetrics(
                                CosmosUserSettings.getFromV2Results(response.getResults(), this),
                                response.getResponseHeaders(), response.getQueryMetrics()))));
    }

    CosmosClient getClient() {
        return client;
    }

    AsyncDocumentClient getDocClientWrapper(){
        return client.getDocClientWrapper();
    }

    @Override
    protected String getURIPathSegment() {
        return Paths.DATABASES_PATH_SEGMENT;
    }

    @Override
    protected String getParentLink() {
        return StringUtils.EMPTY;
    }
}
