// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.exception.CosmosDBAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosDbFactoryTestIT {

    @Value("${cosmosdb.uri:}")
    private String cosmosDbUri;

    @Value("${cosmosdb.key:}")
    private String cosmosDbKey;

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKey() {
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder(TestConstants.COSMOSDB_FAKE_HOST,
            "", TestConstants.DB_NAME).build();
        new CosmosDbFactory(dbConfig);
    }

    @Test
    public void testInvalidEndpoint() {
        final CosmosDBConfig dbConfig =
                CosmosDBConfig.builder(TestConstants.COSMOSDB_FAKE_HOST, TestConstants.COSMOSDB_FAKE_KEY, TestConstants.DB_NAME).build();
        final CosmosDbFactory factory = new CosmosDbFactory(dbConfig);

        assertThat(factory).isNotNull();
    }

    @Test
    public void testConnectWithConnectionString() {
        final CosmosDBConfig dbConfig =
                CosmosDBConfig.builder(TestConstants.COSMOSDB_FAKE_CONNECTION_STRING, TestConstants.DB_NAME).build();
        final CosmosDbFactory factory = new CosmosDbFactory(dbConfig);

        assertThat(factory).isNotNull();
    }

    @Test(expected = CosmosDBAccessException.class)
    public void testInvalidConnectionString() {
        CosmosDBConfig.builder(TestConstants.COSMOSDB_INVALID_FAKE_CONNECTION_STRING, TestConstants.DB_NAME).build();
    }

    @Test
    public void testConnectionPolicyUserAgentKept() {
        final CosmosDBConfig dbConfig =
                CosmosDBConfig.builder(cosmosDbUri, cosmosDbKey, TestConstants.DB_NAME).build();
        final CosmosDbFactory factory = new CosmosDbFactory(dbConfig);
        factory.getCosmosClient();

        final String uaSuffix = factory.getConfig().getConnectionPolicy().userAgentSuffix();
        assertThat(uaSuffix).contains("spring-data");
    }
}
