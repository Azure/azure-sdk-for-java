// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.StoredProcedure;
import com.azure.data.cosmos.internal.Trigger;
import com.azure.data.cosmos.internal.UserDefinedFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CosmosScripts {
    private final CosmosContainer container;
    private final CosmosDatabase database;

    CosmosScripts(CosmosContainer container) {
        this.container = container;
        this.database = container.getDatabase();
    }
    /* CosmosStoredProcedure operations */

    /**
     * Creates a cosmos stored procedure.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos stored procedure response with the
     * created cosmos stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param properties  the cosmos stored procedure properties.
     * @return an {@link Mono} containing the single cosmos stored procedure resource response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> createStoredProcedure(CosmosStoredProcedureProperties properties){
        return this.createStoredProcedure(properties, new CosmosStoredProcedureRequestOptions());
    }

    /**
     * Creates a cosmos stored procedure.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos stored procedure response with the
     * created cosmos stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param properties  the cosmos stored procedure properties.
     * @param options the stored procedure request options.
     * @return an {@link Mono} containing the single cosmos stored procedure resource response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> createStoredProcedure(CosmosStoredProcedureProperties properties,
                                                                     CosmosStoredProcedureRequestOptions options){
        if(options == null){
            options = new CosmosStoredProcedureRequestOptions();
        }
        StoredProcedure sProc = new StoredProcedure();
        sProc.id(properties.id());
        sProc.setBody(properties.body());
        return database.getDocClientWrapper()
                .createStoredProcedure(container.getLink(), sProc, options.toRequestOptions())
                .map(response -> new CosmosStoredProcedureResponse(response, this.container))
                .single();
    }

    /**
     * Reads all cosmos stored procedures in a container.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read cosmos stored procedure properties.
     * In case of failure the {@link Flux} will error.
     *
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read cosmos stored procedures
     * properties or an error.
     */
    public Flux<FeedResponse<CosmosStoredProcedureProperties>> readAllStoredProcedures(FeedOptions options){
        return database.getDocClientWrapper()
                .readStoredProcedures(container.getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(CosmosStoredProcedureProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Query for stored procedures in a container.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Flux} will error.
     *
     * @param query      the the query.
     * @param options    the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained stored procedures or
     * an error.
     */
    public Flux<FeedResponse<CosmosStoredProcedureProperties>> queryStoredProcedures(String query,
                                                                                   FeedOptions options){
        return queryStoredProcedures(new SqlQuerySpec(query), options);
    }

    /**
     * Query for stored procedures in a container.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec  the SQL query specification.
     * @param options    the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained stored procedures or
     * an error.
     */
    public Flux<FeedResponse<CosmosStoredProcedureProperties>> queryStoredProcedures(SqlQuerySpec querySpec,
                                                                                   FeedOptions options){
        return database.getDocClientWrapper()
                .queryStoredProcedures(container.getLink(), querySpec,options)
                .map(response -> BridgeInternal.createFeedResponse( CosmosStoredProcedureProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Gets a CosmosStoredProcedure object without making a service call
     * @param id id of the stored procedure
     * @return a cosmos stored procedure
     */
    public CosmosStoredProcedure getStoredProcedure(String id){
        return new CosmosStoredProcedure(id, this.container);
    }


    /* UDF Operations */

    /**
     * Creates a cosmos user defined function.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos user defined function response.
     * In case of failure the {@link Mono} will error.
     *
     * @param properties       the cosmos user defined function properties
     * @return an {@link Mono} containing the single resource response with the created user defined function or an error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> createUserDefinedFunction(CosmosUserDefinedFunctionProperties properties){
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.id(properties.id());
        udf.setBody(properties.body());

        return database.getDocClientWrapper()
                .createUserDefinedFunction(container.getLink(), udf, null)
                .map(response -> new CosmosUserDefinedFunctionResponse(response, this.container)).single();
    }

    /**
     * Reads all cosmos user defined functions in the container
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read user defined functions.
     * In case of failure the {@link Flux} will error.
     *
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read user defined functions or an error.
     */
    public Flux<FeedResponse<CosmosUserDefinedFunctionProperties>> readAllUserDefinedFunctions(FeedOptions options){
        return database.getDocClientWrapper()
                .readUserDefinedFunctions(container.getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(CosmosUserDefinedFunctionProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Query for user defined functions in the container.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Flux} will error.
     *
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    public Flux<FeedResponse<CosmosUserDefinedFunctionProperties>> queryUserDefinedFunctions(String query,
                                                                                           FeedOptions options){
        return queryUserDefinedFunctions(new SqlQuerySpec(query), options);
    }

    /**
     * Query for user defined functions in the container.
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    public Flux<FeedResponse<CosmosUserDefinedFunctionProperties>> queryUserDefinedFunctions(SqlQuerySpec querySpec,
                                                                                           FeedOptions options){
        return database.getDocClientWrapper()
                .queryUserDefinedFunctions(container.getLink(),querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(CosmosUserDefinedFunctionProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Gets a CosmosUserDefinedFunction object without making a service call
     * @param id id of the user defined function
     * @return a cosmos user defined function
     */
    public CosmosUserDefinedFunction getUserDefinedFunction(String id){
        return new CosmosUserDefinedFunction(id, this.container);
    }

    /* Trigger Operations */
    /**
     * Creates a Cosmos trigger.
     *
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a cosmos trigger response
     * In case of failure the {@link Mono} will error.
     *
     * @param properties the cosmos trigger properties
     * @return an {@link Mono} containing the single resource response with the created trigger or an error.
     */
    public Mono<CosmosTriggerResponse> createTrigger(CosmosTriggerProperties properties){
        Trigger trigger = new Trigger(properties.toJson());

        return database.getDocClientWrapper()
                .createTrigger(container.getLink(), trigger, null)
                .map(response -> new CosmosTriggerResponse(response, this.container))
                .single();
    }

    /**
     * Reads all triggers in a container
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read cosmos trigger properties.
     * In case of failure the {@link Flux} will error.
     *
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read cosmos rigger properties or an error.
     */
    public Flux<FeedResponse<CosmosTriggerProperties>> readAllTriggers(FeedOptions options){
        return database.getDocClientWrapper()
                .readTriggers(container.getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(CosmosTriggerProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Query for triggers in the container
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Flux} will error.
     *
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained triggers or an error.
     */
    public Flux<FeedResponse<CosmosTriggerProperties>> queryTriggers(String query, FeedOptions options){
        return queryTriggers(new SqlQuerySpec(query), options);
    }

    /**
     * Query for triggers in the container
     *
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained triggers or an error.
     */
    public Flux<FeedResponse<CosmosTriggerProperties>> queryTriggers(SqlQuerySpec querySpec,
                                                                   FeedOptions options){
        return database.getDocClientWrapper()
                .queryTriggers(container.getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(CosmosTriggerProperties.getFromV2Results(response.results()),
                        response.responseHeaders()));
    }

    /**
     * Gets a CosmosTrigger object without making a service call
     * @param id id of the cosmos trigger
     * @return a cosmos trigger
     */
    public CosmosTrigger getTrigger(String id){
        return new CosmosTrigger(id, this.container);
    }

}
