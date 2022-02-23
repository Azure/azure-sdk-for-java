// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
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

class EventHubsProcessorsContainerTests {

    @Mock
    private EventHubsProcessorFactory mockProcessorFactory;

    @Mock
    private EventProcessorClient oneEventProcessorClient;

    @Mock
    private EventProcessorClient anotherEventProcessorClient;

    private EventHubsProcessorContainer processorContainer;
    private final RecordEventProcessingListener listener = eventContext -> { };

    private AutoCloseable closeable;
    private final String consumerGroup = "consumer-group";
    private final String anotherConsumerGroup = "consumer-group2";
    private final String destination = "event-hub";


    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(consumerGroup), isA(EventProcessingListener.class)))
            .thenReturn(this.oneEventProcessorClient);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(anotherConsumerGroup), isA(EventProcessingListener.class)))
            .thenReturn(this.anotherEventProcessorClient);

        this.processorContainer = new EventHubsProcessorContainer(mockProcessorFactory);
        doNothing().when(this.oneEventProcessorClient).stop();
        doNothing().when(this.oneEventProcessorClient).start();
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void testSubscribe() {
        this.processorContainer.subscribe(this.destination, this.consumerGroup, this.listener);

        verifySubscriberCreatorCalled();
        verify(this.oneEventProcessorClient, times(1)).start();
    }

    @Test
    void testSubscribeTwice() {
        EventProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination, this.consumerGroup, this.listener);
        verify(this.oneEventProcessorClient, times(1)).start();

        EventProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination, this.consumerGroup, this.listener);

        Assertions.assertEquals(processorClient1, processorClient2);
        verifySubscriberCreatorCalled();
        verify(this.oneEventProcessorClient, times(2)).start();
    }

    @Test
    void testSubscribeWithAnotherGroup() {

        EventProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination, this.consumerGroup, this.listener);
        verify(this.oneEventProcessorClient, times(1)).start();

        EventProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination, this.anotherConsumerGroup, this.listener);
        Assertions.assertNotEquals(processorClient1, processorClient2);

        verifySubscriberCreatorCalled();
        verify(this.oneEventProcessorClient, times(1)).start();
        verify(this.anotherEventProcessorClient, times(1)).start();

    }

    private void verifySubscriberCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce()).createProcessor(anyString(), anyString(), isA(EventProcessingListener.class));
    }



}
