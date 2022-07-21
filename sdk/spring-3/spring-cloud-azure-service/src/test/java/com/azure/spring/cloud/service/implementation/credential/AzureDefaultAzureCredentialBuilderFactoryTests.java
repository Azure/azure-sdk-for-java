// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AzureDefaultAzureCredentialBuilderFactoryTests extends
    AzureHttpClientBuilderFactoryBaseTests<
        DefaultAzureCredentialBuilder,
        AzureIdentityTestProperties,
        AzureDefaultAzureCredentialBuilderFactoryTests.DefaultAzureCredentialBuilderFactoryExt> {

    @Test
    void executorServiceConfigured() {
        AzureIdentityTestProperties properties = createMinimalServiceProperties();
        DefaultAzureCredentialBuilderFactoryExt factory = new DefaultAzureCredentialBuilderFactoryExt(properties);
        factory.setExecutorService(getThreadPoolExecutor());

        DefaultAzureCredentialBuilder builder = factory.build();
        verify(builder, times(1)).executorService(any(ExecutorService.class));
    }

    @Override
    protected AzureIdentityTestProperties createMinimalServiceProperties() {
        return new AzureIdentityTestProperties();
    }

    @Override
    protected DefaultAzureCredentialBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureIdentityTestProperties properties) {
        return new DefaultAzureCredentialBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(DefaultAzureCredentialBuilder builder) {
        builder.build();
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureIdentityTestProperties properties = new AzureIdentityTestProperties();
        properties.getProfile().setTenantId("test-tenant-id");
        properties.getProfile().getEnvironment().setActiveDirectoryEndpoint("test-authority-host");
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setManagedIdentityEnabled(true);

        DefaultAzureCredentialBuilderFactoryExt factory = new DefaultAzureCredentialBuilderFactoryExt(properties);
        DefaultAzureCredentialBuilder builder = factory.build();

        verify(builder, times(1)).tenantId("test-tenant-id");
        verify(builder, times(1)).authorityHost("test-authority-host");
        verify(builder, times(1)).managedIdentityClientId("test-client-id");
    }

    @Override
    protected void verifyCredentialCalled(DefaultAzureCredentialBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        // do nothing
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(DefaultAzureCredentialBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(DefaultAzureCredentialBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    @Override
    protected void verifyHttpClientCalled(DefaultAzureCredentialBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(HttpClient.class));
    }

    @Override
    protected void verifyRetryOptionsCalled(DefaultAzureCredentialBuilder builder, AzureIdentityTestProperties properties, VerificationMode mode) {
        RetryProperties retry = properties.getRetry();
        Integer maxRetries = RetryOptionsProvider.RetryMode.EXPONENTIAL.equals(retry.getMode())
            ? retry.getExponential().getMaxRetries() : retry.getFixed().getMaxRetries();
        verify(builder, mode).maxRetry(maxRetries);
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.initialize();
        return taskExecutor.getThreadPoolExecutor();
    }

    static class DefaultAzureCredentialBuilderFactoryExt extends DefaultAzureCredentialBuilderFactory {

        DefaultAzureCredentialBuilderFactoryExt(AzureProperties properties) {
            super(properties);
        }

        @Override
        public DefaultAzureCredentialBuilder createBuilderInstance() {
            return mock(DefaultAzureCredentialBuilder.class);
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
