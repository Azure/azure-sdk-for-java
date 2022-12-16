// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;

/**
 * Example for extending CosmosFactory for Mutli-Tenancy at the database level
 */
// BEGIN: readme-sample-MultiTenantDBCosmosFactory
public class MultiTenantDBCosmosFactory extends CosmosFactory {

    private String manuallySetDatabaseName;

    /**
     * Validate config and initialization
     *
     * @param cosmosAsyncClient cosmosAsyncClient
     * @param databaseName      databaseName
     */
    public MultiTenantDBCosmosFactory(CosmosAsyncClient cosmosAsyncClient, String databaseName) {
        super(cosmosAsyncClient, databaseName);

        this.manuallySetDatabaseName = databaseName;
    }

    @Override
    public String getDatabaseName() {
        return this.manuallySetDatabaseName;
    }

    public void setManuallySetDatabaseName(String manuallySetDatabaseName) {
        this.manuallySetDatabaseName = manuallySetDatabaseName;
    }

    public String getManuallySetDatabaseName() {
        return manuallySetDatabaseName;
    }
}
// END: readme-sample-MultiTenantDBCosmosFactory
