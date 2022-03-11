// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientTestProperties;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusSessionProcessorClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<
    ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder,
    ServiceBusProcessorClientTestProperties,
    ServiceBusSessionProcessorClientBuilderFactory> {

    @Override
    protected ServiceBusProcessorClientTestProperties createMinimalServiceProperties() {
        ServiceBusProcessorClientTestProperties properties = new ServiceBusProcessorClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory createClientBuilderFactoryWithMockBuilder(ServiceBusProcessorClientTestProperties properties) {
        ServiceBusSessionProcessorClientBuilderFactoryExt factory = spy(new ServiceBusSessionProcessorClientBuilderFactoryExt(mock(ServiceBusClientBuilder.class), properties));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusSessionProcessorClientBuilderFactoryExt extends ServiceBusSessionProcessorClientBuilderFactory {
        ServiceBusSessionProcessorClientBuilderFactoryExt(ServiceBusClientBuilder clientBuilder,
                                                          ServiceBusProcessorClientTestProperties properties) {
            super(clientBuilder, properties, (ServiceBusRecordMessageListener) message -> { }, errorContext -> { });
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
        }
    }
}
