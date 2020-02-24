// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosStoredProcedure;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;

/**
 * The type Cosmos sync stored procedure.
 */
public class CosmosSyncStoredProcedure {
    private final String id;
    private final CosmosSyncContainer container;
    private final CosmosStoredProcedure storedProcedure;

    /**
     * Instantiates a new Cosmos sync stored procedure.
     *
     * @param id the id
     * @param container the container
     * @param storedProcedure the stored procedure
     */
    public CosmosSyncStoredProcedure(String id, CosmosSyncContainer container, CosmosStoredProcedure storedProcedure) {

        this.id = id;
        this.container = container;
        this.storedProcedure = storedProcedure;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /**
     * Read cosmos sync stored procedure.
     *
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse read() throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.read());
    }

    /**
     * Read cosmos sync stored procedure.
     *
     * @param options the options
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse read(CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.read(options));
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncResponse delete() throws CosmosClientException {
        return container.getScripts()
                       .mapDeleteResponseAndBlock(storedProcedure.delete());
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @param options the options
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosSyncResponse delete(CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapDeleteResponseAndBlock(storedProcedure.delete(options));
    }

    /**
     * Execute cosmos sync stored procedure.
     *
     * @param procedureParams the procedure params
     * @param options the options
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse execute(Object[] procedureParams,
                                                     CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.execute(procedureParams, options));
    }

    /**
     * Replace cosmos sync stored procedure.
     *
     * @param storedProcedureSettings the stored procedure settings
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureSettings)
            throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.replace(storedProcedureSettings));
    }

    /**
     * Replace cosmos sync stored procedure.
     *
     * @param storedProcedureSettings the stored procedure settings
     * @param options the options
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureSettings,
                                                     CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.replace(storedProcedureSettings, options));

    }
}
