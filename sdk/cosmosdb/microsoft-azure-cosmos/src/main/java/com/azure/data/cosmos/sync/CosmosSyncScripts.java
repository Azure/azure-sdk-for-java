// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosScripts;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.CosmosStoredProcedureResponse;
import com.azure.data.cosmos.CosmosTriggerProperties;
import com.azure.data.cosmos.CosmosTriggerResponse;
import com.azure.data.cosmos.CosmosUserDefinedFunctionProperties;
import com.azure.data.cosmos.CosmosUserDefinedFunctionResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;

/**
 * The type Cosmos sync scripts.
 */
public class CosmosSyncScripts {
    private final CosmosSyncContainer container;
    private final CosmosScripts asyncScripts;

    /**
     * Instantiates a new Cosmos sync scripts.
     *
     * @param container the container
     * @param asyncScripts the async scripts
     */
    CosmosSyncScripts(CosmosSyncContainer container, CosmosScripts asyncScripts) {
        this.container = container;
        this.asyncScripts = asyncScripts;
    }
    /* CosmosStoredProcedure operations */

    /**
     * Create stored procedure 
     *
     * @param properties the properties
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse createStoredProcedure(CosmosStoredProcedureProperties properties)
            throws CosmosClientException {
        return mapStoredProcedureResponseAndBlock(asyncScripts.createStoredProcedure(properties,
                new CosmosStoredProcedureRequestOptions()));
    }

    /**
     * Create stored procedure cosmos 
     *
     * @param properties the properties
     * @param options the options
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse createStoredProcedure(CosmosStoredProcedureProperties properties,
                                                                   CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return mapStoredProcedureResponseAndBlock(asyncScripts.createStoredProcedure(properties,
                options));
    }

    /**
     * Read all stored procedures iterator.
     *
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosStoredProcedureProperties>> readAllStoredProcedures(FeedOptions options) {
        return getFeedIterator(asyncScripts.readAllStoredProcedures(options));
    }

    /**
     * Query stored procedures iterator.
     *
     * @param query the query
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosStoredProcedureProperties>> queryStoredProcedures(String query,
                                                                                         FeedOptions options) {
        return getFeedIterator(asyncScripts.queryStoredProcedures(query, options));
    }

    /**
     * Query stored procedures iterator.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosStoredProcedureProperties>> queryStoredProcedures(SqlQuerySpec querySpec,
                                                                                         FeedOptions options) {
        return getFeedIterator(asyncScripts.queryStoredProcedures(querySpec, options));

    }

    /**
     * Gets stored procedure.
     *
     * @param id the id
     * @return the stored procedure
     */
    public CosmosSyncStoredProcedure getStoredProcedure(String id) {
        return new CosmosSyncStoredProcedure(id,
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
    public CosmosSyncUserDefinedFunctionResponse createUserDefinedFunction(CosmosUserDefinedFunctionProperties properties) throws CosmosClientException {
        return mapUDFResponseAndBlock(asyncScripts.createUserDefinedFunction(properties));
    }

    /**
     * Read all user defined functions iterator.
     *
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserDefinedFunctionProperties>> readAllUserDefinedFunctions(FeedOptions options) {
        return getFeedIterator(asyncScripts.readAllUserDefinedFunctions(options));
    }

    /**
     * Query user defined functions iterator.
     *
     * @param query the query
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserDefinedFunctionProperties>> queryUserDefinedFunctions(String query,
                                                                                                 FeedOptions options) {
        return getFeedIterator(asyncScripts.queryUserDefinedFunctions(new SqlQuerySpec(query), options));
    }

    /**
     * Query user defined functions iterator.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosUserDefinedFunctionProperties>> queryUserDefinedFunctions(SqlQuerySpec querySpec,
                                                                                                 FeedOptions options) {
        return getFeedIterator(asyncScripts.queryUserDefinedFunctions(querySpec, options));
    }

    /**
     * Gets user defined function.
     *
     * @param id the id
     * @return the user defined function
     */
    public CosmosSyncUserDefinedFunction getUserDefinedFunction(String id) {
        return new CosmosSyncUserDefinedFunction(id,
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
    public CosmosSyncTriggerResponse createTrigger(CosmosTriggerProperties properties) throws CosmosClientException {
        return mapTriggerResponseAndBlock(asyncScripts.createTrigger(properties));
    }

    /**
     * Read all triggers iterator.
     *
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosTriggerProperties>> readAllTriggers(FeedOptions options) {
        return getFeedIterator(asyncScripts.readAllTriggers(options));
    }

    /**
     * Query triggers iterator.
     *
     * @param query the query
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosTriggerProperties>> queryTriggers(String query, FeedOptions options) {
        return getFeedIterator(asyncScripts.queryTriggers(query, options));
    }

    /**
     * Query triggers iterator.
     *
     * @param querySpec the query spec
     * @param options the options
     * @return the iterator
     */
    public Iterator<FeedResponse<CosmosTriggerProperties>> queryTriggers(SqlQuerySpec querySpec,
                                                                         FeedOptions options) {
        return getFeedIterator(asyncScripts.queryTriggers(querySpec, options));
    }

    /**
     * Gets trigger.
     *
     * @param id the id
     * @return the trigger
     */
    public CosmosSyncTrigger getTrigger(String id) {
        return new CosmosSyncTrigger(id,
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
    CosmosSyncStoredProcedureResponse mapStoredProcedureResponseAndBlock(Mono<CosmosStoredProcedureResponse> storedProcedureResponseMono)
            throws CosmosClientException {
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
     * Map delete response and block cosmos sync response.
     *
     * @param responseMono the response mono
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosSyncResponse mapDeleteResponseAndBlock(Mono<CosmosResponse> responseMono)
            throws CosmosClientException {
        try {
            return responseMono
                           .map(this::convertDeleteResponse)
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
     * Convert delete response cosmos sync response.
     *
     * @param response the response
     * @return the cosmos sync response
     */
    CosmosSyncResponse convertDeleteResponse(CosmosResponse response) {
        return new CosmosSyncResponse(response);
    }

    /**
     * Convert response cosmos sync stored procedure response.
     *
     * @param response the response
     * @return the cosmos sync stored procedure response
     */
    CosmosSyncStoredProcedureResponse convertResponse(CosmosStoredProcedureResponse response) {
        return new CosmosSyncStoredProcedureResponse(response, getStoredProcedure(response.storedProcedure().id()));
    }

    /**
     * Map udf response and block cosmos sync user defined function response.
     *
     * @param responseMono the response mono
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosSyncUserDefinedFunctionResponse mapUDFResponseAndBlock(Mono<CosmosUserDefinedFunctionResponse> responseMono)
            throws CosmosClientException {
        try {
            return responseMono
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
     * Convert response cosmos sync user defined function response.
     *
     * @param response the response
     * @return the cosmos sync user defined function response
     */
    CosmosSyncUserDefinedFunctionResponse convertResponse(CosmosUserDefinedFunctionResponse response) {
        return new CosmosSyncUserDefinedFunctionResponse(response,
                getUserDefinedFunction(response.userDefinedFunction().id()));
    }

    //Trigger

    /**
     * Map trigger response and block cosmos sync trigger response.
     *
     * @param responseMono the response mono
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosSyncTriggerResponse mapTriggerResponseAndBlock(Mono<CosmosTriggerResponse> responseMono)
            throws CosmosClientException {
        try {
            return responseMono
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
     * Convert response cosmos sync trigger response.
     *
     * @param response the response
     * @return the cosmos sync trigger response
     */
    CosmosSyncTriggerResponse convertResponse(CosmosTriggerResponse response) {
        return new CosmosSyncTriggerResponse(response,
                getTrigger(response.trigger().id()));
    }

    private <T> Iterator<FeedResponse<T>> getFeedIterator(Flux<FeedResponse<T>> itemFlux) {
        return itemFlux.toIterable(1).iterator();
    }

}
