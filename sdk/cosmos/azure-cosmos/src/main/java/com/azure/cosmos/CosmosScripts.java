// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
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
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse createStoredProcedure(CosmosStoredProcedureProperties properties) {
        return blockStoredProcedureResponse(
            asyncScripts.createStoredProcedure(properties, new CosmosStoredProcedureRequestOptions())
        );
    }

    /**
     * Create stored procedure cosmos
     *
     * @param properties the properties
     * @param options the options
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse createStoredProcedure(
        CosmosStoredProcedureProperties properties,
        CosmosStoredProcedureRequestOptions options) {
        return blockStoredProcedureResponse(asyncScripts.createStoredProcedure(properties,
                                                                                     options));
    }

    /**
     * Read all stored procedures {@link CosmosPagedIterable}.
     *
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosStoredProcedureProperties> readAllStoredProcedures() {
        return getCosmosPagedIterable(asyncScripts.readAllStoredProcedures(new CosmosQueryRequestOptions()));
    }

    /**
     * Read all stored procedures {@link CosmosPagedIterable}.
     *
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    CosmosPagedIterable<CosmosStoredProcedureProperties> readAllStoredProcedures(CosmosQueryRequestOptions options) {
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
        CosmosQueryRequestOptions options) {
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
        CosmosQueryRequestOptions options) {
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
     * @return the cosmos user defined function response
     */
    public CosmosUserDefinedFunctionResponse createUserDefinedFunction(CosmosUserDefinedFunctionProperties properties) {
        return blockUDFResponse(asyncScripts.createUserDefinedFunction(properties));
    }

    /**
     * Read all user defined functions {@link CosmosPagedIterable}.
     *
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosUserDefinedFunctionProperties> readAllUserDefinedFunctions() {
        return getCosmosPagedIterable(asyncScripts.readAllUserDefinedFunctions(new CosmosQueryRequestOptions()));
    }

    /**
     * Read all user defined functions {@link CosmosPagedIterable}.
     *
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    CosmosPagedIterable<CosmosUserDefinedFunctionProperties> readAllUserDefinedFunctions(
        CosmosQueryRequestOptions options) {
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
        CosmosQueryRequestOptions options) {
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
        CosmosQueryRequestOptions options) {
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

    /* Trigger Operations */

    /**
     * Create trigger
     *
     * @param properties the properties
     * @return the cosmos trigger response
     */
    public CosmosTriggerResponse createTrigger(CosmosTriggerProperties properties) {
        return blockTriggerResponse(asyncScripts.createTrigger(properties));
    }

    /**
     * Read all triggers {@link CosmosPagedIterable}.
     *
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosTriggerProperties> readAllTriggers() {
        return getCosmosPagedIterable(asyncScripts.readAllTriggers(new CosmosQueryRequestOptions()));
    }

    /**
     * Read all triggers {@link CosmosPagedIterable}.
     *
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    CosmosPagedIterable<CosmosTriggerProperties> readAllTriggers(CosmosQueryRequestOptions options) {
        return getCosmosPagedIterable(asyncScripts.readAllTriggers(options));
    }

    /**
     * Query triggers {@link CosmosPagedIterable}.
     *
     * @param query the query
     * @param options the options
     * @return the {@link CosmosPagedIterable}
     */
    public CosmosPagedIterable<CosmosTriggerProperties> queryTriggers(String query, CosmosQueryRequestOptions options) {
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
        CosmosQueryRequestOptions options) {
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
     * Block cosmos stored procedure response.
     *
     * @param storedProcedureResponseMono the stored procedure response mono
     * @return the cosmos stored procedure response
     */
    CosmosStoredProcedureResponse blockStoredProcedureResponse(
        Mono<CosmosStoredProcedureResponse> storedProcedureResponseMono) {
        try {
            return storedProcedureResponseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Block cosmos user defined function response.
     *
     * @param responseMono the response mono
     * @return the cosmos user defined function response
     */
    CosmosUserDefinedFunctionResponse blockUDFResponse(
        Mono<CosmosUserDefinedFunctionResponse> responseMono) {
        try {
            return responseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    //Trigger

    /**
     * Block cosmos trigger response.
     *
     * @param responseMono the response mono
     * @return the cosmos trigger response
     */
    CosmosTriggerResponse blockTriggerResponse(Mono<CosmosTriggerResponse> responseMono) {
        try {
            return responseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw Exceptions.propagate(ex);
            }
        }
    }

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }
}
