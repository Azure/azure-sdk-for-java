// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;

/**
 * Example for extending CosmosFactory for Mutli-Tenancy at the database level
 */
// BEGIN: readme-sample-MultiTenantDBCosmosFactory
public class MultiTenantDBCosmosFactory extends CosmosFactory {

    private String tenantId;

    /**
     * Validate config and initialization
     *
     * @param cosmosAsyncClient cosmosAsyncClient
     * @param databaseName      databaseName
     */
    public MultiTenantDBCosmosFactory(CosmosAsyncClient cosmosAsyncClient, String databaseName) {
        super(cosmosAsyncClient, databaseName);

        this.tenantId = databaseName;
    }

    @Override
    public String getDatabaseName() {
        return this.getCosmosAsyncClient().getDatabase(this.tenantId).toString();
    }
}
// END: readme-sample-MultiTenantDBCosmosFactory
