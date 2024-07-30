// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a user defined function in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB supports JavaScript UDFs which can be used inside queries, stored procedures and triggers. For additional
 * details, refer to the server-side JavaScript API documentation.
 */
public class UserDefinedFunction extends Resource {

    /**
     * Constructor.
     */
    public UserDefinedFunction() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonNode the json node that represents the user defined function.
     */
    public UserDefinedFunction(ObjectNode jsonNode) {
        super(jsonNode);
    }

    /**
     * Get the body of the user defined function.
     *
     * @return the body.
     */
    public String getBody() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the user defined function.
     *
     * @param body the body.
     */
    public void setBody(String body) {
        this.set(Constants.Properties.BODY, body, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }
}

