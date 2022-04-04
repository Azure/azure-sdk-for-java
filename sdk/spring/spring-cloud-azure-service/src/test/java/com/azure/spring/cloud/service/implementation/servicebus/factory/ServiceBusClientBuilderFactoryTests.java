// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.service.implementation.AzureAmqpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonTestProperties;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusClientBuilderFactoryTests extends AzureAmqpClientBuilderFactoryBaseTests<
    ServiceBusClientBuilder,
    ServiceBusClientCommonTestProperties,
    ServiceBusClientBuilderFactoryTests.ServiceBusClientBuilderFactoryExt> {

    @Test
    void azureSasCredentialConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(anyString(), any(AzureSasCredential.class));
    }

    @Test
    void azureNamedKeyCredentialConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(anyString(), any(AzureNamedKeyCredential.class));
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
        verify(builder, mode).credential(any(String.class), any(tokenCredentialClass));
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
