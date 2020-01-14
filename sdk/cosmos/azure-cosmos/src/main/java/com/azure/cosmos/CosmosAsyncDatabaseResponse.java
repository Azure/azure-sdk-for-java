// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.ResourceResponse;
import org.apache.commons.lang3.StringUtils;

public class CosmosAsyncDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties> {
    private final CosmosAsyncDatabase database;

    CosmosAsyncDatabaseResponse(ResourceResponse<Database> response, CosmosAsyncClient client) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
            database = null;
        } else {
            CosmosDatabaseProperties props = new CosmosDatabaseProperties(bodyAsString, null);
            super.setProperties(props);
            database = new CosmosAsyncDatabase(props.getId(), client);
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
    public long getDatabaseQuota() {
        return resourceResponseWrapper.getDatabaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current getDatabase usage.
     */
    public long getDatabaseUsage() {
        return resourceResponseWrapper.getDatabaseUsage();
    }

}
