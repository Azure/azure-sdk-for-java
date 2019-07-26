// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties>{
    private CosmosDatabase database;

    CosmosDatabaseResponse(ResourceResponse<Database> response, CosmosClient client) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosDatabaseProperties(response));
            database = new CosmosDatabase(resourceSettings().id(), client);
        }
    }

    /**
     * Gets the CosmosDatabase object
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
        return resourceSettings();
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
