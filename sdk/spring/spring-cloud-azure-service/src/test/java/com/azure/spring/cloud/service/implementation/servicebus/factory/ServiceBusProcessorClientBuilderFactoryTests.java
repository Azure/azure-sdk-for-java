// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientTestProperties;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusProcessorClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<
    ServiceBusClientBuilder.ServiceBusProcessorClientBuilder,
    ServiceBusProcessorClientTestProperties,
    ServiceBusProcessorClientBuilderFactory> {

    @Test
    void queueConfigured() {
        ServiceBusProcessorClientTestProperties properties = new ServiceBusProcessorClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityType(ServiceBusEntityType.QUEUE);
        properties.setEntityName("test-queue");

        final ServiceBusProcessorClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        final ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = factory.build();
        builder.buildProcessorClient();

        verify(builder, times(1)).queueName("test-queue");
    }

    @Test
    void errorHandlerConfigured() {
        ServiceBusErrorHandler errorHandler = errorContext -> { };
        ServiceBusRecordMessageListener messageListener = eventContext -> { };

        final ServiceBusProcessorClientBuilderFactory factory = new ServiceBusProcessorClientBuilderFactoryExt(
            mock(ServiceBusClientBuilder.class),
            createMinimalServiceProperties(),
            messageListener,
            errorHandler);
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = factory.build();

        verify(builder, times(1)).processError(errorHandler);
    }

    @Test
    void messageListenerConfigured() {
        ServiceBusErrorHandler errorHandler = errorContext -> { };
        ServiceBusRecordMessageListener messageListener = message -> { };

        final ServiceBusProcessorClientBuilderFactory factory = new ServiceBusProcessorClientBuilderFactoryExt(
            mock(ServiceBusClientBuilder.class),
            createMinimalServiceProperties(),
            messageListener,
            errorHandler);
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = factory.build();

        verify(builder, times(1)).processMessage(any());
    }

    @Test
    void wrongMessageListenerTypeWillThrow() {
        ServiceBusErrorHandler errorHandler = errorContext -> { };
        MessageListener<?> messageListener = message -> { };

        final ServiceBusProcessorClientBuilderFactory factory = new ServiceBusProcessorClientBuilderFactoryExt(
            mock(ServiceBusClientBuilder.class),
            createMinimalServiceProperties(),
            messageListener,
            errorHandler);

        Assertions.assertThrows(IllegalArgumentException.class, factory::build);
    }

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
        return spy(new ServiceBusProcessorClientBuilderFactoryExt(getSharedServiceBusClientBuilder(properties), properties));
    }

    @Override
    void verifyServicePropertiesConfigured(boolean isShareServiceClientBuilder) {
        ServiceBusProcessorClientTestProperties properties = getServiceBusProcessorClientTestProperties(isShareServiceClientBuilder);

        final ServiceBusProcessorClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        final ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = factory.build();
        builder.buildProcessorClient();

        verify(factory.getServiceBusClientBuilder(), times(1)).customEndpointAddress(customEndpoint);

        verify(builder, times(1)).topicName("test-topic");
        verify(builder, times(1)).subscriptionName("test-subscription");
        verify(builder, times(1)).receiveMode(ServiceBusReceiveMode.PEEK_LOCK);
        verify(builder, times(1)).subQueue(SubQueue.NONE);
        verify(builder, times(1)).prefetchCount(100);
        verify(builder, times(1)).maxAutoLockRenewDuration(Duration.ofSeconds(5));
        verify(builder, times(1)).disableAutoComplete();
        verify(builder, times(1)).maxConcurrentCalls(10);

        verify(factory.getServiceBusClientBuilder(), times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    private ServiceBusProcessorClientTestProperties getServiceBusProcessorClientTestProperties(boolean isShareServiceClientBuilder) {
        ServiceBusProcessorClientTestProperties properties = new ServiceBusProcessorClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityName("test-topic");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        properties.setReceiveMode(ServiceBusReceiveMode.PEEK_LOCK);
        properties.setSubQueue(SubQueue.NONE);
        properties.setPrefetchCount(100);
        properties.setMaxAutoLockRenewDuration(Duration.ofSeconds(5));
        properties.setAutoComplete(false);
        properties.setMaxConcurrentCalls(10);
        properties.setMaxConcurrentSessions(20);
        properties.setCustomEndpointAddress(this.customEndpoint);
        properties.setShareServiceBusClientBuilder(isShareServiceClientBuilder);
        return properties;
    }

    @Override
    void buildClient(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        builder.buildProcessorClient();
    }

    static class ServiceBusProcessorClientBuilderFactoryExt extends ServiceBusProcessorClientBuilderFactory {
        private ServiceBusClientBuilder serviceBusClientBuilder;
        private final ServiceBusProcessorClientTestProperties properties;
        private ServiceBusProcessorClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorClientTestProperties properties) {

            this(serviceBusClientBuilder, properties, (ServiceBusRecordMessageListener) messageContext -> { }, errorContext -> { });
        }

        ServiceBusProcessorClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorClientTestProperties properties,
                                                   MessageListener<?> messageListener,
                                                   ServiceBusErrorHandler errorHandler) {

            super(serviceBusClientBuilder, properties, messageListener, errorHandler);
            this.properties = properties;
            if (properties.isShareServiceBusClientBuilder() && serviceBusClientBuilder != null) {
                this.serviceBusClientBuilder = serviceBusClientBuilder;
            }
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
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
