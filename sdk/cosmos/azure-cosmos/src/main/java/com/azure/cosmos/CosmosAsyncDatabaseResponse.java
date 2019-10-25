// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.internal.Database;
import com.azure.cosmos.internal.ResourceResponse;
import com.azure.cosmos.internal.Database;
import com.azure.cosmos.internal.ResourceResponse;

public class CosmosAsyncDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties>{
    private CosmosAsyncDatabase database;

    CosmosAsyncDatabaseResponse(ResourceResponse<Database> response, CosmosAsyncClient client) {
        super(response);
        if(response.getResource() == null){
            super.setProperties(null);
        }else{
            super.setProperties(new CosmosDatabaseProperties(response));
            database = new CosmosAsyncDatabase(this.getProperties().getId(), client);
        }
    }

    /**
     * Gets the CosmosAsyncDatabase object
     *
     * @return {@link CosmosAsyncDatabase}
     */
    public CosmosAsyncDatabase getDatabase() {
        return database;
    }

    /**
     * Gets the cosmos database properties
     *
     * @return the cosmos database properties
     */
    public CosmosDatabaseProperties getProperties() {
        return super.getProperties();
    }

    /**
     * Gets the Max Quota.
     *
     * @return the getDatabase quota.
     */
    public long getDatabaseQuota(){
        return resourceResponseWrapper.getDatabaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current getDatabase usage.
     */
    public long getDatabaseUsage(){
        return resourceResponseWrapper.getDatabaseUsage();
    }

}
