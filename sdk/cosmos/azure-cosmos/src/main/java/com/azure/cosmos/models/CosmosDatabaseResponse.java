// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;

/**
 * The type Cosmos database response. This contains get the methods to obtain database properties and other response 
 * methods
 */
public class CosmosDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties> {
    private final CosmosAsyncDatabaseResponse responseWrapper;
    private final CosmosDatabase database;

    CosmosDatabaseResponse(CosmosAsyncDatabaseResponse response, CosmosClient client) {
        super(response.resourceResponseWrapper, response.getProperties());
        this.responseWrapper = response;
        if (responseWrapper.getDatabase() != null) {
            this.database = BridgeInternal.createCosmosDatabase(responseWrapper.getDatabase().getId(), client,
                responseWrapper.getDatabase());
        } else {
            this.database = null;
        }
    }

    /**
     * Gets the CosmosAsyncDatabase object
     *
     * @return {@link CosmosDatabase}
     */
    public CosmosDatabase getDatabase() {
        return database;
    }

    /**
     * Gets the cosmos database properties
     *
     * @return the cosmos database properties
     */
    public CosmosDatabaseProperties getProperties() {
        return responseWrapper.getProperties();
    }

    /**
     * Gets the Max Quota.
     *
     * @return the database quota.
     */
    public long getDatabaseQuota() {
        return responseWrapper.getDatabaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current database usage.
     */
    public long getDatabaseUsage() {
        return responseWrapper.getDatabaseUsage();
    }

}
