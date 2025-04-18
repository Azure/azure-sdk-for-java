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
import org.mockito.verification.VerificationMode;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusSessionProcessorClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<
    ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder,
    ServiceBusProcessorClientTestProperties,
    ServiceBusSessionProcessorClientBuilderFactory> {

    @Test
    void queueConfigured() {
        ServiceBusProcessorClientTestProperties properties = new ServiceBusProcessorClientTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEntityType(ServiceBusEntityType.QUEUE);
        properties.setEntityName("test-queue");

        final ServiceBusSessionProcessorClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        final ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder = factory.build();
        builder.buildProcessorClient();

        verify(builder, times(1)).queueName("test-queue");
    }

    @Test
    void errorHandlerConfigured() {
        ServiceBusErrorHandler errorHandler = errorContext -> { };
        ServiceBusRecordMessageListener messageListener = eventContext -> { };

        final ServiceBusProcessorClientBuilderFactory factory = new ServiceBusProcessorClientBuilderFactoryTests.ServiceBusProcessorClientBuilderFactoryExt(
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

        final ServiceBusProcessorClientBuilderFactory factory = new ServiceBusProcessorClientBuilderFactoryTests.ServiceBusProcessorClientBuilderFactoryExt(
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

        final ServiceBusProcessorClientBuilderFactory factory = new ServiceBusProcessorClientBuilderFactoryTests.ServiceBusProcessorClientBuilderFactoryExt(
            mock(ServiceBusClientBuilder.class),
            createMinimalServiceProperties(),
            messageListener,
            errorHandler);

        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.build());
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
    protected ServiceBusSessionProcessorClientBuilderFactory createClientBuilderFactoryWithMockBuilder(ServiceBusProcessorClientTestProperties properties) {
        return spy(new ServiceBusSessionProcessorClientBuilderFactoryExt(mock(ServiceBusClientBuilder.class), properties));
    }

    @Override
    void verifyServicePropertiesConfigured(boolean isShareServiceClientBuilder) {
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

        final ServiceBusSessionProcessorClientBuilderFactory factory = createClientBuilderFactoryWithMockBuilder(properties);
        doReturn(isShareServiceClientBuilder).when(factory).isShareServiceBusClientBuilder();
        final ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder = factory.build();
        builder.buildProcessorClient();

        verify(builder, times(1)).topicName("test-topic");
        verify(builder, times(1)).subscriptionName("test-subscription");
        verify(builder, times(1)).receiveMode(ServiceBusReceiveMode.PEEK_LOCK);
        verify(builder, times(1)).subQueue(SubQueue.NONE);
        verify(builder, times(1)).prefetchCount(100);
        verify(builder, times(1)).maxAutoLockRenewDuration(Duration.ofSeconds(5));
        verify(builder, times(1)).disableAutoComplete();
        verify(builder, times(1)).maxConcurrentCalls(10);
        verify(builder, times(1)).maxConcurrentSessions(20);

        VerificationMode calledTimes = isShareServiceClientBuilder ? times(0) : times(1);
        verify(factory.getServiceBusClientBuilder(), calledTimes).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
    }

    @Override
    void buildClient(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        builder.buildProcessorClient();
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
