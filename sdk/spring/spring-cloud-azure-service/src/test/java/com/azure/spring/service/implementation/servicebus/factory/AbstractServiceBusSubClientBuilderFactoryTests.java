// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

abstract class AbstractServiceBusSubClientBuilderFactoryTests<Builder,
    Properties extends ServiceBusClientCommonProperties,
    Factory extends AbstractServiceBusSubClientBuilderFactory<?, ?>> {

    private static final Configuration NOOP = new Configuration();

    protected abstract Properties createMinimalServiceProperties();
    protected abstract Factory getMinimalClientBuilderFactory();
    protected abstract Factory getSasCredentialConfiguredClientBuilderFactory();
    protected abstract Factory getTokenCredentialConfiguredClientBuilderFactory();
    protected abstract Factory getNamedKeyCredentialConfiguredClientBuilderFactory();

    @Test
    void retryOptionsConfigured() {
        final Factory builderFactory = getMinimalClientBuilderFactory();
        builderFactory.build(NOOP);
        verify(builderFactory.getServiceBusClientBuilder(), times(1)).retryOptions(any(AmqpRetryOptions.class));
    }

    @Test
    void transportTypeConfigured() {
        final Factory factory = getMinimalClientBuilderFactory();
        factory.build(NOOP);
        verify(factory.getServiceBusClientBuilder(), times(1)).transportType(any(AmqpTransportType.class));
    }

    @Test
    void azureSasCredentialConfigured() {
        final Factory factory = getSasCredentialConfiguredClientBuilderFactory();
        factory.build(NOOP);
        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(AzureSasCredential.class));
    }

    @Test
    void tokenCredentialConfigured() {
        final Factory factory = getTokenCredentialConfiguredClientBuilderFactory();
        factory.build(NOOP);
        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(TokenCredential.class));
    }

    @Test
    void azureNamedKeyCredentialConfigured() {
        final Factory factory = getNamedKeyCredentialConfiguredClientBuilderFactory();
        factory.build(NOOP);
        verify(factory.getServiceBusClientBuilder(), times(1)).credential(anyString(), any(AzureNamedKeyCredential.class));
    }
}
