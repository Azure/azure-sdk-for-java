// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.PropertyLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CosmosFactoryUnitTest {

    @Test
    public void userAgentSpringDataCosmosSuffix() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder();
        try {
            Method updateCosmosClientBuilderWithUASuffix = CosmosFactory.class.getDeclaredMethod(
                "updateCosmosClientBuilderWithUASuffix", CosmosClientBuilder.class);

            //  static method invoke call
            updateCosmosClientBuilderWithUASuffix.setAccessible(true);
            updateCosmosClientBuilderWithUASuffix.invoke(null, cosmosClientBuilder);

            //  getUserAgentSuffix method from CosmosClientBuilder
            Method getUserAgentSuffix = CosmosClientBuilder.class.getDeclaredMethod("getUserAgentSuffix");
            getUserAgentSuffix.setAccessible(true);
            String userAgentSuffix = (String) getUserAgentSuffix.invoke(cosmosClientBuilder);
            assertThat(userAgentSuffix).contains(Constants.USER_AGENT_SUFFIX);
            assertThat(userAgentSuffix).contains(PropertyLoader.getProjectVersion());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
