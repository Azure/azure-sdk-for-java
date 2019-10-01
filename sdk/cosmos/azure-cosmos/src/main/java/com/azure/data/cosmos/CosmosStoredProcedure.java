// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

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
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosStoredProcedureResponse read() throws CosmosClientException {
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
    public CosmosStoredProcedureResponse read(CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.read(options));
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosStoredProcedureResponse delete() throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.delete());
    }

    /**
     * Delete cosmos stored procedure.
     *
     * @param options the options
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    CosmosStoredProcedureResponse delete(CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.delete(options));
    }

    /**
     * Execute cosmos sync stored procedure.
     *
     * @param procedureParams the procedure params
     * @param options the options
     * @return the cosmos sync stored procedure response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosStoredProcedureResponse execute(Object[] procedureParams,
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
    public CosmosStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureSettings)
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
    public CosmosStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureSettings,
                                                 CosmosStoredProcedureRequestOptions options) throws CosmosClientException {
        return container.getScripts()
                       .mapStoredProcedureResponseAndBlock(storedProcedure.replace(storedProcedureSettings, options));

    }
}
