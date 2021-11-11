// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.credential;

import com.azure.core.http.ProxyOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import com.azure.spring.service.AzureServiceClientBuilderFactoryTestBase;
import com.azure.spring.service.core.http.TestHttpClient;
import com.azure.spring.service.core.http.TestHttpClientProvider;
import com.azure.spring.service.core.properties.TestAzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AzureDefaultAzureCredentialBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<DefaultAzureCredentialBuilder,
    AzureProperties, AzureDefaultAzureCredentialBuilderFactory> {

    @Override
    protected TestAzureGlobalProperties createMinimalServiceProperties() {
        return new TestAzureGlobalProperties();
    }

    @Test
    void testAuthorityHostAndExecutorServiceConfigured() {
        AzureProperties properties = createMinimalServiceProperties();
        final DefaultAzureCredentialBuilder builder = mock(DefaultAzureCredentialBuilder.class);
        ThreadPoolExecutor executor = getThreadPoolExecutor();
        AzureDefaultAzureCredentialBuilderFactory factory = new AzureDefaultAzureCredentialBuilderFactory(
            properties, builder, executor);
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(builder, times(1)).executorService(executor);

        String aadEndpoint = properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();
        verify(builder, times(1)).authorityHost(aadEndpoint);
    }

    @Test
    void testHttpClientConfigured() {
        AzureProperties properties = createMinimalServiceProperties();
        final DefaultAzureCredentialBuilder builder = mock(DefaultAzureCredentialBuilder.class);
        ThreadPoolExecutor executor = getThreadPoolExecutor();
        AzureDefaultAzureCredentialBuilderFactory factory = new AzureDefaultAzureCredentialBuilderFactory(
            properties, builder, executor);
        factory.setHttpClientProvider(new TestHttpClientProvider());
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(builder, times(1)).httpClient(any(TestHttpClient.class));
    }

    @Test
    void testRetryOptionsConfigured() {
        TestAzureGlobalProperties properties = createMinimalServiceProperties();
        RetryProperties retryProperties = properties.getRetry();
        retryProperties.setMaxAttempts(3);
        Duration duration = Duration.ofMillis(3);
        retryProperties.setTimeout(duration);
        final DefaultAzureCredentialBuilder builder = mock(DefaultAzureCredentialBuilder.class);
        ThreadPoolExecutor executor = getThreadPoolExecutor();
        AzureDefaultAzureCredentialBuilderFactory factory = new AzureDefaultAzureCredentialBuilderFactory(
            properties, builder, executor);
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(builder, times(1)).maxRetry(3);
    }

    @Test
    void testProxyOptionsConfigured() {
        TestAzureGlobalProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        final DefaultAzureCredentialBuilder builder = mock(DefaultAzureCredentialBuilder.class);
        ThreadPoolExecutor executor = getThreadPoolExecutor();
        AzureDefaultAzureCredentialBuilderFactory factory = new AzureDefaultAzureCredentialBuilderFactory(
            properties, builder, executor);
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(builder, times(1)).proxyOptions(any(ProxyOptions.class));
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.initialize();
        return taskExecutor.getThreadPoolExecutor();
    }
}
