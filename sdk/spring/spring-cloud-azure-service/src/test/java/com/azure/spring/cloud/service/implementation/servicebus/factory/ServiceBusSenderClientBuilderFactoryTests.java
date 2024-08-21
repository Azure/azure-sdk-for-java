// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusSenderClientTestProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusSenderClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<
    ServiceBusClientBuilder.ServiceBusSenderClientBuilder,
    ServiceBusSenderClientTestProperties,
    ServiceBusSenderClientBuilderFactory> {

    @Test
    void queueConfigured() {
        ServiceBusSenderClientTestProperties properties = new ServiceBusSenderClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityType(ServiceBusEntityType.QUEUE);
        properties.setEntityName("test-queue");

        final ServiceBusSenderClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = factory.build();
        builder.buildClient();

        verify(builder, times(1)).queueName("test-queue");
    }

    @Override
    protected ServiceBusSenderClientTestProperties createMinimalServiceProperties() {
        ServiceBusSenderClientTestProperties properties = new ServiceBusSenderClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSenderClientBuilderFactory createClientBuilderFactoryWithMockBuilder(ServiceBusSenderClientTestProperties properties) {
        return spy(new ServiceBusReceiverClientBuilderFactoryExt(getSharedServiceBusClientBuilder(properties), properties));
    }

    @Override
    void verifyServicePropertiesConfigured(boolean isShareServiceClientBuilder) {
        ServiceBusSenderClientTestProperties properties = new ServiceBusSenderClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityName("test-topic");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setCustomEndpointAddress(this.customEndpoint);
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);

        final ServiceBusSenderClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = factory.build();
        builder.buildClient();

        verify(factory.getServiceBusClientBuilder(), times(1)).customEndpointAddress(customEndpoint);
        verify(builder, times(1)).topicName("test-topic");

        verify(factory.getServiceBusClientBuilder(), times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    @Override
    void buildClient(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {
        builder.buildClient();
    }

    static class ServiceBusReceiverClientBuilderFactoryExt extends ServiceBusSenderClientBuilderFactory {
        private ServiceBusClientBuilder serviceBusClientBuilder;
        private final ServiceBusSenderClientTestProperties properties;
        ServiceBusReceiverClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                  ServiceBusSenderClientTestProperties properties) {
            super(serviceBusClientBuilder, properties);
            this.properties = properties;
            if (properties.isShareServiceBusClientBuilder() && serviceBusClientBuilder != null) {
                this.serviceBusClientBuilder = serviceBusClientBuilder;
            }
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSenderClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class);
        }

        @Override
        protected ServiceBusClientBuilder getServiceBusClientBuilder() {
            if (!this.isShareServiceBusClientBuilder() && this.serviceBusClientBuilder == null) {
                TestServiceBusClientBuilderFactory clientBuilderFactory = spy(new TestServiceBusClientBuilderFactory(properties));
                this.serviceBusClientBuilder = clientBuilderFactory.build();
            }
            return this.serviceBusClientBuilder;
        }
    }
}
