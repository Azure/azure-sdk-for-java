// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.spring.core.properties.retry.RetryProperties;
import com.azure.spring.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CosmosClientBuilderFactoryTestsTests extends AzureServiceClientBuilderFactoryBaseTests<CosmosClientBuilder,
    TestAzureCosmosHttpProperties, CosmosClientBuilderFactory> {

    private static final Configuration NOOP = new Configuration();
    private static final String ENDPOINT = "https://test.documents.azure.com:443/";

    @Test
    void azureKeyCredentialConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        properties.setKey("key");
        final CosmosClientBuilder builder = new CosmosClientBuilderFactoryExt(properties).build(NOOP);
        CosmosClient cosmosClient = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureKeyCredential.class));
    }

    @Test
    void tokenCredentialConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");

        final CosmosClientBuilder builder = new CosmosClientBuilderFactoryExt(properties).build(NOOP);
        final CosmosClient cosmosClient = builder.buildClient();

        verify(builder, times(1)).credential(any(ClientSecretCredential.class));
    }

    @Test
    void clientCertificateTokenCredentialConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");

        final CosmosClientBuilder builder = new CosmosClientBuilderFactoryExt(properties).build(NOOP);
        final CosmosClient cosmosClient = builder.buildClient();
        verify(builder, times(1)).credential(any(ClientCertificateCredential.class));
    }

    @Test
    void gatewayConnectionModeConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.GATEWAY);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build(NOOP);
        verify(builder, times(1)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(0)).directMode(any(DirectConnectionConfig.class));
    }

    @Test
    void directConnectionModeConfigured() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        properties.setConnectionMode(ConnectionMode.DIRECT);
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build(NOOP);
        verify(builder, times(0)).gatewayMode(any(GatewayConnectionConfig.class));
        verify(builder, times(1)).directMode(any(DirectConnectionConfig.class), any(GatewayConnectionConfig.class));
    }

    @Test
    void throttlingRetryOptionsConfiguredRetry() {
        TestAzureCosmosHttpProperties properties = createMinimalServiceProperties();
        RetryProperties retryProperties = properties.getRetry();
        retryProperties.setTimeout(Duration.ofMillis(1000));
        final CosmosClientBuilderFactoryExt factoryExt = new CosmosClientBuilderFactoryExt(properties);
        final CosmosClientBuilder builder = factoryExt.build(NOOP);
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
