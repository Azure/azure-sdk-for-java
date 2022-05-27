// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class ConfigurationClientBuilderFactoryTests extends
    AzureHttpClientBuilderFactoryBaseTests<
        ConfigurationClientBuilder,
        AzureAppConfigurationTestProperties,
        ConfigurationClientBuilderFactoryTests.ConfigurationClientBuilderFactoryExt> {

    private static final String ENDPOINT = "https://abc.azconfig.io";


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
    protected AzureAppConfigurationTestProperties createMinimalServiceProperties() {
        return new AzureAppConfigurationTestProperties();
    }

    @Override
    protected ConfigurationClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(
        AzureAppConfigurationTestProperties properties) {
        return new ConfigurationClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(ConfigurationClientBuilder builder) {
        builder.buildClient();
    }

    @Override
    protected void verifyCredentialCalled(ConfigurationClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected void verifyRetryOptionsCalled(ConfigurationClientBuilder builder,
                                            AzureAppConfigurationTestProperties properties,
                                            VerificationMode mode) {
        // TODO (xiada): change this when we use the retryOptions instead of the retryPolicy function.
        verify(builder, mode).retryPolicy(any(RetryPolicy.class));
    }

    @Override
    protected void verifyHttpClientCalled(ConfigurationClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(ConfigurationClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(ConfigurationClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    static class ConfigurationClientBuilderFactoryExt extends ConfigurationClientBuilderFactory {

        ConfigurationClientBuilderFactoryExt(AzureAppConfigurationTestProperties configurationProperties) {
            super(configurationProperties);
        }

        @Override
        protected ConfigurationClientBuilder createBuilderInstance() {
            return mock(ConfigurationClientBuilder.class);
        }

        @Override
        public HttpClientOptions getHttpClientOptions() {
            return super.getHttpClientOptions();
        }

        @Override
        public List<HttpPipelinePolicy> getHttpPipelinePolicies() {
            return super.getHttpPipelinePolicies();
        }
    }
}

