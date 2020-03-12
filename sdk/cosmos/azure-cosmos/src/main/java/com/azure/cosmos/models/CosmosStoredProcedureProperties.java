// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
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
public final class CosmosStoredProcedureProperties extends Resource {

    /**
     * Constructor.
     */
    public CosmosStoredProcedureProperties() {
        super();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return return the Cosmos stored procedure properties with id set
     */
    public CosmosStoredProcedureProperties setId(String id) {
        super.setId(id);
        return this;
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the stored procedure.
     */
    CosmosStoredProcedureProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param id the id of the stored procedure
     * @param body the body of the stored procedure
     */
    public CosmosStoredProcedureProperties(String id, String body) {
        super();
        super.setId(id);
        this.setBody(body);
    }

    /**
     * Get the body of the stored procedure.
     *
     * @return the body of the stored procedure.
     */
    public String getBody() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the stored procedure.
     *
     * @param body the body of the stored procedure.
     */
    public void setBody(String body) {
        super.set(Constants.Properties.BODY, body);
    }


    static List<CosmosStoredProcedureProperties> getFromV2Results(List<StoredProcedure> results) {
        return results.stream().map(sproc -> new CosmosStoredProcedureProperties(sproc.toJson()))
                   .collect(Collectors.toList());
    }
}
