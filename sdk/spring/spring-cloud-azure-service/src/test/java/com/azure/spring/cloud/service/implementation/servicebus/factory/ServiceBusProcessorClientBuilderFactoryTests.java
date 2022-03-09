// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientTestProperties;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusProcessorClientBuilderFactoryTests
    extends AbstractServiceBusSubClientBuilderFactoryTests<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder,
    ServiceBusProcessorClientTestProperties, ServiceBusProcessorClientBuilderFactory> {

    @Override
    protected ServiceBusProcessorClientTestProperties createMinimalServiceProperties() {
        ServiceBusProcessorClientTestProperties properties = new ServiceBusProcessorClientTestProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getMinimalClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusProcessorClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusProcessorClientBuilderFactoryExt getClientBuilderFactory(ServiceBusProcessorClientTestProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        MessageListener<?> listener = (ServiceBusRecordMessageListener) messageContext -> { };

        ServiceBusProcessorClientBuilderFactoryExt factory =
            spy(new ServiceBusProcessorClientBuilderFactoryExt(clientBuilder, properties, listener, errorContext -> { }));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusProcessorClientBuilderFactoryExt extends ServiceBusProcessorClientBuilderFactory {
        ServiceBusProcessorClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorClientTestProperties properties,
                                                   MessageListener<?> messageListener,
                                                   ServiceBusErrorHandler errorHandler) {
            super(serviceBusClientBuilder, properties, messageListener, errorHandler);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
        }
    }
}
