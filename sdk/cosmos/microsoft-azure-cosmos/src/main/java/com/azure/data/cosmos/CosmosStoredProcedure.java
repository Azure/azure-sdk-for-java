// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.StoredProcedure;
import reactor.core.publisher.Mono;

public class CosmosStoredProcedure {

    private CosmosContainer cosmosContainer;
    private String id;

    CosmosStoredProcedure(String id, CosmosContainer cosmosContainer) {
        this.id = id;
        this.cosmosContainer = cosmosContainer;
    }

    /**
     * Get the id of the {@link CosmosStoredProcedure}
     * @return the id of the {@link CosmosStoredProcedure}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosStoredProcedure}
     * @param id the id of the {@link CosmosStoredProcedure}
     * @return the same {@link CosmosStoredProcedure} that had the id set
     */
    CosmosStoredProcedure id(String id) {
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
    public Mono<CosmosStoredProcedureResponse> read() {
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
    public Mono<CosmosStoredProcedureResponse> read(CosmosStoredProcedureRequestOptions options) {
        if(options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase().getDocClientWrapper().readStoredProcedure(getLink(), options.toRequestOptions())
                .map(response -> new CosmosStoredProcedureResponse(response, cosmosContainer)).single();
    }

    /**
     * Deletes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosResponse> delete() {
        return delete(null);
    }

    /**
     * Deletes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosResponse> delete(CosmosStoredProcedureRequestOptions options) {
        if(options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                .getDocClientWrapper()
                .deleteStoredProcedure(getLink(), options.toRequestOptions())
                .map(response -> new CosmosResponse(response.getResource()))
                .single();
    }

    /**
     * Executes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the stored procedure response.
     * In case of failure the {@link Mono} will error.
     *
     * @param procedureParams the array of procedure parameter values.
     * @param options         the request options.
     * @return an {@link Mono} containing the single resource response with the stored procedure response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> execute(Object[] procedureParams, CosmosStoredProcedureRequestOptions options) {
        if(options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                .getDocClientWrapper()
                .executeStoredProcedure(getLink(), options.toRequestOptions(), procedureParams)
                .map(response -> new CosmosStoredProcedureResponse(response, cosmosContainer))
                .single();
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureSettings the stored procedure properties
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureSettings) {
        return replace(storedProcedureSettings, null);
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureSettings the stored procedure properties.
     * @param options                 the request options.
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureSettings,
                                                       CosmosStoredProcedureRequestOptions options) {
        if(options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                .getDocClientWrapper()
                .replaceStoredProcedure(new StoredProcedure(storedProcedureSettings.toJson()), options.toRequestOptions())
                .map(response -> new CosmosStoredProcedureResponse(response, cosmosContainer))
                .single();
    }

    String URIPathSegment() {
        return Paths.STORED_PROCEDURES_PATH_SEGMENT;
    }

    String parentLink() {
        return cosmosContainer.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(parentLink());
        builder.append("/");
        builder.append(URIPathSegment());
        builder.append("/");
        builder.append(id());
        return builder.toString();
    }
}
