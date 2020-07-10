// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.spring.data.cosmos.CosmosDbFactory;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CosmosTemplateUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectNullDbFactory() {
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder("", "", TestConstants.DB_NAME).build();
        final CosmosDbFactory cosmosDbFactory = new CosmosDbFactory(dbConfig);

        new CosmosTemplate(cosmosDbFactory, null, TestConstants.DB_NAME);
    }
}
