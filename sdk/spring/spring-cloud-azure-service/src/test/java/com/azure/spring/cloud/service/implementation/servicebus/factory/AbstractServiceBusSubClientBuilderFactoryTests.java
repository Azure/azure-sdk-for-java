// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.AzureGenericServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonTestProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

abstract class AbstractServiceBusSubClientBuilderFactoryTests<B,
    P extends ServiceBusClientCommonTestProperties,
    F extends AbstractServiceBusSubClientBuilderFactory<B, ?>> extends AzureGenericServiceClientBuilderFactoryBaseTests<P, F> {

    @Test
    void minimalSettingsCanWork() {
        final F factory = factoryWithMinimalSettings();
        B builder = factory.build();
    }

    @Test
    void clientSecretTokenCredentialConfigured() {
        final F factory = factoryWithClientSecretTokenCredentialConfigured();
        B builder = factory.build();

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(ClientSecretCredential.class));
    }

    @Test
    void clientCertificateTokenCredentialConfigured() {
        final F factory = factoryWithClientCertificateTokenCredentialConfigured();
        B builder = factory.build();

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(ClientCertificateCredential.class));
    }

    @Test
    void managedIdentityTokenCredentialConfigured() {
        final F factory = factoryWithManagedIdentityTokenCredentialConfigured();
        B builder = factory.build();

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(ManagedIdentityCredential.class));
    }

    @Test
    void proxyPropertiesConfigured() {
        P properties = createMinimalServiceProperties();
        AmqpProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");
        proxyProperties.setAuthenticationType("basic");

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);

        B builder = factory.build();

        verify(factory.getServiceBusClientBuilder(), times(1)).proxyOptions(any(ProxyOptions.class));
    }

    @Test
    void fixedRetrySettingsCanWork() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setMaxRetries(2);
        properties.getRetry().setTryTimeout(Duration.ofSeconds(3));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();

        verify(factory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
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

        verify(factory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    @Test
    void transportTypeConfigured() {
        P properties = createMinimalServiceProperties();
        AmqpTransportType transportType = AmqpTransportType.AMQP_WEB_SOCKETS;
        properties.getClient().setTransportType(transportType);
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        factory.build();
        verify(factory.getServiceBusClientBuilder(), times(1)).transportType(transportType);
    }

    @Test
    void azureSasCredentialConfigured() {
        P properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        factory.build();
        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(AzureSasCredential.class));
    }

    @Test
    void azureNamedKeyCredentialConfigured() {
        P properties = createMinimalServiceProperties();
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        factory.build();
        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(AzureNamedKeyCredential.class));
    }
}
