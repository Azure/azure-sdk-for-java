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
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.AzureGenericServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonTestProperties;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

abstract class AbstractServiceBusSubClientBuilderFactoryTests<B,
    P extends ServiceBusClientCommonTestProperties,
    F extends AbstractServiceBusSubClientBuilderFactory<B, ?>> extends AzureGenericServiceClientBuilderFactoryBaseTests<P, F> {

    abstract void verifyServicePropertiesConfigured(boolean isShareServiceClientBuilder);
    abstract void buildClient(B builder);

    @Test
    void servicePropertiesConfigured() {
        verifyServicePropertiesConfigured(true);
        verifyServicePropertiesConfigured(false);
    }

    @Test
    void minimalSettingsCanWork() {
        final F factory = factoryWithMinimalSettings();
        B builder = factory.build();
        buildClient(builder);
    }

    @Test
    void clientSecretTokenCredentialConfigured() {
        verifyClientSecretTokenCredentialConfigured(true);
        verifyClientSecretTokenCredentialConfigured(false);
    }

    @Test
    void clientCertificateTokenCredentialConfigured() {
        verifyClientCertificateCredentialConfigured(true);
        verifyClientCertificateCredentialConfigured(false);
    }

    @Test
    void usernamePasswordTokenCredentialConfigured() {
        verifyUsernamePasswordCredentialConfigured(true);
        verifyUsernamePasswordCredentialConfigured(false);
    }

    @Test
    void managedIdentityTokenCredentialConfigured() {
        verifyManagedIdentityCredentialConfigured(true);
        verifyManagedIdentityCredentialConfigured(false);
    }

    @Test
    void proxyPropertiesConfigured() {
        verifyProxyPropertiesConfigured(true);
        verifyProxyPropertiesConfigured(false);
    }

    @Test
    void fixedRetrySettingsCanWork() {
        verifyFixedRetryPropertiesConfigured(true);
        verifyFixedRetryPropertiesConfigured(false);
    }

    @Test
    void exponentialRetrySettingsCanWork() {
        exponentialRetryPropertiesConfigured(true);
        exponentialRetryPropertiesConfigured(false);
    }

    @Test
    void transportTypeConfigured() {
        verifyTransportTypeConfigured(true);
        verifyTransportTypeConfigured(false);
    }

    @Test
    void connectionStringConfigured() {
        verifyConnectionConfigured(true);
        verifyConnectionConfigured(false);
    }

    private void verifyClientSecretTokenCredentialConfigured(boolean isShareServiceClientBuilder) {
        final F factory = factoryWithClientSecretTokenCredentialConfigured(createMinimalServiceProperties());
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).credential(any(ClientSecretCredential.class));
    }
    private void verifyClientCertificateCredentialConfigured(boolean isShareServiceClientBuilder) {
        final F factory = factoryWithClientCertificateTokenCredentialConfigured(createMinimalServiceProperties());
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).credential(any(ClientCertificateCredential.class));
    }

    private void verifyUsernamePasswordCredentialConfigured(boolean isShareServiceClientBuilder) {
        final F factory = factoryWithUsernamePasswordTokenCredentialConfigured(createMinimalServiceProperties());
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).credential(any(UsernamePasswordCredential.class));
    }

    private void verifyManagedIdentityCredentialConfigured(boolean isShareServiceClientBuilder) {
        final F factory = factoryWithManagedIdentityTokenCredentialConfigured(createMinimalServiceProperties());
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).credential(any(ManagedIdentityCredential.class));
    }

    private void verifyProxyPropertiesConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        AmqpProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");
        proxyProperties.setAuthenticationType("basic");

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).proxyOptions(any(ProxyOptions.class));
    }

    private void verifyFixedRetryPropertiesConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setMaxRetries(2);
        properties.getRetry().setTryTimeout(Duration.ofSeconds(3));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).retryOptions(any(AmqpRetryOptions.class));
    }

    private void exponentialRetryPropertiesConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);
        properties.getRetry().getExponential().setMaxRetries(2);
        properties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        properties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        properties.getRetry().setTryTimeout(Duration.ofSeconds(5));

        F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).retryOptions(any(AmqpRetryOptions.class));
    }

    private void verifyTransportTypeConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        AmqpTransportType transportType = AmqpTransportType.AMQP_WEB_SOCKETS;
        properties.getClient().setTransportType(transportType);

        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).transportType(transportType);
    }

    private void verifyConnectionConfigured(boolean isShareServiceClientBuilder) {
        P properties = createMinimalServiceProperties();
        properties.setConnectionString("test-connection-string");
        final F factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        B builder = factory.build();
        buildClient(builder);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).connectionString("test-connection-string");
    }
}
