// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a stored procedure in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB allows stored procedures to be executed in the storage tier, directly against a document collection. The
 * script gets executed under ACID transactions on the primary storage partition of the specified collection. For
 * additional details, refer to the server-side JavaScript API documentation.
 */
public class StoredProcedure extends Resource {

    /**
     * Constructor.
     */
    public StoredProcedure() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonNode the json node that represents the stored procedure.
     */
    public StoredProcedure(ObjectNode jsonNode) {
        super(jsonNode);
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return the current stored procedure
     */
    public StoredProcedure setId(String id){
        super.setId(id);
        return this;
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
        this.set(Constants.Properties.BODY, body, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }
}

