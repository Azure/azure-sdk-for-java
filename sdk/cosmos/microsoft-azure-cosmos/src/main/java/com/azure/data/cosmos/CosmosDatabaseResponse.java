// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

public class CosmosDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties> {
    private final CosmosAsyncDatabaseResponse responseWrapper;
    private final CosmosDatabase database;

    CosmosDatabaseResponse(CosmosAsyncDatabaseResponse response, CosmosClient client) {
        super(response.properties());
        this.responseWrapper = response;
        if (responseWrapper.database() != null) {
            this.database = new CosmosDatabase(responseWrapper.database().id(), client, responseWrapper.database());
        } else {
            this.database = null;
        }
    }

    /**
     * Gets the CosmosAsyncDatabase object
     *
     * @return {@link CosmosDatabase}
     */
    public CosmosDatabase database() {
        return database;
    }

    /**
     * Gets the cosmos database properties
     *
     * @return the cosmos database properties
     */
    public CosmosDatabaseProperties properties() {
        return responseWrapper.properties();
    }

    /**
     * Gets the Max Quota.
     *
     * @return the database quota.
     */
    public long databaseQuota() {
        return responseWrapper.databaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current database usage.
     */
    public long databaseUsage() {
        return responseWrapper.databaseUsage();
    }

}
