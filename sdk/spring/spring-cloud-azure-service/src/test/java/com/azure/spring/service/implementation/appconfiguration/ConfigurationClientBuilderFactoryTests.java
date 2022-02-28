// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.appconfiguration;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.service.implementation.core.http.TestHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
class ConfigurationClientBuilderFactoryTests extends AzureHttpClientBuilderFactoryBaseTests<ConfigurationClientBuilder,
    AzureAppConfigurationTestProperties, ConfigurationClientBuilderFactory> {

    private static final String ENDPOINT = "https://abc.azconfig.io";


    @Override
    protected AzureAppConfigurationTestProperties createMinimalServiceProperties() {
        return new AzureAppConfigurationTestProperties();
    }

    @Test
    void testServiceVersionConfigured() {
        AzureAppConfigurationTestProperties properties = new AzureAppConfigurationTestProperties();
        properties.setServiceVersion(ConfigurationServiceVersion.V1_0);

        final ConfigurationClientBuilderFactoryExt factoryExt = new ConfigurationClientBuilderFactoryExt(properties);
        final ConfigurationClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).serviceVersion(ConfigurationServiceVersion.V1_0);
    }

    @Test
    void testEndpointConfigured() {
        AzureAppConfigurationTestProperties properties = new AzureAppConfigurationTestProperties();
        properties.setEndpoint(ENDPOINT);

        final ConfigurationClientBuilderFactoryExt factoryExt = new ConfigurationClientBuilderFactoryExt(properties);
        final ConfigurationClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).endpoint(ENDPOINT);
    }

    @Override
    protected ConfigurationClientBuilderFactory getClientBuilderFactoryWithMockBuilder(AzureAppConfigurationTestProperties properties) {
        return new ConfigurationClientBuilderFactoryExt(properties);
    }

    @Override
    protected void verifyHttpClientCalled(ConfigurationClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyHttpPipelinePolicyAdded(ConfigurationClientBuilder builder, HttpPipelinePolicy policy, VerificationMode mode) {
        verify(builder, mode).addPolicy(policy);
    }


    static class ConfigurationClientBuilderFactoryExt extends ConfigurationClientBuilderFactory {

        ConfigurationClientBuilderFactoryExt(AzureAppConfigurationTestProperties configurationProperties) {
            super(configurationProperties);
        }

        @Override
        protected ConfigurationClientBuilder createBuilderInstance() {
            return mock(ConfigurationClientBuilder.class);
        }
    }
}

