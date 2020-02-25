// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * The type Cosmos sync scripts.
 */
public class CosmosScripts {
    private final CosmosContainer container;
    private final CosmosAsyncScripts asyncScripts;

    /**
     * Instantiates a new Cosmos sync scripts.
     *
     * @param container the container
     * @param asyncScripts the async scripts
     */
    CosmosScripts(CosmosContainer container, CosmosAsyncScripts asyncScripts) {
        this.container = container;
        this.asyncScripts = asyncScripts;
    }
    /* CosmosAsyncStoredProcedure operations */

    /**
     * Create stored procedure
     *
     * @param properties the properties
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosStoredProcedureResponse createStoredProcedure(CosmosStoredProcedureProperties properties) throws
        CosmosClientException {
        return mapStoredProcedureResponseAndBlock(
            asyncScripts.createStoredProcedure(properties, new CosmosStoredProcedureRequestOptions())
        );
    }

    /**
     * Create stored procedure cosmos
     *
     * @param properties the properties
     * @param options the options
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosStoredProcedureResponse createStoredProcedure(
        CosmosStoredProcedureProperties properties,
        CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return mapStoredProcedureResponseAndBlock(asyncScripts.createStoredProcedure(properties,
                                                                                     options));
    }

    /**
     * Read all stored procedures {@link CosmosPagedIterable}.
     *
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosStoredProcedureProperties> readAllStoredProcedures(FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.readAllStoredProcedures(options));
    }

    /**
     * Query stored procedures {@link CosmosPagedIterable}.
     *
     * @param query the query
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosStoredProcedureProperties> queryStoredProcedures(
        String query,
        FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.queryStoredProcedures(query, options));
    }

    /**
     * Query stored procedures {@link CosmosPagedIterable}.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosStoredProcedureProperties> queryStoredProcedures(
        SqlQuerySpec querySpec,
        FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.queryStoredProcedures(querySpec, options));

    }

    /**
     * Gets stored procedure.
     *
     * @param id the id
     * @return the stored procedure
     */
    public CosmosStoredProcedure getStoredProcedure(String id) {
        return new CosmosStoredProcedure(id,
                                         this.container,
                                         asyncScripts.getStoredProcedure(id));
    }


    /* UDF Operations */

    /**
     * Create user defined function
     *
     * @param properties the properties
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosUserDefinedFunctionResponse createUserDefinedFunction(CosmosUserDefinedFunctionProperties properties) 
        throws CosmosClientException {
        return mapUDFResponseAndBlock(asyncScripts.createUserDefinedFunction(properties));
    }

    /**
     * Read all user defined functions {@link CosmosPagedIterable}.
     *
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosUserDefinedFunctionProperties> readAllUserDefinedFunctions(
        FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.readAllUserDefinedFunctions(options));
    }

    /**
     * Query user defined functions {@link CosmosPagedIterable}.
     *
     * @param query the query
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosUserDefinedFunctionProperties> queryUserDefinedFunctions(
        String query,
        FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.queryUserDefinedFunctions(new SqlQuerySpec(query), options));
    }

    /**
     * Query user defined functions {@link CosmosPagedIterable}.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosUserDefinedFunctionProperties> queryUserDefinedFunctions(
        SqlQuerySpec querySpec,
        FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.queryUserDefinedFunctions(querySpec, options));
    }

    /**
     * Gets user defined function.
     *
     * @param id the id
     * @return the user defined function
     */
    public CosmosUserDefinedFunction getUserDefinedFunction(String id) {
        return new CosmosUserDefinedFunction(id,
                                             this.container,
                                             asyncScripts.getUserDefinedFunction(id));
    }

    /**
     * Create trigger
     *
     * @param properties the properties
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    /* Trigger Operations */
    public CosmosTriggerResponse createTrigger(CosmosTriggerProperties properties) throws CosmosClientException {
        return mapTriggerResponseAndBlock(asyncScripts.createTrigger(properties));
    }

    /**
     * Read all triggers {@link CosmosPagedIterable}.
     *
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosTriggerProperties> readAllTriggers(FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.readAllTriggers(options));
    }

    /**
     * Query triggers {@link CosmosPagedIterable}.
     *
     * @param query the query
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosTriggerProperties> queryTriggers(String query, FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.queryTriggers(query, options));
    }

    /**
     * Query triggers {@link CosmosPagedIterable}.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosTriggerProperties> queryTriggers(
        SqlQuerySpec querySpec,
        FeedOptions options) {
        return getCosmosPagedIterable(asyncScripts.queryTriggers(querySpec, options));
    }

    /**
     * Gets trigger.
     *
     * @param id the id
     * @return the trigger
     */
    public CosmosTrigger getTrigger(String id) {
        return new CosmosTrigger(id,
                                 this.container,
                                 asyncScripts.getTrigger(id));
    }

    /**
     * Map stored procedure response and block cosmos sync stored procedure response.
     *
     * @param storedProcedureResponseMono the stored procedure response mono
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosStoredProcedureResponse mapStoredProcedureResponseAndBlock(
        Mono<CosmosAsyncStoredProcedureResponse> storedProcedureResponseMono) throws CosmosClientException {
        try {
            return storedProcedureResponseMono
                       .map(this::convertResponse)
                       .block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosClientException) {
                throw (CosmosClientException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Convert response cosmos sync stored procedure response.
     *
     * @param response the response
     * @return the cosmos sync stored procedure response
     */
    CosmosStoredProcedureResponse convertResponse(CosmosAsyncStoredProcedureResponse response) {
        if (response.getStoredProcedure() != null) {
            return new CosmosStoredProcedureResponse(response, getStoredProcedure(response.getStoredProcedure().id()));
        } else {
            return new CosmosStoredProcedureResponse(response, null);
        }
    }

    /**
     * Map udf response and block cosmos sync user defined function response.
     *
     * @param responseMono the response mono
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosUserDefinedFunctionResponse mapUDFResponseAndBlock(
        Mono<CosmosAsyncUserDefinedFunctionResponse> responseMono) throws CosmosClientException {
        try {
            return responseMono
                       .map(this::convertResponse)
                       .block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosClientException) {
                throw (CosmosClientException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    /**
     * Convert response cosmos sync user defined function response.
     *
     * @param response the response
     * @return the cosmos sync user defined function response
     */
    CosmosUserDefinedFunctionResponse convertResponse(CosmosAsyncUserDefinedFunctionResponse response) {
        if (response.getUserDefinedFunction() != null) {
            return new CosmosUserDefinedFunctionResponse(response,
                                                         getUserDefinedFunction(response.getUserDefinedFunction()
                                                                                    .getId()));
        } else {
            return new CosmosUserDefinedFunctionResponse(response, null);
        }
    }

    //Trigger

    /**
     * Map trigger response and block cosmos sync trigger response.
     *
     * @param responseMono the response mono
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosTriggerResponse mapTriggerResponseAndBlock(Mono<CosmosAsyncTriggerResponse> responseMono) throws
        CosmosClientException {
        try {
            return responseMono
                       .map(this::convertResponse)
                       .block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosClientException) {
                throw (CosmosClientException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    /**
     * Convert response cosmos sync trigger response.
     *
     * @param response the response
     * @return the cosmos sync trigger response
     */
    CosmosTriggerResponse convertResponse(CosmosAsyncTriggerResponse response) {
        if (response.getTrigger() != null) {
            return new CosmosTriggerResponse(response,
                                             getTrigger(response.getTrigger().getId()));
        } else {
            return new CosmosTriggerResponse(response, null);
        }
    }

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }
}
