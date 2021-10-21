// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.spring.service.AzureServiceClientBuilderFactoryTestBase;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;

public class CosmosClientBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<CosmosClientBuilder,
    TestAzureCosmosProperties, CosmosClientBuilderFactory> {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";

    @Test
    void testGateConnectionModeConfigured() {
        TestAzureCosmosProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.GATEWAY);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(0)).directMode(any(DirectConnectionConfig.class));
    }

    @Test
    void testDirectConnectionModeConfigured() {
        TestAzureCosmosProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.DIRECT);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(1)).directMode(any(DirectConnectionConfig.class));
    }

    @Override
    protected TestAzureCosmosProperties createMinimalServiceProperties() {
        TestAzureCosmosProperties cosmosProperties = new TestAzureCosmosProperties();
        cosmosProperties.setEndpoint(ENDPOINT);
        return cosmosProperties;
    }

    static class CosmosClientBuilderFactoryExt extends CosmosClientBuilderFactory {

        CosmosClientBuilderFactoryExt(TestAzureCosmosProperties cosmosProperties) {
            super(cosmosProperties);
        }

        @Override
        protected CosmosClientBuilder createBuilderInstance() {
            return mock(CosmosClientBuilder.class);
        }
    }
}
