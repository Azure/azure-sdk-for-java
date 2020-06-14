// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.microsoft.azure.spring.data.cosmosdb.CosmosDbFactory;
import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CosmosTemplateUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectNullDbFactory() {
        final CosmosDBConfig dbConfig = CosmosDBConfig.cosmosDBConfigbuilder("", "", TestConstants.DB_NAME).build();
        final CosmosDbFactory cosmosDbFactory = new CosmosDbFactory(dbConfig);

        new CosmosTemplate(cosmosDbFactory, null, TestConstants.DB_NAME);
    }
}
