// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

public class ServiceBusProcessorsContainerTests {
    @Mock
    private ServiceBusProcessorFactory mockProcessorFactory;
    @Mock
    private ServiceBusProcessorClient oneProcessorClient;
    @Mock
    private ServiceBusProcessorClient anotherProcessorClient;

    private ServiceBusProcessorContainer processorContainer;
    private final RecordMessageProcessingListener listener = messageContext -> { };

    private AutoCloseable closeable;
    private final String subscription = "subscription";
    private final String anotherSubscription = "subscription2";
    private final String destination = "service-bus";

    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(subscription), isA(MessageProcessingListener.class)))
            .thenReturn(this.oneProcessorClient);
        when(this.mockProcessorFactory.createProcessor(eq(destination), isA(MessageProcessingListener.class)))
            .thenReturn(this.oneProcessorClient);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(anotherSubscription), isA(MessageProcessingListener.class)))
            .thenReturn(this.anotherProcessorClient);
        this.processorContainer = new ServiceBusProcessorContainer(mockProcessorFactory);
        doNothing().when(this.oneProcessorClient).stop();
        doNothing().when(this.oneProcessorClient).start();
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void testSubscribeQueue() {
        this.processorContainer.subscribe(this.destination, this.listener);

        verifySubscriberQueueCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();
    }

    @Test
    void testSubscribeQueueTwice() {
        ServiceBusProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination, this.listener);
        verify(this.oneProcessorClient, times(1)).start();

        ServiceBusProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination, this.listener);

        Assertions.assertEquals(processorClient1, processorClient2);
        verifySubscriberQueueCreatorCalled();
        verify(this.oneProcessorClient, times(2)).start();
    }

    @Test
    void testSubscribeTopic() {
        this.processorContainer.subscribe(this.destination, this.subscription, this.listener);

        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();
    }

    @Test
    void testSubscribeTopicTwice() {
        ServiceBusProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination, this.subscription, this.listener);
        verify(this.oneProcessorClient, times(1)).start();

        ServiceBusProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination, this.subscription, this.listener);

        Assertions.assertEquals(processorClient1, processorClient2);
        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(2)).start();
    }

    @Test
    void testSubscribeWithAnotherSubscription() {

        ServiceBusProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination, this.subscription, this.listener);
        verify(this.oneProcessorClient, times(1)).start();

        ServiceBusProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination, this.anotherSubscription, this.listener);
        Assertions.assertNotEquals(processorClient1, processorClient2);

        verifySubscriberTopicCreatorCalled();
        verify(this.oneProcessorClient, times(1)).start();
        verify(this.anotherProcessorClient, times(1)).start();

    }

    private void verifySubscriberTopicCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce()).createProcessor(anyString(), anyString(), isA(MessageProcessingListener.class));
    }

    private void verifySubscriberQueueCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce()).createProcessor(anyString(), isA(MessageProcessingListener.class));
    }

}
