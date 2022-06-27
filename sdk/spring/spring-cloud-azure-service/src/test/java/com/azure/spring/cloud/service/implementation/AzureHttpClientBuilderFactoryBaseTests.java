// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation;

import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClientProvider;
import com.azure.spring.cloud.service.implementation.core.http.TestPerCallHttpPipelinePolicy;
import com.azure.spring.cloud.service.implementation.core.http.TestPerRetryHttpPipelinePolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class AzureHttpClientBuilderFactoryBaseTests<B, P extends AzureHttpSdkProperties, F extends AbstractAzureHttpClientBuilderFactory<B>>
    extends AzureServiceClientBuilderFactoryBaseTests<B, P, F> {

    protected abstract HttpClientOptions getHttpClientOptions(F builderFactory);
    protected abstract List<HttpPipelinePolicy> getHttpPipelinePolicies(F builderFactory);
    protected abstract void verifyHttpClientCalled(B builder, VerificationMode mode);
    protected abstract void verifyRetryOptionsCalled(B builder, P properties, VerificationMode mode);

    @Test
    void testHttpClientConfigured() {
        P properties = createMinimalServiceProperties();

        final F builderFactory = createClientBuilderFactoryWithMockBuilder(properties);

        builderFactory.setHttpClientProvider(new TestHttpClientProvider());

        final B builder = builderFactory.build();
        buildClient(builder);

        verifyHttpClientCalled(builder, times(1));
    }

    @Test
    void testDefaultHttpPipelinePoliciesConfigured() {
        P properties = createMinimalServiceProperties();

        final F builderFactory = createClientBuilderFactoryWithMockBuilder(properties);

        TestPerCallHttpPipelinePolicy perCallHttpPipelinePolicy = new TestPerCallHttpPipelinePolicy();
        TestPerRetryHttpPipelinePolicy perRetryHttpPipelinePolicy = new TestPerRetryHttpPipelinePolicy();
        builderFactory.addHttpPipelinePolicy(perCallHttpPipelinePolicy);
        builderFactory.addHttpPipelinePolicy(perRetryHttpPipelinePolicy);


        final B builder = builderFactory.build();
        buildClient(builder);

        List<HttpPipelinePolicy> httpPipelinePolicies = getHttpPipelinePolicies(builderFactory);
        Assertions.assertEquals(2, httpPipelinePolicies.size());
        Assertions.assertEquals(perCallHttpPipelinePolicy, httpPipelinePolicies.get(0));
        Assertions.assertEquals(perRetryHttpPipelinePolicy, httpPipelinePolicies.get(1));
    }

    @Test
    void proxyPropertiesConfigured() {
        P properties = createMinimalServiceProperties();
        HttpProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");
        proxyProperties.setNonProxyHosts("localhost");

        final F builderFactory = createClientBuilderFactoryWithMockBuilder(properties);

        HttpClientProvider mockHttpClientProvider = mock(HttpClientProvider.class);
        builderFactory.setHttpClientProvider(mockHttpClientProvider);

        B builder = builderFactory.build();
        buildClient(builder);

        HttpClientOptions httpClientOptions = getHttpClientOptions(builderFactory);
        ProxyOptions proxyOptions = httpClientOptions.getProxyOptions();
        Assertions.assertEquals("localhost", proxyOptions.getAddress().getHostName());
        Assertions.assertEquals(8080, proxyOptions.getAddress().getPort());
        Assertions.assertEquals(ProxyOptions.Type.HTTP, proxyOptions.getType());
        Assertions.assertEquals("(localhost)", proxyOptions.getNonProxyHosts());

        verify(mockHttpClientProvider, times(1)).createInstance(eq(httpClientOptions));
    }

    @Test
    void fixedRetrySettingsCanWork() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setMaxRetries(2);
        properties.getRetry().getFixed().setDelay(Duration.ofSeconds(2));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);
        verifyRetryOptionsCalled(builder, properties, times(1));

    }

    @Test
    void exponentialRetrySettingsCanWork() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);
        properties.getRetry().getExponential().setMaxRetries(2);
        properties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        properties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);
        verifyRetryOptionsCalled(builder, properties, times(1));
    }

}
