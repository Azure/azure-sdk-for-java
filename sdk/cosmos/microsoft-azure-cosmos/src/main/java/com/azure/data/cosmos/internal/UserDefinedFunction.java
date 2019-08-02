// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Resource;

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
     * @param jsonString the json string that represents the user defined function.
     */
    public UserDefinedFunction(String jsonString) {
        super(jsonString);
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
        BridgeInternal.setProperty(this, Constants.Properties.BODY, body);
    }
}

