// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonTestProperties;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<ServiceBusClientBuilder,
    ServiceBusClientCommonTestProperties, ServiceBusClientBuilderFactory> {

    @Override
    protected ServiceBusClientCommonTestProperties createMinimalServiceProperties() {
        return new ServiceBusClientCommonTestProperties();
    }

    @Test
    void retryOptionsConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        final ServiceBusClientBuilderFactoryExt builderFactory = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = builderFactory.build();
        verify(builder, times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    @Test
    void transportTypeConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        final ServiceBusClientBuilderFactoryExt builderFactory = new ServiceBusClientBuilderFactoryExt(properties);
        final ServiceBusClientBuilder builder = builderFactory.build();
        verify(builder, times(1)).transportType(any(AmqpTransportType.class));
    }

    @Test
    void azureSasCredentialConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.setSasToken("test-token");
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(anyString(), any(AzureSasCredential.class));
    }

    @Test
    void tokenCredentialConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(anyString(), any(TokenCredential.class));
    }

    @Test
    void azureNamedKeyCredentialConfigured() {
        ServiceBusClientCommonTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");

        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(anyString(), any(AzureNamedKeyCredential.class));
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
