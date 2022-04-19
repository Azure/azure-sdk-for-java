// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

class DefaultEventHubsNamespaceProcessorFactoryTests {

    private EventHubsProcessorFactory processorFactory;
    private final String eventHubName = "eventHub";
    private final String consumerGroup = "group";
    private final String anotherConsumerGroup = "group2";
    private final EventHubsRecordMessageListener listener = eventContext -> { };
    private final EventHubsErrorHandler errorHandler = errorContext -> { };
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
        EventProcessorClient processorClient = processorFactory.createProcessor(eventHubName, consumerGroup, listener, errorHandler);

        assertNotNull(processorClient);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testCreateEventProcessorClientTwice() {
        EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, errorHandler);
        assertNotNull(client);

        processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, errorHandler);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testRecreateEventProcessorClient() {
        final EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, errorHandler);
        assertNotNull(client);

        EventProcessorClient anotherClient = processorFactory.createProcessor(eventHubName, anotherConsumerGroup, this.listener, errorHandler);
        assertNotNull(anotherClient);
        assertEquals(2, processorAddedTimes);
    }

    @Test
    void customizerShouldBeCalledOnEachCreatedClient() {
        AtomicInteger calledTimes = new AtomicInteger();
        DefaultEventHubsNamespaceProcessorFactory factory = (DefaultEventHubsNamespaceProcessorFactory) this.processorFactory;

        factory.addBuilderCustomizer(builder -> calledTimes.getAndIncrement());

        factory.createProcessor("eventhub-1", "consumer-group-1", this.listener, this.errorHandler);
        factory.createProcessor("eventhub-1", "consumer-group-2", this.listener, this.errorHandler);
        factory.createProcessor("eventhub-2", "consumer-group-1", this.listener, this.errorHandler);
        factory.createProcessor("eventhub-2", "consumer-group-2", this.listener, this.errorHandler);

        assertEquals(4, calledTimes.get());
    }

    @Test
    void dedicatedCustomizerShouldBeCalledOnlyWhenMatchingClientsCreated() {
        AtomicInteger customizer1CalledTimes = new AtomicInteger();
        AtomicInteger customizer2CalledTimes = new AtomicInteger();
        DefaultEventHubsNamespaceProcessorFactory factory = (DefaultEventHubsNamespaceProcessorFactory) this.processorFactory;

        factory.addBuilderCustomizer("eventhub-1", "consumer-group-1", builder -> customizer1CalledTimes.getAndIncrement());
        factory.addBuilderCustomizer("eventhub-1", "consumer-group-2", builder -> customizer2CalledTimes.getAndIncrement());

        factory.createProcessor("eventhub-1", "consumer-group-1", this.listener, this.errorHandler);
        factory.createProcessor("eventhub-1", "consumer-group-2", this.listener, this.errorHandler);
        factory.createProcessor("eventhub-2", "consumer-group-1", this.listener, this.errorHandler);
        factory.createProcessor("eventhub-2", "consumer-group-2", this.listener, this.errorHandler);

        assertEquals(1, customizer1CalledTimes.get());
        assertEquals(1, customizer2CalledTimes.get());
    }

}
