// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;

/**
 * Template class for cosmos db
 */
public class MultiTenantDBCosmosTemplate extends CosmosTemplate {

    public MultiTenantDBCosmosTemplate(CosmosFactory cosmosFactory, CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter, IsNewAwareAuditingHandler cosmosAuditingHandler) {
        super(cosmosFactory, cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);

        super.databaseName = cosmosFactory.getDatabaseName();
    }

    @Override
    public void setNameAndCreateDatabaseIfNotExists(String dbName) {
        super.setNameAndCreateDatabaseIfNotExists(dbName);
    }

    public String getDatabaseName() {
        return super.databaseName;
    }
}
