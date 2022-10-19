// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.time.Duration;

import static org.mockito.Mockito.times;

public abstract class AzureAmqpClientBuilderFactoryBaseTests<B, P extends AzureAmqpSdkProperties, F extends AbstractAzureAmqpClientBuilderFactory<B>>
    extends AzureServiceClientBuilderFactoryBaseTests<B, P, F> {

    protected abstract void verifyRetryOptionsCalled(B builder, VerificationMode mode);
    protected abstract void verifyProxyOptionsCalled(B builder, VerificationMode mode);
    protected abstract void verifyTransportTypeCalled(B builder, VerificationMode mode);

    @Test
    void proxyPropertiesConfigured() {
        P properties = createMinimalServiceProperties();
        AmqpProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");
        proxyProperties.setAuthenticationType("basic");

        final F builderFactory = createClientBuilderFactoryWithMockBuilder(properties);

        B builder = builderFactory.build();
        buildClient(builder);

        verifyProxyOptionsCalled(builder, times(1));
    }

    @Test
    void fixedRetrySettingsCanWork() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setMaxRetries(2);
        properties.getRetry().setTryTimeout(Duration.ofSeconds(3));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);
        verifyRetryOptionsCalled(builder, times(1));

    }

    @Test
    void exponentialRetrySettingsCanWork() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);
        properties.getRetry().getExponential().setMaxRetries(2);
        properties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        properties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        properties.getRetry().setTryTimeout(Duration.ofSeconds(5));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);
        verifyRetryOptionsCalled(builder, times(1));
    }

    @Test
    void transportTypeConfigured() {
        P properties = createMinimalServiceProperties();
        properties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        final F builderFactory = createClientBuilderFactoryWithMockBuilder(properties);
        final B builder = builderFactory.build();
        buildClient(builder);

        verifyTransportTypeCalled(builder, times(1));
    }

}
