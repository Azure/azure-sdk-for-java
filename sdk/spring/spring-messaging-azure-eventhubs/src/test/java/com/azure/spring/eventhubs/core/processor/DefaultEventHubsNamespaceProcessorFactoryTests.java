// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.processor;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.implementation.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.service.eventhubs.processor.EventHubsRecordMessageListener;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorErrorContextConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

class DefaultEventHubsNamespaceProcessorFactoryTests {

    private EventHubsProcessorFactory processorFactory;
    private final String eventHubName = "eventHub";
    private final String consumerGroup = "group";
    private final String anotherConsumerGroup = "group2";
    private final EventHubsRecordMessageListener listener = eventContext -> { };
    private final EventProcessorErrorContextConsumer errorContextConsumer = errorContext -> { };
    private int processorAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.processorFactory = new DefaultEventHubsNamespaceProcessorFactory(mock(CheckpointStore.class),
            namespaceProperties);
        processorAddedTimes = 0;
        this.processorFactory.addListener((eventHub, consumerGroup, client) -> processorAddedTimes++);
    }

    @Test
    void testGetEventProcessorClient() {
        EventProcessorClient processorClient = processorFactory.createProcessor(eventHubName, consumerGroup, listener, errorContextConsumer);

        assertNotNull(processorClient);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testCreateEventProcessorClientTwice() {
        EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, errorContextConsumer);
        assertNotNull(client);

        processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, errorContextConsumer);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testRecreateEventProcessorClient() throws Exception {
        final EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, errorContextConsumer);
        assertNotNull(client);

        EventProcessorClient anotherClient = processorFactory.createProcessor(eventHubName, anotherConsumerGroup, this.listener, errorContextConsumer);
        assertNotNull(anotherClient);
        assertEquals(2, processorAddedTimes);
    }

}
