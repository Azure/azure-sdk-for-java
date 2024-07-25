// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.AzureGenericServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonTestProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

abstract class AbstractServiceBusSubClientBuilderFactoryTests<B,
    P extends ServiceBusClientCommonTestProperties,
    F extends AbstractServiceBusSubClientBuilderFactory<B, ?>> extends AzureGenericServiceClientBuilderFactoryBaseTests<P, F> {

    abstract void verifyServicePropertiesConfigured();

    abstract void buildClient(B builder);

    protected String customEndpoint = "https://custom.endpoint.test";

    ServiceBusClientBuilderFactory clientBuilderFactory;
    ServiceBusClientBuilder clientBuilder;

    protected ServiceBusClientBuilder getSharedServiceBusClientBuilder(P properties) {
        if (this.clientBuilderFactory == null) {
            this.clientBuilderFactory = spy(new TestServiceBusClientBuilderFactory(properties));
            this.clientBuilder = this.clientBuilderFactory.build();
        }
        return this.clientBuilder;
    }

    @Test
    void fqdnConfigured() {
        verifyFqdnConfigured();
    }

    @Test
    void servicePropertiesConfigured() {
        verifyServicePropertiesConfigured();
    }

    @Test
    void minimalSettingsCanWork() {
        final F factory = factoryWithMinimalSettings();
        B builder = factory.build();
        buildClient(builder);
    }

    @Test
    void clientSecretTokenCredentialConfigured() {
        verifyClientSecretTokenCredentialConfigured();
    }

    @Test
    void clientCertificateTokenCredentialConfigured() {
        verifyClientCertificateCredentialConfigured();
    }

    @Test
    void usernamePasswordTokenCredentialConfigured() {
        verifyUsernamePasswordCredentialConfigured();
    }

    @Test
    void managedIdentityTokenCredentialConfigured() {
        verifyManagedIdentityCredentialConfigured();
    }

    @Test
    void proxyPropertiesConfigured() {
        verifyProxyPropertiesConfigured();
    }

    @Test
    void fixedRetrySettingsCanWork() {
        verifyFixedRetryPropertiesConfigured();
    }

    @Test
    void exponentialRetrySettingsCanWork() {
        exponentialRetryPropertiesConfigured();
    }

    @Test
    void transportTypeConfigured() {
        verifyTransportTypeConfigured();
    }

    @Test
    void connectionStringConfigured() {
        verifyConnectionConfigured();
    }

    private void verifyFqdnConfigured() {
        P properties = createMinimalServiceProperties();
        properties.setNamespace("another-namespace");
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);

        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    private void verifyClientSecretTokenCredentialConfigured() {
        final F factory = factoryWithClientSecretTokenCredentialConfigured(createMinimalServiceProperties());
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(ClientSecretCredential.class));
    }
    private void verifyClientCertificateCredentialConfigured() {
        final F factory = factoryWithClientCertificateTokenCredentialConfigured(createMinimalServiceProperties());
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(ClientCertificateCredential.class));
    }

    private void verifyUsernamePasswordCredentialConfigured() {
        final F factory = factoryWithUsernamePasswordTokenCredentialConfigured(createMinimalServiceProperties());
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(UsernamePasswordCredential.class));
    }

    private void verifyManagedIdentityCredentialConfigured() {
        final F factory = factoryWithManagedIdentityTokenCredentialConfigured(createMinimalServiceProperties());
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(ManagedIdentityCredential.class));
    }

    private void verifyProxyPropertiesConfigured() {
        P properties = createMinimalServiceProperties();
        AmqpProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");
        proxyProperties.setAuthenticationType("basic");

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).proxyOptions(any(ProxyOptions.class));
    }

    private void verifyFixedRetryPropertiesConfigured() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setMaxRetries(2);
        properties.getRetry().setTryTimeout(Duration.ofSeconds(3));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    private void exponentialRetryPropertiesConfigured() {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);
        properties.getRetry().getExponential().setMaxRetries(2);
        properties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        properties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        properties.getRetry().setTryTimeout(Duration.ofSeconds(5));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    private void verifyTransportTypeConfigured() {
        P properties = createMinimalServiceProperties();
        AmqpTransportType transportType = AmqpTransportType.AMQP_WEB_SOCKETS;
        properties.getClient().setTransportType(transportType);

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).transportType(transportType);
    }

    private void verifyConnectionConfigured() {
        P properties = createMinimalServiceProperties();
        properties.setConnectionString("test-connection-string");
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).connectionString("test-connection-string");
    }
}
