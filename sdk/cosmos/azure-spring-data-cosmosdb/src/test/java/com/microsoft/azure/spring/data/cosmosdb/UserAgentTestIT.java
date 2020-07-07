// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.common.PropertyLoader;
import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore("Cannot use fake uri and key with CosmosDbFactory as it tries the connection on new creation."
    + "At the same time, cannot use mockito with real values, because it won't prepare PropertyLoader class for mocking")
@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyLoader.class)
@PropertySource(value = {"classpath:application.properties"})
public class UserAgentTestIT {

    private static final String TEST_VERSION = "1.0.0-FOR-TEST";

    @Test
    public void testUserAgentSuffixAppended() {
        PowerMockito.mockStatic(PropertyLoader.class);
        Mockito.doReturn(TEST_VERSION).when(PropertyLoader.getProjectVersion());
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder(TestConstants.COSMOSDB_FAKE_HOST,
            TestConstants.COSMOSDB_FAKE_KEY, TestConstants.DB_NAME).build();
        final CosmosDbFactory factory = new CosmosDbFactory(dbConfig);
        factory.getCosmosClient();
        assertThat(factory.getConfig().getConnectionPolicy().userAgentSuffix()).contains(TEST_VERSION);
    }

}
