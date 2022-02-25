// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.service.implementation.servicebus.TestServiceBusProcessorClientProperties;
import com.azure.spring.service.servicebus.processor.ServiceBusMessageListener;
import com.azure.spring.service.servicebus.processor.ServiceBusRecordMessageListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

import java.util.function.Consumer;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusSessionProcessorClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder,
    TestServiceBusProcessorClientProperties, ServiceBusSessionProcessorClientBuilderFactory> {

    @Override
    protected TestServiceBusProcessorClientProperties createMinimalServiceProperties() {
        TestServiceBusProcessorClientProperties properties = new TestServiceBusProcessorClientProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getMinimalClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusSessionProcessorClientBuilderFactoryExt getClientBuilderFactory(TestServiceBusProcessorClientProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        ServiceBusMessageListener listener = (ServiceBusRecordMessageListener) messageContext -> {

        };
        Consumer<ServiceBusErrorContext> errorContextConsumer = errorContext -> { };
        ServiceBusSessionProcessorClientBuilderFactoryExt factory =
            spy(new ServiceBusSessionProcessorClientBuilderFactoryExt(clientBuilder, properties, listener, errorContextConsumer));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusSessionProcessorClientBuilderFactoryExt extends ServiceBusSessionProcessorClientBuilderFactory {
        ServiceBusSessionProcessorClientBuilderFactoryExt(ServiceBusClientBuilder clientBuilder,
                                                          TestServiceBusProcessorClientProperties properties,
                                                          ServiceBusMessageListener messageListener,
                                                          Consumer<ServiceBusErrorContext> errorContextConsumer) {
            super(clientBuilder, properties, messageListener, errorContextConsumer);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
        }
    }
}
