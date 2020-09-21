// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.TestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CosmosTemplateUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectNullDbFactory() {
        CosmosAsyncClient client = CosmosFactory.createCosmosAsyncClient(new CosmosClientBuilder()
            .endpoint("")
            .key(""));
        new CosmosFactory(client, TestConstants.DB_NAME);
    }
}
