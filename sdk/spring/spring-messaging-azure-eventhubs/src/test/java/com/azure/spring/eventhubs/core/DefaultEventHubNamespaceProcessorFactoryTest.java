// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.processor.EventHubProcessorFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

class DefaultEventHubNamespaceProcessorFactoryTest {

    private EventHubProcessorFactory processorFactory;
    private final String eventHubName = "eventHub";
    private final String consumerGroup = "group";
    private final String anotherConsumerGroup = "group2";
    private final RecordEventProcessingListenerImpl listener = new RecordEventProcessingListenerImpl();
    private int processorAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.processorFactory = new DefaultEventHubNamespaceProcessorFactory(mock(CheckpointStore.class),
            namespaceProperties);
        processorAddedTimes = 0;
        this.processorFactory.addListener(new EventHubProcessorFactory.Listener() {
            @Override
            public void processorAdded(String eventHub, String consumerGroup) {
                processorAddedTimes++;
            }
        });
    }

    @Test
    void testGetEventProcessorClient() {
        EventProcessorClient processorClient = processorFactory.createProcessor(eventHubName, consumerGroup, listener);

        assertNotNull(processorClient);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testCreateEventProcessorClientTwice() {
        EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener);
        assertNotNull(client);

        processorFactory.createProcessor(eventHubName, consumerGroup, this.listener);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testRecreateEventProcessorClient() throws Exception {
        final EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener);
        assertNotNull(client);

        EventProcessorClient anotherClient = processorFactory.createProcessor(eventHubName, anotherConsumerGroup, this.listener);
        assertNotNull(anotherClient);
        assertEquals(2, processorAddedTimes);
    }

    static class BuilderReturn {
        private static Answer<?> self = (Answer<Object>) invocation -> {
            if (invocation.getMethod().getReturnType().isAssignableFrom(invocation.getMock().getClass())) {
                return invocation.getMock();
            }

            return null;
        };
    }
}
