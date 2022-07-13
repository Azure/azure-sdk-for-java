// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusSenderClientTestProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

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
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        return spy(new ServiceBusReceiverClientBuilderFactoryExt(clientBuilder, properties));
    }

    @Override
    void verifyServicePropertiesConfigured(boolean isShareServiceClientBuilder) {
        ServiceBusSenderClientTestProperties properties = new ServiceBusSenderClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityName("test-topic");
        properties.setEntityType(ServiceBusEntityType.TOPIC);

        final ServiceBusSenderClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = factory.build();
        builder.buildClient();

        verify(builder, times(1)).topicName("test-topic");

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    @Override
    void buildClient(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {
        builder.buildClient();
    }

    static class ServiceBusReceiverClientBuilderFactoryExt extends ServiceBusSenderClientBuilderFactory {
        ServiceBusReceiverClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                  ServiceBusSenderClientTestProperties properties) {
            super(serviceBusClientBuilder, properties);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSenderClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class);
        }
    }
}
