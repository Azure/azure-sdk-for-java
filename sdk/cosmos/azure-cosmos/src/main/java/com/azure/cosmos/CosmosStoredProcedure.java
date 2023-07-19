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
      * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.read_no_params -->
      * <pre>
      * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
      *
      * CosmosStoredProcedureResponse response = procedure.read&#40;&#41;;
      * </pre>
      * <!-- end com.azure.cosmos.CosmosStoredProcedure.read_no_params -->
      * @return the cosmos stored procedure response
      */
    public CosmosStoredProcedureResponse read() {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.read());
    }

    /**
     * Read cosmos sync stored procedure.
     * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.read_with_options_param -->
     * <pre>
     * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
     * CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions&#40;&#41;;
     *
     * CosmosStoredProcedureResponse response = procedure.read&#40;options&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosStoredProcedure.read_with_options_param -->
     * @param options the options
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse read(CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.read(options));
    }

    /**
     * Delete cosmos stored procedure.
     * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.delete -->
     * <pre>
     * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
     *
     * CosmosStoredProcedureResponse response = procedure.delete&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosStoredProcedure.delete -->
     * @return the cosmos response
     */
    public CosmosStoredProcedureResponse delete() {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.delete());
    }

    /**
     * Delete cosmos stored procedure.
     * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.delete_with_options_param -->
     * <pre>
     * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
     * CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions&#40;&#41;;
     *
     * CosmosStoredProcedureResponse response = procedure.delete&#40;options&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosStoredProcedure.delete_with_options_param -->
     * @param options the options
     * @return the cosmos response
     */
    CosmosStoredProcedureResponse delete(CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.delete(options));
    }

    /**
     * Execute cosmos sync stored procedure.
     * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.execute -->
     * <pre>
     * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
     * CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions&#40;&#41;;
     * List&lt;Object&gt; procedureParams = new ArrayList&lt;&gt;&#40;&#41;;
     *
     * CosmosStoredProcedureResponse response = procedure.execute&#40;procedureParams, options&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosStoredProcedure.execute -->
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
     * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_param -->
     * <pre>
     * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
     * CosmosStoredProcedureProperties properties = new CosmosStoredProcedureProperties&#40;id, body&#41;;
     *
     * CosmosStoredProcedureResponse response = procedure.replace&#40;properties&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_param -->
     * @param storedProcedureProperties the stored procedure settings
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse replace(CosmosStoredProcedureProperties storedProcedureProperties) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.replace(storedProcedureProperties));
    }

    /**
     * Replace cosmos sync stored procedure.
     * <!-- src_embed com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_and_options_param -->
     * <pre>
     * CosmosStoredProcedure procedure = new CosmosStoredProcedure&#40;id, cosmosContainer, cosmosAsyncStoredProcedure&#41;;
     * CosmosStoredProcedureProperties properties = new CosmosStoredProcedureProperties&#40;id, body&#41;;
     * CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions&#40;&#41;;
     *
     * CosmosStoredProcedureResponse response = procedure.replace&#40;properties, options&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_and_options_param -->
     * @param storedProcedureProperties the stored procedure settings
     * @param options the options
     * @return the cosmos stored procedure response
     */
    public CosmosStoredProcedureResponse replace(
        CosmosStoredProcedureProperties storedProcedureProperties,
        CosmosStoredProcedureRequestOptions options) {
        return container.getScripts()
                   .blockStoredProcedureResponse(storedProcedure.replace(storedProcedureProperties, options));

    }
}
