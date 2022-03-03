// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.listener;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
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

class EventHubsMessageListenerContainerTests {

    @Mock
    private EventHubsProcessorFactory mockProcessorFactory;

    @Mock
    private EventProcessorClient delegateEventProcessorClient;
    @Mock
    private EventProcessorClient anotherDelegateEventProcessorClient;

    private EventHubsMessageListenerContainer listenerContainer;
    private final EventHubsRecordMessageListener listener = eventContext -> { };
    private AutoCloseable closeable;
    private final String consumerGroup = "consumer-group";
    private final String anotherConsumerGroup = "another-consumer-group";
    private final String destination = "event-hub";

    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(consumerGroup),
            isA(EventHubsContainerProperties.class)))
            .thenReturn(this.delegateEventProcessorClient);

        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(anotherConsumerGroup),
            isA(EventHubsContainerProperties.class)))
            .thenReturn(this.anotherDelegateEventProcessorClient);

        EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
        containerProperties.setEventHubName(destination);
        containerProperties.setConsumerGroup(consumerGroup);
        containerProperties.setMessageListener(listener);

        this.listenerContainer = new EventHubsMessageListenerContainer(mockProcessorFactory, containerProperties);
        doNothing().when(this.delegateEventProcessorClient).stop();
        doNothing().when(this.delegateEventProcessorClient).start();

        doNothing().when(this.anotherDelegateEventProcessorClient).stop();
        doNothing().when(this.anotherDelegateEventProcessorClient).start();

    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void testStart() {
        this.listenerContainer.start();

        verifySubscriberCreatorCalled();
        verify(this.delegateEventProcessorClient, times(1)).start();
    }

    @Test
    void testStartTwice() {
        this.listenerContainer.start();
        verify(this.delegateEventProcessorClient, times(1)).start();

        this.listenerContainer.start();
        verify(this.delegateEventProcessorClient, times(2)).start();

        verifySubscriberCreatorCalled();
    }

    @Test
    void testCreateListenerContainerWithAnotherGroup() {
        EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
        containerProperties.setEventHubName(destination);
        containerProperties.setConsumerGroup(anotherConsumerGroup);
        containerProperties.setMessageListener(listener);
        EventHubsMessageListenerContainer anotherListenerContainer = new EventHubsMessageListenerContainer(this.mockProcessorFactory, containerProperties);
        this.listenerContainer.start();
        verify(this.delegateEventProcessorClient, times(1)).start();

        anotherListenerContainer.start();
        verify(this.anotherDelegateEventProcessorClient, times(1)).start();

        verifySubscriberCreatorCalled();
        verify(this.delegateEventProcessorClient, times(1)).start();
        verify(this.anotherDelegateEventProcessorClient, times(1)).start();

    }

    private void verifySubscriberCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce())
            .createProcessor(anyString(), anyString(), isA(EventHubsContainerProperties.class));
    }


}
