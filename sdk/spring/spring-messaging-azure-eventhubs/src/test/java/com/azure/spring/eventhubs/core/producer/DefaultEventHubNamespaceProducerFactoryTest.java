// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultEventHubNamespaceProducerFactoryTest {

    private EventHubProducerFactory producerFactory;
    private final String eventHubName = "eventHub";
    private int producerAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.producerFactory = new DefaultEventHubNamespaceProducerFactory(namespaceProperties);
        producerAddedTimes = 0;
        this.producerFactory.addListener(new EventHubProducerFactory.Listener() {
            @Override
            public void producerAdded(String name) {
                producerAddedTimes++;
            }
        });
    }

    @Test
    void testCreateEventProducerClient() {
        EventHubProducer producer = producerFactory.createProducer(eventHubName);

        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateEventProducerClientTwice() {
        EventHubProducer producer = producerFactory.createProducer(eventHubName);
        assertNotNull(producer);

        producer = producerFactory.createProducer(eventHubName);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testRecreateEventProducerClient() {
        final EventHubProducer producer = producerFactory.createProducer(eventHubName);
        assertNotNull(producer);

        String anotherEventHubName = "eventHub2";
        final EventHubProducer anotherProducer = producerFactory.createProducer(anotherEventHubName);
        assertNotNull(anotherProducer);
        assertEquals(2, producerAddedTimes);
    }

}
