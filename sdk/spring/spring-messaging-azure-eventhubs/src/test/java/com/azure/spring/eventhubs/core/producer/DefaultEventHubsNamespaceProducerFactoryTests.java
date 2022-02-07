// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultEventHubsNamespaceProducerFactoryTests {

    private EventHubsProducerFactory producerFactory;
    private final String eventHubName = "eventHub";
    private int producerAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.producerFactory = new DefaultEventHubsNamespaceProducerFactory(namespaceProperties);
        producerAddedTimes = 0;
        this.producerFactory.addListener(new EventHubsProducerFactory.Listener() {
            @Override
            public void producerAdded(String name, EventHubProducerAsyncClient client) {
                producerAddedTimes++;
            }
        });
    }

    @Test
    void testCreateEventProducerClient() {
        EventHubProducerAsyncClient producer = producerFactory.createProducer(eventHubName);

        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateEventProducerClientTwice() {
        EventHubProducerAsyncClient producer = producerFactory.createProducer(eventHubName);
        assertNotNull(producer);

        producer = producerFactory.createProducer(eventHubName);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testRecreateEventProducerClient() {
        final EventHubProducerAsyncClient producer = producerFactory.createProducer(eventHubName);
        assertNotNull(producer);

        String anotherEventHubName = "eventHub2";
        final EventHubProducerAsyncClient anotherProducer = producerFactory.createProducer(anotherEventHubName);
        assertNotNull(anotherProducer);
        assertEquals(2, producerAddedTimes);
    }

}
