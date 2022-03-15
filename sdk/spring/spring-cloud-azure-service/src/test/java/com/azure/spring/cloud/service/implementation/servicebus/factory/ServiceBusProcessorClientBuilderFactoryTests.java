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

class ServiceBusProcessorClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<
    ServiceBusClientBuilder.ServiceBusProcessorClientBuilder,
    ServiceBusProcessorClientTestProperties,
    ServiceBusProcessorClientBuilderFactory> {

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
    protected ServiceBusProcessorClientBuilderFactory createClientBuilderFactoryWithMockBuilder(ServiceBusProcessorClientTestProperties properties) {
        ServiceBusProcessorClientBuilderFactoryExt factory = spy(new ServiceBusProcessorClientBuilderFactoryExt(mock(ServiceBusClientBuilder.class), properties));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusProcessorClientBuilderFactoryExt extends ServiceBusProcessorClientBuilderFactory {
        ServiceBusProcessorClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorClientTestProperties properties) {

            super(serviceBusClientBuilder, properties, (ServiceBusRecordMessageListener) messageContext -> { }, errorContext -> { });
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
        }
    }
}
