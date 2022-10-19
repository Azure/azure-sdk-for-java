// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
        this.producerFactory.addListener((name, client) -> producerAddedTimes++);
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

    @Test
    void customizerShouldBeCalledOnEachCreatedClient() {
        AtomicInteger calledTimes = new AtomicInteger();
        DefaultEventHubsNamespaceProducerFactory factory = (DefaultEventHubsNamespaceProducerFactory) this.producerFactory;

        factory.addBuilderCustomizer(builder -> calledTimes.getAndIncrement());

        factory.createProducer("eventhub-1");
        factory.createProducer("eventhub-2");
        factory.createProducer("eventhub-3");

        assertEquals(3, calledTimes.get());
    }

    @Test
    void dedicatedCustomizerShouldBeCalledOnlyWhenMatchingClientsCreated() {
        AtomicInteger customizer1CalledTimes = new AtomicInteger();
        AtomicInteger customizer2CalledTimes = new AtomicInteger();
        DefaultEventHubsNamespaceProducerFactory factory = (DefaultEventHubsNamespaceProducerFactory) this.producerFactory;

        factory.addBuilderCustomizer("eventhub-1", builder -> customizer1CalledTimes.getAndIncrement());
        factory.addBuilderCustomizer("eventhub-2", builder -> customizer2CalledTimes.getAndIncrement());

        factory.createProducer("eventhub-1");
        factory.createProducer("eventhub-2");
        factory.createProducer("eventhub-3");
        factory.createProducer("eventhub-4");

        assertEquals(1, customizer1CalledTimes.get());
        assertEquals(1, customizer2CalledTimes.get());
    }

}
