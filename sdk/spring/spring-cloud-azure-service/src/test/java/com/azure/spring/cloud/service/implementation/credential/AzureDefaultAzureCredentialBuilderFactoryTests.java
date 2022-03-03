// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.credential;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.http.DefaultHttpProvider;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClientProvider;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureDefaultAzureCredentialBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<DefaultAzureCredentialBuilder,
    AzureProperties, DefaultAzureCredentialBuilderFactory> {

    @Override
    protected AzureGlobalTestProperties createMinimalServiceProperties() {
        return new AzureGlobalTestProperties();
    }

    @Test
    void authorityHostAndExecutorServiceConfigured() {
        AzureProperties properties = createMinimalServiceProperties();

        DefaultAzureCredentialBuilderFactoryExt factory = new DefaultAzureCredentialBuilderFactoryExt(properties);
        ThreadPoolExecutor executor = getThreadPoolExecutor();
        factory.setExecutorService(executor);

        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        DefaultAzureCredentialBuilder builder = factory.getBuilder();
        verify(builder, times(1)).executorService(executor);

        String aadEndpoint = properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();
        verify(builder, times(1)).authorityHost(aadEndpoint);
    }

    @Test
    void httpClientConfigured() {
        AzureProperties properties = createMinimalServiceProperties();
        DefaultAzureCredentialBuilderFactoryExt factory = new DefaultAzureCredentialBuilderFactoryExt(properties);
        factory.setExecutorService(getThreadPoolExecutor());
        factory.setHttpClientProvider(new TestHttpClientProvider());
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(factory.getBuilder(), times(1)).httpClient(any(TestHttpClient.class));
    }

    @Test
    void retryOptionsConfigured() {
        AzureGlobalTestProperties properties = createMinimalServiceProperties();
        RetryProperties retryProperties = properties.getRetry();
        retryProperties.setMaxRetries(3);
        Duration duration = Duration.ofMillis(3);

        DefaultAzureCredentialBuilderFactoryExt factory = new DefaultAzureCredentialBuilderFactoryExt(properties);
        factory.setExecutorService(getThreadPoolExecutor());
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(factory.getBuilder(), times(1)).maxRetry(3);
    }

    @Test
    void proxyOptionsConfigured() {
        AzureGlobalTestProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        DefaultAzureCredentialBuilderFactoryProxyExt factory = new DefaultAzureCredentialBuilderFactoryProxyExt(properties);

        DefaultAzureCredentialBuilder builder = factory.getBuilder();
        HttpClientProvider defaultHttpClientProvider = factory.getDefaultHttpClientProvider();
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(builder, times(1)).httpClient(any(HttpClient.class));
        verify(defaultHttpClientProvider, times(1)).createInstance(any(HttpClientOptions.class));
    }

    @Test
    void executorServiceConfigured() {
        AzureGlobalTestProperties properties = createMinimalServiceProperties();
        DefaultAzureCredentialBuilderFactoryExt factory = new DefaultAzureCredentialBuilderFactoryExt(properties);
        factory.setExecutorService(getThreadPoolExecutor());

        DefaultAzureCredentialBuilder builder = factory.getBuilder();
        DefaultAzureCredentialBuilder credentialBuilder = factory.build();
        verify(builder, times(1)).executorService(any(ExecutorService.class));
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.initialize();
        return taskExecutor.getThreadPoolExecutor();
    }

    static class DefaultAzureCredentialBuilderFactoryExt extends DefaultAzureCredentialBuilderFactory {

        private final DefaultAzureCredentialBuilder builder = mock(DefaultAzureCredentialBuilder.class);

        DefaultAzureCredentialBuilderFactoryExt(AzureProperties properties) {
            super(properties);
        }

        @Override
        public DefaultAzureCredentialBuilder createBuilderInstance() {
            return builder;
        }

        DefaultAzureCredentialBuilder getBuilder() {
            return builder;
        }
    }

    static class DefaultAzureCredentialBuilderFactoryProxyExt extends DefaultAzureCredentialBuilderFactoryExt {

        private HttpClientProvider httpClientProvider = mock(DefaultHttpProvider.class);

        DefaultAzureCredentialBuilderFactoryProxyExt(AzureProperties properties) {
            super(properties);

            HttpClient httpClient = mock(HttpClient.class);
            when(this.httpClientProvider.createInstance(any(HttpClientOptions.class))).thenReturn(httpClient);
        }

        @Override
        protected HttpClientProvider getHttpClientProvider() {
            return httpClientProvider;
        }

        HttpClientProvider getDefaultHttpClientProvider() {
            return getHttpClientProvider();
        }
    }
}
