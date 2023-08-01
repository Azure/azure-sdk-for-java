// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.service.implementation.AzureAmqpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonTestProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusNamespaceTestProperties;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusClientBuilderFactoryTests extends AzureAmqpClientBuilderFactoryBaseTests<
    ServiceBusClientBuilder,
    ServiceBusClientCommonTestProperties,
    ServiceBusClientBuilderFactoryTests.ServiceBusClientBuilderFactoryExt> {

    static final String CONNECTION_STRING_FORMAT = "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    @Test
    void namespaceClientPropertiesConfigured() {
        ServiceBusNamespaceTestProperties properties = new ServiceBusNamespaceTestProperties();
        properties.setNamespace("test-namespace");
        properties.setCrossEntityTransactions(true);

        final ServiceBusClientBuilderFactoryExt factoryExt = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
        verify(builder, times(1)).enableCrossEntityTransactions();
    }

    @Test
    void connectionStringConfigured() {
        ServiceBusNamespaceTestProperties properties = new ServiceBusNamespaceTestProperties();
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");
        properties.setConnectionString(connectionString);

        final ServiceBusClientBuilderFactoryExt factoryExt = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).connectionString(connectionString);
    }

    @Override
    protected ServiceBusClientCommonTestProperties createMinimalServiceProperties() {
        ServiceBusClientCommonTestProperties properties = new ServiceBusClientCommonTestProperties();
        properties.setNamespace("test-namespace");
        return properties;
    }

    @Override
    protected ServiceBusClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(
        ServiceBusClientCommonTestProperties properties) {
        return new ServiceBusClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(ServiceBusClientBuilder builder) {
        // do nothing
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        ServiceBusClientCommonTestProperties properties = new ServiceBusClientCommonTestProperties();
        properties.setNamespace("test-namespace");

        final ServiceBusClientBuilderFactoryExt factoryExt = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    @Override
    protected void verifyRetryOptionsCalled(ServiceBusClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).retryOptions(any(AmqpRetryOptions.class));
    }

    @Override
    protected void verifyProxyOptionsCalled(ServiceBusClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).proxyOptions(any(ProxyOptions.class));
    }

    @Override
    protected void verifyTransportTypeCalled(ServiceBusClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).transportType(any(AmqpTransportType.class));
    }

    @Override
    protected void verifyCredentialCalled(ServiceBusClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    static class ServiceBusClientBuilderFactoryExt extends ServiceBusClientBuilderFactory {

        ServiceBusClientBuilderFactoryExt(ServiceBusClientCommonTestProperties properties) {
            super(properties);
        }

        @Override
        public ServiceBusClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.class);
        }
    }
}
