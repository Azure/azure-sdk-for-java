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

class ServiceBusProcessorClientBuilderFactoryTests
    extends AbstractServiceBusSubClientBuilderFactoryTests<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder,
    TestServiceBusProcessorClientProperties, ServiceBusProcessorClientBuilderFactory> {

    @Override
    protected TestServiceBusProcessorClientProperties createMinimalServiceProperties() {
        TestServiceBusProcessorClientProperties properties = new TestServiceBusProcessorClientProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getMinimalClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        TestServiceBusProcessorClientProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusProcessorClientBuilderFactoryExt getClientBuilderFactory(TestServiceBusProcessorClientProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        ServiceBusMessageListener listener = (ServiceBusRecordMessageListener) messageContext -> {

        };

        ServiceBusProcessorClientBuilderFactoryExt factory =
            spy(new ServiceBusProcessorClientBuilderFactoryExt(clientBuilder, properties, listener, errorContext -> { }));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusProcessorClientBuilderFactoryExt extends ServiceBusProcessorClientBuilderFactory {
        ServiceBusProcessorClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   TestServiceBusProcessorClientProperties properties,
                                                   ServiceBusMessageListener messageListener,
                                                   Consumer<ServiceBusErrorContext> errorContextConsumer) {
            super(serviceBusClientBuilder, properties, messageListener, errorContextConsumer);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
        }
    }
}
