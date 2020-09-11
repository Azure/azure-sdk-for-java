// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.Trigger;
import com.azure.cosmos.implementation.UserDefinedFunction;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

/**
 * The type Cosmos async scripts. This contains async methods to operate on cosmos scripts like UDFs, StoredProcedures
 * and Triggers
 */
public class CosmosAsyncScripts {
    private final CosmosAsyncContainer container;
    private final CosmosAsyncDatabase database;

    CosmosAsyncScripts(CosmosAsyncContainer container) {
        this.container = container;
        this.database = container.getDatabase();
    }
    /* CosmosAsyncStoredProcedure operations */

    /**
     * Creates a cosmos stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos stored procedure response with the
     * created cosmos stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param properties the cosmos stored procedure properties.
     * @return an {@link Mono} containing the single cosmos stored procedure resource response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> createStoredProcedure(CosmosStoredProcedureProperties properties) {
        return this.createStoredProcedure(properties, new CosmosStoredProcedureRequestOptions());
    }

    /**
     * Creates a cosmos stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos stored procedure response with the
     * created cosmos stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param properties the cosmos stored procedure properties.
     * @param options the stored procedure request options.
     * @return an {@link Mono} containing the single cosmos stored procedure resource response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> createStoredProcedure(
        CosmosStoredProcedureProperties properties,
        CosmosStoredProcedureRequestOptions options) {
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        StoredProcedure sProc = new StoredProcedure();
        sProc.setId(properties.getId());
        sProc.setBody(properties.getBody());
        final CosmosStoredProcedureRequestOptions requestOptions = options;
        return withContext(context -> createStoredProcedureInternal(sProc, requestOptions, context));
    }

    /**
     * Reads all cosmos stored procedures in a container.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read cosmos stored
     * procedure properties.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read cosmos stored
     * procedures
     * properties or an error.
     */
    public CosmosPagedFlux<CosmosStoredProcedureProperties> readAllStoredProcedures() {
        return readAllStoredProcedures(new CosmosQueryRequestOptions());
    }

    /**
     * Reads all cosmos stored procedures in a container.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read cosmos stored
     * procedure properties.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read cosmos stored
     * procedures
     * properties or an error.
     */
    CosmosPagedFlux<CosmosStoredProcedureProperties> readAllStoredProcedures(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllStoredProcedures." + this.container.getId();
            pagedFluxOptions.setTracerInformation(this.container.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.container.getDatabase().getClient().getServiceEndpoint(),
                this.container.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper()
                .readStoredProcedures(container.getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosStoredProcedurePropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    /**
     * Query for stored procedures in a container.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the the query.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained stored
     * procedures or
     * an error.
     */
    public CosmosPagedFlux<CosmosStoredProcedureProperties> queryStoredProcedures(
        String query,
            CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryStoredProceduresInternal(new SqlQuerySpec(query), options);
    }

    /**
     * Query for stored procedures in a container.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained stored
     * procedures or
     * an error.
     */
    public CosmosPagedFlux<CosmosStoredProcedureProperties> queryStoredProcedures(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryStoredProceduresInternal(querySpec, options);
    }

    /**
     * Gets a CosmosAsyncStoredProcedure object without making a service call
     *
     * @param id id of the stored procedure
     * @return a cosmos stored procedure
     */
    public CosmosAsyncStoredProcedure getStoredProcedure(String id) {
        return new CosmosAsyncStoredProcedure(id, this.container);
    }

    /* UDF Operations */

    /**
     * Creates a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos user defined function response.
     * In case of failure the {@link Mono} will error.
     *
     * @param properties the cosmos user defined function properties
     * @return an {@link Mono} containing the single resource response with the created user defined function or an
     * error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> createUserDefinedFunction(
        CosmosUserDefinedFunctionProperties properties) {
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.setId(properties.getId());
        udf.setBody(properties.getBody());
        return withContext(context -> createUserDefinedFunctionInternal(udf, context));
    }

    /**
     * Reads all cosmos user defined functions in the container
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read user defined functions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read user defined
     * functions or an
     * error.
     */
    public CosmosPagedFlux<CosmosUserDefinedFunctionProperties> readAllUserDefinedFunctions() {
        return readAllUserDefinedFunctions(new CosmosQueryRequestOptions());
    }

    /**
     * Reads all cosmos user defined functions in the container
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read user defined functions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read user defined
     * functions or an
     * error.
     */
    CosmosPagedFlux<CosmosUserDefinedFunctionProperties> readAllUserDefinedFunctions(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllUserDefinedFunctions." + this.container.getId();
            pagedFluxOptions.setTracerInformation(this.container.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.container.getDatabase().getClient().getServiceEndpoint(),
                this.container.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper()
                .readUserDefinedFunctions(container.getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosUserDefinedFunctionPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    /**
     * Query for user defined functions in the container.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained user defined
     * functions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained user defined
     * functions
     * or an error.
     */
    public CosmosPagedFlux<CosmosUserDefinedFunctionProperties> queryUserDefinedFunctions(
        String query,
        CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryUserDefinedFunctions(new SqlQuerySpec(query), options);
    }

    /**
     * Query for user defined functions in the container.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained user defined
     * functions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained user defined
     * functions
     * or an error.
     */
    public CosmosPagedFlux<CosmosUserDefinedFunctionProperties> queryUserDefinedFunctions(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryUserDefinedFunctionsInternal(querySpec, options);
    }

    /**
     * Gets a CosmosAsyncUserDefinedFunction object without making a service call
     *
     * @param id id of the user defined function
     * @return a cosmos user defined function
     */
    public CosmosAsyncUserDefinedFunction getUserDefinedFunction(String id) {
        return new CosmosAsyncUserDefinedFunction(id, this.container);
    }

    /* Trigger Operations */

    /**
     * Creates a Cosmos trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a cosmos trigger response
     * In case of failure the {@link Mono} will error.
     *
     * @param properties the cosmos trigger properties
     * @return an {@link Mono} containing the single resource response with the created trigger or an error.
     */
    public Mono<CosmosTriggerResponse> createTrigger(CosmosTriggerProperties properties) {
        return withContext(context -> createTriggerInternal(properties, context));
    }

    /**
     * Reads all triggers in a container
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read cosmos trigger
     * properties.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read cosmos rigger
     * properties or
     * an error.
     */
    public CosmosPagedFlux<CosmosTriggerProperties> readAllTriggers() {
        return readAllTriggers(new CosmosQueryRequestOptions());
    }

    /**
     * Reads all triggers in a container
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read cosmos trigger
     * properties.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read cosmos rigger
     * properties or
     * an error.
     */
    CosmosPagedFlux<CosmosTriggerProperties> readAllTriggers(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllTriggers." + this.container.getId();
            pagedFluxOptions.setTracerInformation(this.container.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.container.getDatabase().getClient().getServiceEndpoint(),
                this.container.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper()
                .readTriggers(container.getLink(), options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosTriggerPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    /**
     * Query for triggers in the container
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained triggers or an
     * error.
     */
    public CosmosPagedFlux<CosmosTriggerProperties> queryTriggers(String query, CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryTriggersInternal(false, new SqlQuerySpec(query), options);
    }

    /**
     * Query for triggers in the container
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained triggers or an
     * error.
     */
    public CosmosPagedFlux<CosmosTriggerProperties> queryTriggers(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryTriggersInternal(true, querySpec, options);
    }

    /**
     * Gets a CosmosAsyncTrigger object without making a service call
     *
     * @param id id of the cosmos trigger
     * @return a cosmos trigger
     */
    public CosmosAsyncTrigger getTrigger(String id) {
        return new CosmosAsyncTrigger(id, this.container);
    }

    private CosmosPagedFlux<CosmosStoredProcedureProperties> queryStoredProceduresInternal(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "queryStoredProcedures." + this.container.getId();
            pagedFluxOptions.setTracerInformation(this.container.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.container.getDatabase().getClient().getServiceEndpoint(),
                this.container.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper()
                .queryStoredProcedures(container.getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosStoredProcedurePropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    private CosmosPagedFlux<CosmosUserDefinedFunctionProperties> queryUserDefinedFunctionsInternal(
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "queryUserDefinedFunctions." + this.container.getId();
            pagedFluxOptions.setTracerInformation(this.container.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.container.getDatabase().getClient().getServiceEndpoint(),
                this.container.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper()
                .queryUserDefinedFunctions(container.getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosUserDefinedFunctionPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    private CosmosPagedFlux<CosmosTriggerProperties> queryTriggersInternal(
        boolean isParameterised,
        SqlQuerySpec querySpec,
        CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName;
            if (isParameterised) {
                spanName = "queryTriggers." + this.container.getId() + "." + querySpec.getQueryText();
            } else {
                spanName = "queryTriggers." + this.container.getId();
            }

            pagedFluxOptions.setTracerInformation(this.container.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.container.getDatabase().getClient().getServiceEndpoint(),
                this.container.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return database.getDocClientWrapper()
                .queryTriggers(container.getLink(), querySpec, options)
                .map(response -> BridgeInternal.createFeedResponse(
                    ModelBridgeInternal.getCosmosTriggerPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders()));
        });
    }

    private Mono<CosmosStoredProcedureResponse> createStoredProcedureInternal(StoredProcedure sProc,
                                                                           CosmosStoredProcedureRequestOptions options,
                                                                           Context context) {
        String spanName = "createStoredProcedure." + container.getId();
        Mono<CosmosStoredProcedureResponse> responseMono = createStoredProcedureInternal(sProc, options);
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosStoredProcedureResponse> createStoredProcedureInternal(StoredProcedure sProc,
                                                                           CosmosStoredProcedureRequestOptions options) {
        return database.getDocClientWrapper()
            .createStoredProcedure(container.getLink(), sProc, ModelBridgeInternal.toRequestOptions(options)).map(response -> ModelBridgeInternal.createCosmosStoredProcedureResponse(response))
            .single();
    }

    private Mono<CosmosUserDefinedFunctionResponse> createUserDefinedFunctionInternal(
        UserDefinedFunction udf,
        Context context) {
        String spanName = "createUserDefinedFunction." + container.getId();
        Mono<CosmosUserDefinedFunctionResponse> responseMono = createUserDefinedFunctionInternal(udf);
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosUserDefinedFunctionResponse> createUserDefinedFunctionInternal(
        UserDefinedFunction udf) {
        return database.getDocClientWrapper()
            .createUserDefinedFunction(container.getLink(), udf, null).map(response -> ModelBridgeInternal.createCosmosUserDefinedFunctionResponse(response)).single();
    }

    private Mono<CosmosTriggerResponse> createTriggerInternal(CosmosTriggerProperties properties, Context context) {
        String spanName = "createTrigger." + container.getId();
        Mono<CosmosTriggerResponse> responseMono = createTriggerInternal(properties);
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono,
            context,
            spanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosTriggerResponse> createTriggerInternal(CosmosTriggerProperties properties) {
        Trigger trigger = new Trigger(ModelBridgeInternal.toJsonFromJsonSerializable(ModelBridgeInternal.getResource(properties)));
        return database.getDocClientWrapper()
            .createTrigger(container.getLink(), trigger, null)
            .map(response -> ModelBridgeInternal.createCosmosTriggerResponse(response))
            .single();
    }

}
