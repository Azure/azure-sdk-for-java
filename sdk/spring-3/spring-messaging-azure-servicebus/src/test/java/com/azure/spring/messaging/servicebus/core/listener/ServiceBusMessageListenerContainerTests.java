// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.listener;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusMessageListenerContainerTests {
    @Mock
    private ServiceBusProcessorFactory mockProcessorFactory;
    @Mock
    private ServiceBusProcessorClient oneProcessorClient;
    @Mock
    private ServiceBusProcessorClient anotherProcessorClient;

    private final ServiceBusRecordMessageListener listener = messageContext -> { };
    private AutoCloseable closeable;
    private final String subscription = "subscription";
    private final String anotherSubscription = "subscription2";
    private final String destination = "service-bus";

    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(subscription), isA(ServiceBusContainerProperties.class)))
            .thenReturn(this.oneProcessorClient);
        when(this.mockProcessorFactory.createProcessor(eq(destination), isA(ServiceBusContainerProperties.class)))
            .thenReturn(this.oneProcessorClient);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(anotherSubscription), isA(ServiceBusContainerProperties.class)))
            .thenReturn(this.anotherProcessorClient);

        doNothing().when(this.oneProcessorClient).stop();
        doNothing().when(this.oneProcessorClient).start();

        doNothing().when(this.anotherProcessorClient).stop();
        doNothing().when(this.anotherProcessorClient).start();
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void testStartQueue() {

        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(destination);
        containerProperties.setMessageListener(listener);

        ServiceBusMessageListenerContainer messageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);

        messageListenerContainer.start();

        verifySubscriberQueueCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();
    }

    @Test
    void testCreateQueueTwice() {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(destination);
        containerProperties.setMessageListener(listener);

        ServiceBusMessageListenerContainer messageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);

        messageListenerContainer.start();
        verifySubscriberQueueCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();

        ServiceBusMessageListenerContainer anotherMessageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);
        anotherMessageListenerContainer.start();
        verifySubscriberQueueCreatorCalled();
        verify(this.oneProcessorClient, times(2)).start();
    }

    @Test
    void testStartTopic() {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(destination);
        containerProperties.setSubscriptionName(subscription);
        containerProperties.setMessageListener(listener);

        ServiceBusMessageListenerContainer messageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);
        messageListenerContainer.start();

        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();
    }

    @Test
    void testCreateTopicTwice() {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(destination);
        containerProperties.setSubscriptionName(subscription);
        containerProperties.setMessageListener(listener);

        ServiceBusMessageListenerContainer messageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);

        messageListenerContainer.start();

        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();

        ServiceBusMessageListenerContainer anotherMessageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);
        anotherMessageListenerContainer.start();

        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(2)).start();
    }

    @Test
    void testCreateWithAnotherSubscription() {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(destination);
        containerProperties.setSubscriptionName(subscription);
        containerProperties.setMessageListener(listener);

        ServiceBusMessageListenerContainer messageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, containerProperties);

        messageListenerContainer.start();

        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();

        ServiceBusContainerProperties anotherContainerProperties = new ServiceBusContainerProperties();
        anotherContainerProperties.setEntityName(destination);
        anotherContainerProperties.setSubscriptionName(anotherSubscription);
        anotherContainerProperties.setMessageListener(listener);

        ServiceBusMessageListenerContainer anotherMessageListenerContainer = new ServiceBusMessageListenerContainer(mockProcessorFactory, anotherContainerProperties);
        anotherMessageListenerContainer.start();

        verifySubscriberTopicCreatorCalled();

        verify(this.oneProcessorClient, times(1)).start();
        verify(this.anotherProcessorClient, times(1)).start();
    }

    private void verifySubscriberTopicCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce()).createProcessor(anyString(), anyString(), isA(ServiceBusContainerProperties.class));
    }

    private void verifySubscriberQueueCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce()).createProcessor(anyString(), isA(ServiceBusContainerProperties.class));
    }

}
