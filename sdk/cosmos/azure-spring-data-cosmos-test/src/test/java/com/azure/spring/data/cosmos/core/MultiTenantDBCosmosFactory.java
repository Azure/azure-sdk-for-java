// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;

/**
 * Example for extending CosmosFactory for Mutli-Tenancy at the database level
 */
public class MultiTenantDBCosmosFactory extends CosmosFactory {

    public String manuallySetDatabaseName;

    /**
     * Validate config and initialization
     *
     * @param cosmosAsyncClient cosmosAsyncClient
     * @param databaseName      databaseName
     */
    public MultiTenantDBCosmosFactory(CosmosAsyncClient cosmosAsyncClient, String databaseName) {
        super(cosmosAsyncClient, databaseName);

        this.databaseName = databaseName;
    }

    @Override
    public String getDatabaseName() {
        return this.manuallySetDatabaseName;
    }
}
