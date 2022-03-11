// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusReceiverClientTestProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusSessionReceiverClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<
    ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder,
    ServiceBusReceiverClientTestProperties,
    ServiceBusSessionReceiverClientBuilderFactory> {

    @Override
    protected ServiceBusReceiverClientTestProperties createMinimalServiceProperties() {
        ServiceBusReceiverClientTestProperties properties = new ServiceBusReceiverClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSessionReceiverClientBuilderFactory createClientBuilderFactoryWithMockBuilder(ServiceBusReceiverClientTestProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        ServiceBusSessionReceiverClientBuilderFactoryExt factory = spy(new ServiceBusSessionReceiverClientBuilderFactoryExt(clientBuilder, properties));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusSessionReceiverClientBuilderFactoryExt extends ServiceBusSessionReceiverClientBuilderFactory {

        ServiceBusSessionReceiverClientBuilderFactoryExt(ServiceBusClientBuilder clientBuilder,
                                                         ServiceBusReceiverClientTestProperties properties) {
            super(clientBuilder, properties);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);
        }
    }
}
