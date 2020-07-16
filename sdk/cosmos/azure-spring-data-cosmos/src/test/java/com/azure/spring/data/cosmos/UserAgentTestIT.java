// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.common.PropertyLoader;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;

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
        Assertions.assertThat(factory.getConfig().getConnectionPolicy().userAgentSuffix()).contains(TEST_VERSION);
    }

}
