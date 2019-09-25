// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosAsyncDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties>{
    private CosmosAsyncDatabase database;

    CosmosAsyncDatabaseResponse(ResourceResponse<Database> response, CosmosAsyncClient client) {
        super(response);
        if(response.getResource() == null){
            super.properties(null);
        }else{
            super.properties(new CosmosDatabaseProperties(response));
            database = new CosmosAsyncDatabase(this.properties().id(), client);
        }
    }

    /**
     * Gets the CosmosAsyncDatabase object
     *
     * @return {@link CosmosAsyncDatabase}
     */
    public CosmosAsyncDatabase database() {
        return database;
    }

    /**
     * Gets the cosmos database properties
     *
     * @return the cosmos database properties
     */
    public CosmosDatabaseProperties properties() {
        return this.properties();
    }

    /**
     * Gets the Max Quota.
     *
     * @return the database quota.
     */
    public long databaseQuota(){
        return resourceResponseWrapper.getDatabaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current database usage.
     */
    public long databaseUsage(){
        return resourceResponseWrapper.getDatabaseUsage();
    }

}
