// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.models.CosmosAsyncStoredProcedureResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The type Cosmos async stored procedure.
 */
public class CosmosAsyncStoredProcedure {

    @SuppressWarnings("EnforceFinalFields")
    private final CosmosAsyncContainer cosmosContainer;
    private String id;

    CosmosAsyncStoredProcedure(String id, CosmosAsyncContainer cosmosContainer) {
        this.id = id;
        this.cosmosContainer = cosmosContainer;
    }

    /**
     * Get the id of the {@link CosmosAsyncStoredProcedure}
     *
     * @return the id of the {@link CosmosAsyncStoredProcedure}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncStoredProcedure}
     *
     * @param id the id of the {@link CosmosAsyncStoredProcedure}
     * @return the same {@link CosmosAsyncStoredProcedure} that had the id set
     */
    CosmosAsyncStoredProcedure id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Read a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the read stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response with the read stored procedure or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> read() {
        return read(null);
    }

    /**
     * Read a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the read stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the read stored procedure or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> read(CosmosStoredProcedureRequestOptions options) {
        return withContext(context -> read(options, context)).subscriberContext(TracerProvider.callDepthAttributeFunc);
    }

    /**
     * Deletes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> delete() {
        return delete(null);
    }

    /**
     * Deletes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> delete(CosmosStoredProcedureRequestOptions options) {
        return withContext(context -> delete(options, context)).subscriberContext(TracerProvider.callDepthAttributeFunc);
    }

    /**
     * Executes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the stored procedure
     * response.
     * In case of failure the {@link Mono} will error.
     *
     * @param procedureParams the array of procedure parameter values.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the stored procedure response or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> execute(Object[] procedureParams,
                                                            CosmosStoredProcedureRequestOptions options) {
        return withContext(context -> execute(procedureParams, options, context)).subscriberContext(TracerProvider.callDepthAttributeFunc);
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureSettings the stored procedure properties
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureSettings) {
        return replace(storedProcedureSettings, null);
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureSettings the stored procedure properties.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosAsyncStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureSettings,
                                                            CosmosStoredProcedureRequestOptions options) {
        return withContext(context -> replace(storedProcedureSettings, options, context)).subscriberContext(TracerProvider.callDepthAttributeFunc);
    }

    String getURIPathSegment() {
        return Paths.STORED_PROCEDURES_PATH_SEGMENT;
    }

    String getParentLink() {
        return cosmosContainer.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(getParentLink());
        builder.append("/");
        builder.append(getURIPathSegment());
        builder.append("/");
        builder.append(id());
        return builder.toString();
    }

    private Mono<CosmosAsyncStoredProcedureResponse> read(CosmosStoredProcedureRequestOptions options,
                                                          Context context) {
        String spanName = "readStoredProcedure." + cosmosContainer.getId();
        Map<String, String> tracingAttributes = TracerProvider.createTracingMap(cosmosContainer.getDatabase().getId(),
            cosmosContainer.getDatabase().getClient().getServiceEndpoint(), spanName);
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        Mono<CosmosAsyncStoredProcedureResponse> responseMono =
            cosmosContainer.getDatabase().getDocClientWrapper().readStoredProcedure(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer)).single();
        return this.cosmosContainer.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }

    private Mono<CosmosAsyncStoredProcedureResponse> delete(CosmosStoredProcedureRequestOptions options,
                                                            Context context) {
        String spanName = "deleteStoredProcedure." + cosmosContainer.getId();
        Map<String, String> tracingAttributes = TracerProvider.createTracingMap(cosmosContainer.getDatabase().getId(),
            cosmosContainer.getDatabase().getClient().getServiceEndpoint(), spanName);
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        Mono<CosmosAsyncStoredProcedureResponse> responseMono = cosmosContainer.getDatabase()
            .getDocClientWrapper()
            .deleteStoredProcedure(getLink(), ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer))
            .single();
        return this.cosmosContainer.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }

    private Mono<CosmosAsyncStoredProcedureResponse> execute(Object[] procedureParams,
                                                             CosmosStoredProcedureRequestOptions options,
                                                             Context context) {
        String spanName = "executeStoredProcedure." + cosmosContainer.getId();
        Map<String, String> tracingAttributes = TracerProvider.createTracingMap(cosmosContainer.getDatabase().getId(),
            cosmosContainer.getDatabase().getClient().getServiceEndpoint(), spanName);
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        Mono<CosmosAsyncStoredProcedureResponse> responseMono = cosmosContainer.getDatabase()
            .getDocClientWrapper()
            .executeStoredProcedure(getLink(), ModelBridgeInternal.toRequestOptions(options), procedureParams)
            .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer,
                this.id))
            .single();
        return this.cosmosContainer.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }

    private Mono<CosmosAsyncStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureSettings,
                                                             CosmosStoredProcedureRequestOptions options,
                                                             Context context) {
        String spanName = "replaceStoredProcedure." + cosmosContainer.getId();
        Map<String, String> tracingAttributes = TracerProvider.createTracingMap(cosmosContainer.getDatabase().getId(),
            cosmosContainer.getDatabase().getClient().getServiceEndpoint(), spanName);
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        Mono<CosmosAsyncStoredProcedureResponse> responseMono = cosmosContainer.getDatabase()
            .getDocClientWrapper()
            .replaceStoredProcedure(new StoredProcedure(ModelBridgeInternal.toJsonFromJsonSerializable(storedProcedureSettings)),
                ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer))
            .single();
        return this.cosmosContainer.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }

}
