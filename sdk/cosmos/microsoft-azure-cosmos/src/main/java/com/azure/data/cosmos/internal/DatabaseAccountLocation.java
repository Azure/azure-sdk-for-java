// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.JsonSerializable;

import static com.azure.data.cosmos.BridgeInternal.setProperty;

/**
 * Represents the location of a database account in the Azure Cosmos DB database service.
 */
public class DatabaseAccountLocation extends JsonSerializable {

    /**
     * DEFAULT Constructor. Creates a new instance of the
     * DatabaseAccountLocation object.
     */
    public DatabaseAccountLocation() {
        super();
    }

    /**
     * Creates a new instance of the DatabaseAccountLocation object from a JSON
     * string.
     *
     * @param jsonString the JSON string that represents the DatabaseAccountLocation object.
     */
    public DatabaseAccountLocation(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets The name of the database account location.
     *
     * @return the name of the database account location.
     */
    public String getName() {
        return super.getString(Constants.Properties.Name);
    }

    /**
     * Sets the name of the database account location.
     *
     * @param name the name of the database account location.
     */
    public void setName(String name) {
        setProperty(this, Constants.Properties.Name, name);
    }

    /**
     * Gets The endpoint (the URI) of the database account location.
     *
     * @return the endpoint of the database account location.
     */
    public String getEndpoint() {
        return super.getString(Constants.Properties.DATABASE_ACCOUNT_ENDPOINT);
    }

    /**
     * Sets the endpoint (the URI) of the database account location.
     *
     * @param endpoint the endpoint of the database account location.
     */
    public void setEndpoint(String endpoint) {
        setProperty(this, Constants.Properties.DATABASE_ACCOUNT_ENDPOINT, endpoint);
    }
}
