// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.spring.core.properties.retry.RetryProperties;
import com.azure.spring.service.AzureServiceClientBuilderFactoryTestBase;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CosmosClientBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<CosmosClientBuilder,
    TestAzureCosmosHttpProperties, CosmosClientBuilderFactory> {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";

    @Test
    void testGatewayConnectionModeConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.GATEWAY);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(0)).directMode(any(DirectConnectionConfig.class));
    }

    @Test
    void testDirectConnectionModeConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.DIRECT);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(0)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(1)).directMode(any(DirectConnectionConfig.class), any(GatewayConnectionConfig.class));
    }

    @Test
    void testThrottlingRetryOptionsConfiguredRetry() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        RetryProperties retryProperties = properties.getRetry();
        retryProperties.setTimeout(Duration.ofMillis(1000));
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).throttlingRetryOptions(any(ThrottlingRetryOptions.class));
    }

    @Override
    protected TestAzureCosmosHttpProperties createMinimalServiceProperties() {
        TestAzureCosmosHttpProperties cosmosProperties = new TestAzureCosmosHttpProperties();
        cosmosProperties.setEndpoint(ENDPOINT);
        return cosmosProperties;
    }

    static class CosmosClientBuilderFactoryExt extends CosmosClientBuilderFactory {

        CosmosClientBuilderFactoryExt(TestAzureCosmosHttpProperties cosmosProperties) {
            super(cosmosProperties);
        }

        @Override
        protected CosmosClientBuilder createBuilderInstance() {
            return mock(CosmosClientBuilder.class);
        }
    }
}
