// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.models.CosmosAsyncStoredProcedureResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

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
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase().getDocClientWrapper().readStoredProcedure(getLink(),
            ModelBridgeInternal.toRequestOptions(options))
                              .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer)).single();
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
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                   .getDocClientWrapper()
                   .deleteStoredProcedure(getLink(), ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer))
                   .single();
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
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                   .getDocClientWrapper()
                   .executeStoredProcedure(getLink(), ModelBridgeInternal.toRequestOptions(options), procedureParams)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer, this.id))
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
        if (options == null) {
            options = new CosmosStoredProcedureRequestOptions();
        }
        return cosmosContainer.getDatabase()
                   .getDocClientWrapper()
                   .replaceStoredProcedure(new StoredProcedure(ModelBridgeInternal.toJsonFromJsonSerializable(
                       ModelBridgeInternal.getResourceFromResourceWrapper(storedProcedureSettings))),
                       ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosAsyncStoredProcedureResponse(response, cosmosContainer))
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
        builder.append(id());
        return builder.toString();
    }
}
