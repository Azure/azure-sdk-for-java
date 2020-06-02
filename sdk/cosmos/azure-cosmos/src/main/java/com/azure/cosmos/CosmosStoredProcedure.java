// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;

import java.util.List;

/**
 * The type Cosmos sync stored procedure.
 */
public class CosmosStoredProcedure {
    private final String id;
    private final CosmosContainer container;
    private final CosmosAsyncStoredProcedure storedProcedure;

    /**
     * Instantiates a new Cosmos sync stored procedure.
     *
     * @param id the id
     * @param container the container
     * @param storedProcedure the stored procedure
     */
    public CosmosStoredProcedure(String id, CosmosContainer container, CosmosAsyncStoredProcedure storedProcedure) {

        this.id = id;
        this.container = container;
        this.storedProcedure = storedProcedure;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String getId() {
        return id;
    }

    /**
     * Read cosmos sync stored procedure.
     *
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse read() {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.read());
    }

    /**
     * Read cosmos sync stored procedure.
     *
     * @param options the options
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse read(CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.read(options));
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @return the cosmos response
     */
    public CosmosStoredProcedureResponse delete() {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.delete());
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @param options the options
     * @return the cosmos response
     */
    CosmosStoredProcedureResponse delete(CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.delete(options));
    }

    /**
     * Execute cosmos sync stored procedure.
     *
     * @param procedureParams the procedure params
     * @param options the options
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse execute(
        List<Object> procedureParams,
        CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.execute(procedureParams, options));
    }

    /**
     * Replace cosmos sync stored procedure.
     *
     * @param storedProcedureSettings the stored procedure settings
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureSettings) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.replace(storedProcedureSettings));
    }

    /**
     * Replace cosmos sync stored procedure.
     *
     * @param storedProcedureSettings the stored procedure settings
     * @param options the options
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse replace(
        CosmosStoredProcedureProperties storedProcedureSettings,
        CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.replace(storedProcedureSettings, options));

    }
}
