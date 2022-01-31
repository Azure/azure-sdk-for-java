// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusClientBuilderFactory;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ServiceBusClientBuilderFactoryTestsTests extends AzureServiceClientBuilderFactoryBaseTests<ServiceBusClientBuilder,
    TestServiceBusClientCommonProperties, ServiceBusClientBuilderFactory> {
    private static final Configuration NOOP = new Configuration();
    @Override
    protected TestServiceBusClientCommonProperties createMinimalServiceProperties() {
        return new TestServiceBusClientCommonProperties();
    }

    @Test
    void retryOptionsConfigured() {
        TestServiceBusClientCommonProperties properties = createMinimalServiceProperties();
        final ServiceBusClientBuilderFactoryExt builderFactory = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = builderFactory.build(NOOP);
        verify(builder, times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    @Test
    void transportTypeConfigured() {
        TestServiceBusClientCommonProperties properties = createMinimalServiceProperties();
        final ServiceBusClientBuilderFactoryExt builderFactory = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = builderFactory.build(NOOP);
        verify(builder, times(1)).transportType(any(AmqpTransportType.class));
    }

    @Test
    void azureSasCredentialConfigured() {
        TestServiceBusClientCommonProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.setSasToken("test-token");
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build(NOOP);
        verify(builder, times(1)).credential(anyString(), any(AzureSasCredential.class));
    }

    @Test
    void tokenCredentialConfigured() {
        TestServiceBusClientCommonProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build(NOOP);
        verify(builder, times(1)).credential(anyString(), any(TokenCredential.class));
    }

    @Test
    void azureNamedKeyCredentialConfigured() {
        TestServiceBusClientCommonProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");

        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build(NOOP);
        verify(builder, times(1)).credential(anyString(), any(AzureNamedKeyCredential.class));
    }

    static class ServiceBusClientBuilderFactoryExt extends ServiceBusClientBuilderFactory {

        ServiceBusClientBuilderFactoryExt(TestServiceBusClientCommonProperties properties) {
            super(properties);
        }

        @Override
        public ServiceBusClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.class);
        }
    }
}
