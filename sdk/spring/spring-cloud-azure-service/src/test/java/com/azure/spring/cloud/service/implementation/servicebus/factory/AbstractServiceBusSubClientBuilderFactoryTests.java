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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusClientBuilderFactoryTests.CONNECTION_STRING_FORMAT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

abstract class AbstractServiceBusSubClientBuilderFactoryTests<B,
    P extends ServiceBusClientCommonTestProperties,
    F extends AbstractServiceBusSubClientBuilderFactory<B, ?>> extends AzureGenericServiceClientBuilderFactoryBaseTests<P, F> {

    abstract void verifyServicePropertiesConfigured(boolean isShareServiceClientBuilder);
    abstract void buildClient(B builder);

    protected String customEndpoint = "https://custom.endpoint.test";
    ServiceBusClientBuilderFactory sharedClientBuilderFactory;
    ServiceBusClientBuilder sharedClientBuilder;
    ServiceBusClientBuilder clientBuilder;

    protected ServiceBusClientBuilder getSharedServiceBusClientBuilder(P properties) {
        if (properties.isShareServiceBusClientBuilder()) {
            if (this.sharedClientBuilder == null && this.clientBuilder == null) {
                this.sharedClientBuilderFactory = spy(new TestServiceBusClientBuilderFactory(properties));
                this.sharedClientBuilder = this.sharedClientBuilderFactory.build();
            }
            return this.sharedClientBuilder;
        }
        return null;
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void fqdnConfigured(boolean isShareServiceClientBuilder) {
        verifyFqdnConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void servicePropertiesConfigured(boolean isShareServiceClientBuilder) {
        verifyServicePropertiesConfigured(isShareServiceClientBuilder);
    }

    @Test
    void minimalSettingsCanWork() {
        final F factory = factoryWithMinimalSettings();
        B builder = factory.build();
        buildClient(builder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void clientSecretTokenCredentialConfigured(boolean isShareServiceClientBuilder) {
        verifyClientSecretTokenCredentialConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void clientCertificateTokenCredentialConfigured(boolean isShareServiceClientBuilder) {
        verifyClientCertificateCredentialConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void usernamePasswordTokenCredentialConfigured(boolean isShareServiceClientBuilder) {
        verifyUsernamePasswordCredentialConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void managedIdentityTokenCredentialConfigured(boolean isShareServiceClientBuilder) {
        verifyManagedIdentityCredentialConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void proxyPropertiesConfigured(boolean isShareServiceClientBuilder) {
        verifyProxyPropertiesConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void fixedRetrySettingsCanWork(boolean isShareServiceClientBuilder) {
        verifyFixedRetryPropertiesConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void exponentialRetrySettingsCanWork(boolean isShareServiceClientBuilder) {
        exponentialRetryPropertiesConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void transportTypeConfigured(boolean isShareServiceClientBuilder) {
        verifyTransportTypeConfigured(isShareServiceClientBuilder);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void connectionStringConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");
        properties.setConnectionString(connectionString);
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).connectionString(connectionString);
    }

    private void verifyFqdnConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        properties.setNamespace("another-namespace");
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();

        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(),
            times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    private void verifyClientSecretTokenCredentialConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        final F factory = factoryWithClientSecretTokenCredentialConfigured(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(ClientSecretCredential.class));
    }

    private void verifyClientCertificateCredentialConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        final F factory = factoryWithClientCertificateTokenCredentialConfigured(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(ClientCertificateCredential.class));
    }

    private void verifyUsernamePasswordCredentialConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        final F factory = factoryWithUsernamePasswordTokenCredentialConfigured(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(UsernamePasswordCredential.class));
    }

    private void verifyManagedIdentityCredentialConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        final F factory = factoryWithManagedIdentityTokenCredentialConfigured(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).credential(any(ManagedIdentityCredential.class));
    }

    private void verifyProxyPropertiesConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        AmqpProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");
        proxyProperties.setAuthenticationType("basic");

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).proxyOptions(any(ProxyOptions.class));
    }

    private void verifyFixedRetryPropertiesConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setMaxRetries(2);
        properties.getRetry().setTryTimeout(Duration.ofSeconds(3));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    private void exponentialRetryPropertiesConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);
        properties.getRetry().getExponential().setMaxRetries(2);
        properties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        properties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        properties.getRetry().setTryTimeout(Duration.ofSeconds(5));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    private void verifyTransportTypeConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        AmqpTransportType transportType = AmqpTransportType.AMQP_WEB_SOCKETS;
        properties.getClient().setTransportType(transportType);

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        verify(factory.getServiceBusClientBuilder(), times(1)).transportType(transportType);
    }

}
