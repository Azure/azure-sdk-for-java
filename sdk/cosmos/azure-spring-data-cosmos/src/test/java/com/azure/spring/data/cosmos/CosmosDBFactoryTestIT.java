// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosDBFactoryTestIT {

    @Value("${cosmosdb.uri:}")
    private String cosmosDbUri;

    @Value("${cosmosdb.key:}")
    private String cosmosDbKey;

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKey() {
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder()
                                                      .database(TestConstants.DB_NAME)
                                                      .cosmosClientBuilder(new CosmosClientBuilder()
                                                          .endpoint(TestConstants.COSMOSDB_FAKE_HOST)
                                                          .key(""))
                                                      .build();
        new CosmosDBFactory(dbConfig).getCosmosSyncClient();
    }

    @Test
    public void testInvalidEndpoint() {
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder()
                                                      .database(TestConstants.DB_NAME)
                                                      .cosmosClientBuilder(new CosmosClientBuilder()
                                                          .endpoint(TestConstants.COSMOSDB_FAKE_HOST)
                                                          .key(TestConstants.COSMOSDB_FAKE_KEY))
                                                      .build();
        final CosmosDBFactory factory = new CosmosDBFactory(dbConfig);

        assertThat(factory).isNotNull();
    }

    @Test
    public void testConnectionPolicyUserAgentKept() throws IllegalAccessException {
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder()
                                                      .database(TestConstants.DB_NAME)
                                                      .cosmosClientBuilder(new CosmosClientBuilder()
                                                          .endpoint(cosmosDbUri)
                                                          .key(cosmosDbKey))
                                                      .build();

        new CosmosDBFactory(dbConfig).getCosmosAsyncClient();

        final String uaSuffix = getUserAgentSuffixValue(dbConfig.getCosmosClientBuilder());
        assertThat(uaSuffix).contains("spring-data");
    }

    private String getUserAgentSuffixValue(CosmosClientBuilder cosmosClientBuilder) throws IllegalAccessException {
        final Field userAgentSuffix = FieldUtils.getDeclaredField(CosmosClientBuilder.class,
            "userAgentSuffix", true);
        return (String)userAgentSuffix.get(cosmosClientBuilder);
    }
}
