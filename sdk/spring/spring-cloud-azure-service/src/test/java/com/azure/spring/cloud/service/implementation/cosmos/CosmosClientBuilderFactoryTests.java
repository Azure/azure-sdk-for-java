// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CosmosClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<CosmosClientBuilder,
    AzureCosmosTestProperties, CosmosClientBuilderFactory> {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";

    @Test
    void azureKeyCredentialConfigured() {
        AzureCosmosTestProperties properties = createMinimalServiceProperties();
        properties.setKey("key");
        final CosmosClientBuilder builder = new CosmosClientBuilderFactoryExt(properties).build();
        CosmosClient cosmosClient = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureKeyCredential.class));
    }

    @Test
    void tokenCredentialConfigured() {
        AzureCosmosTestProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");

        final CosmosClientBuilder builder = new CosmosClientBuilderFactoryExt(properties).build();
        final CosmosClient cosmosClient = builder.buildClient();

        verify(builder, times(1)).credential(any(ClientSecretCredential.class));
    }

    @Test
    void clientCertificateTokenCredentialConfigured() {
        AzureCosmosTestProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");

        final CosmosClientBuilder builder = new CosmosClientBuilderFactoryExt(properties).build();
        final CosmosClient cosmosClient = builder.buildClient();
        verify(builder, times(1)).credential(any(ClientCertificateCredential.class));
    }

    @Test
    void gatewayConnectionModeConfigured() {
        AzureCosmosTestProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.GATEWAY);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(0)).directMode(any(DirectConnectionConfig.class));
    }

    @Test
    void directConnectionModeConfigured() {
        AzureCosmosTestProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.DIRECT);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(0)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(1)).directMode(any(DirectConnectionConfig.class), any(GatewayConnectionConfig.class));
    }

    @Test
    void throttlingRetryOptionsConfiguredRetry() {
        AzureCosmosTestProperties properties = createMinimalServiceProperties();
        properties.getThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(1);
        properties.getThrottlingRetryOptions().setMaxRetryWaitTime(Duration.ofSeconds(2));
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).throttlingRetryOptions(any(ThrottlingRetryOptions.class));
    }

    @Override
    protected AzureCosmosTestProperties createMinimalServiceProperties() {
        AzureCosmosTestProperties cosmosProperties = new AzureCosmosTestProperties();
        cosmosProperties.setEndpoint(ENDPOINT);
        return cosmosProperties;
    }

    static class CosmosClientBuilderFactoryExt extends CosmosClientBuilderFactory {

        CosmosClientBuilderFactoryExt(AzureCosmosTestProperties cosmosProperties) {
            super(cosmosProperties);
        }

        @Override
        protected CosmosClientBuilder createBuilderInstance() {
            return mock(CosmosClientBuilder.class);
        }
    }
}
