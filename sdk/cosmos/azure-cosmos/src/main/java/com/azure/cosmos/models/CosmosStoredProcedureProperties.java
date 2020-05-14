// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.StoredProcedure;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a stored procedure in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB allows stored procedures to be executed in the storage tier, directly against a container. The
 * script gets executed under ACID transactions on the primary storage partition of the specified collection. For
 * additional details, refer to the server-side JavaScript API documentation.
 */
public final class CosmosStoredProcedureProperties extends ResourceWrapper{

    private StoredProcedure storedProcedure;
    /**
     * Constructor.
     */
    public CosmosStoredProcedureProperties() {
        this.storedProcedure = new StoredProcedure();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return return the Cosmos stored procedure properties with id set
     */
    public CosmosStoredProcedureProperties setId(String id) {
        this.storedProcedure = new StoredProcedure();
        this.storedProcedure.setId(id);

        return this;
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the stored procedure.
     */
    CosmosStoredProcedureProperties(String jsonString) {
        this.storedProcedure = new StoredProcedure(jsonString);
    }

    /**
     * Constructor.
     *
     * @param id the id of the stored procedure
     * @param body the body of the stored procedure
     */
    public CosmosStoredProcedureProperties(String id, String body) {
        this.storedProcedure = new StoredProcedure();
        storedProcedure.setId(id);
        storedProcedure.setBody(body);
    }

    /**
     * Get the body of the stored procedure.
     *
     * @return the body of the stored procedure.
     */
    public String getBody() {
        return this.storedProcedure.getBody();
    }

    /**
     * Set the body of the stored procedure.
     *
     * @param body the body of the stored procedure.
     * @return return the Cosmos stored procedure properties.
     */
    public CosmosStoredProcedureProperties setBody(String body) {
        this.storedProcedure.setBody(body);
        return this;
    }

    static List<CosmosStoredProcedureProperties> getFromV2Results(List<StoredProcedure> results) {
        return results.stream().map(sproc -> new CosmosStoredProcedureProperties(sproc.toJson()))
                   .collect(Collectors.toList());
    }

    @Override
    Resource getResource() {
        return this.storedProcedure;
    }
}
