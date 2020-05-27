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
    private final CosmosAsyncStoredProcedure storedProcedure;

    /**
     * Instantiates a new Cosmos sync stored procedure.
     *
     * @param id the id
     * @param storedProcedure the stored procedure
     */
    public CosmosStoredProcedure(String id, CosmosAsyncStoredProcedure storedProcedure) {
        this.id = id;
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
     * @return the cosmos sync stored procedure response
     */
    public CosmosStoredProcedureResponse read() {
        return storedProcedure.read().block();
    }

    /**
     * Read cosmos sync stored procedure.
     *
     * @param options the options
     * @return the cosmos sync stored procedure response
     */
    public CosmosStoredProcedureResponse read(CosmosStoredProcedureRequestOptions options) {
        return storedProcedure.read(options).block();
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @return the cosmos sync response
     */
    public CosmosStoredProcedureResponse delete() {
        return storedProcedure.delete().block();
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @param options the options
     * @return the cosmos sync response
     */
    CosmosStoredProcedureResponse delete(CosmosStoredProcedureRequestOptions options) {
        return storedProcedure.delete(options).block();
    }

    /**
     * Execute cosmos sync stored procedure.
     *
     * @param procedureParams the procedure params
     * @param options the options
     * @return the cosmos sync stored procedure response
     */
    public CosmosStoredProcedureResponse execute(
        List<Object> procedureParams,
        CosmosStoredProcedureRequestOptions options) {
        return storedProcedure.execute(procedureParams, options).block();
    }

    /**
     * Replace cosmos sync stored procedure.
     *
     * @param storedProcedureSettings the stored procedure settings
     * @return the cosmos sync stored procedure response
     */
    public CosmosStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureSettings) {
        return storedProcedure.replace(storedProcedureSettings).block();
    }

    /**
     * Replace cosmos sync stored procedure.
     *
     * @param storedProcedureSettings the stored procedure settings
     * @param options the options
     * @return the cosmos sync stored procedure response
     */
    public CosmosStoredProcedureResponse replace(
        CosmosStoredProcedureProperties storedProcedureSettings,
        CosmosStoredProcedureRequestOptions options) {
        return storedProcedure.replace(storedProcedureSettings, options).block();

    }
}
