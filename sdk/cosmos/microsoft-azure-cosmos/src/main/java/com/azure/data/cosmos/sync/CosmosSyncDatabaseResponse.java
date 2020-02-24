// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosDatabaseProperties;
import com.azure.data.cosmos.CosmosDatabaseResponse;

public class CosmosSyncDatabaseResponse extends CosmosSyncResponse {
    private final CosmosDatabaseResponse responseWrapper;
    private final CosmosSyncDatabase database;

    CosmosSyncDatabaseResponse(CosmosDatabaseResponse response, CosmosSyncClient client) {
        super(response);
        this.responseWrapper = response;
        if (responseWrapper.database() != null) {
            this.database = new CosmosSyncDatabase(responseWrapper.database().id(), client, responseWrapper.database());
        } else {
            this.database = null;
        }
    }

    /**
     * Gets the CosmosDatabase object
     *
     * @return {@link CosmosSyncDatabase}
     */
    public CosmosSyncDatabase database() {
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
