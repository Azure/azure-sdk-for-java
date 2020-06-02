// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncStoredProcedure}
     *
     * @param id the id of the {@link CosmosAsyncStoredProcedure}
     * @return the same {@link CosmosAsyncStoredProcedure} that had the id set
     */
    CosmosAsyncStoredProcedure setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Read a stored procedure
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
     * Read a stored procedure
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
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase().getDocClientWrapper().readStoredProcedure(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
                              .map(response -> ModelBridgeInternal.createCosmosStoredProcedureResponse(response)).single();
    }

    /**
     * Deletes a stored procedure
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> delete() {
        return delete(null);
    }

    /**
     * Deletes a stored procedure
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> delete(CosmosStoredProcedureRequestOptions options) {
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                   .getDocClientWrapper()
                   .deleteStoredProcedure(getLink(), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosStoredProcedureResponse(response))
                   .single();
    }

    /**
     * Executes a stored procedure
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the stored procedure
     * response.
     * In case of failure the {@link Mono} will error.
     *
     * @param procedureParams the list of procedure parameter values.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the stored procedure response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> execute(List<Object> procedureParams,
                                                       CosmosStoredProcedureRequestOptions options) {
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                   .getDocClientWrapper()
                   .executeStoredProcedure(getLink(), ModelBridgeInternal.toRequestOptions(options), procedureParams)
                   .map(response -> ModelBridgeInternal.createCosmosStoredProcedureResponse(response))
                   .single();
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureProperties the stored procedure properties
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureProperties) {
        return replace(storedProcedureProperties, null);
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureProperties the stored procedure properties.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> replace(CosmosStoredProcedureProperties storedProcedureProperties,
                                                       CosmosStoredProcedureRequestOptions options) {
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                   .getDocClientWrapper()
                   .replaceStoredProcedure(new StoredProcedure(ModelBridgeInternal.toJsonFromJsonSerializable(
                       ModelBridgeInternal.getResource(storedProcedureProperties))),
                       ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosStoredProcedureResponse(response))
                   .single();
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
        builder.append(getId());
        return builder.toString();
    }
}
