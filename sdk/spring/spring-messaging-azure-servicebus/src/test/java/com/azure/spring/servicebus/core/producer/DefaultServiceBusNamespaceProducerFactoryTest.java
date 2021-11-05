// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.producer;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultServiceBusNamespaceProducerFactoryTest {
    private ServiceBusProducerFactory producerFactory;
    private final String entityName = "serviceBus";
    private int producerAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.producerFactory = new DefaultServiceBusNamespaceProducerFactory(namespaceProperties);
        producerAddedTimes = 0;
        this.producerFactory.addListener((name) -> producerAddedTimes++);
    }

    @Test
    void testCreateEventProducerClient() {
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);

        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateEventProducerClientTwice() {
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);
        assertNotNull(producer);

        producer = producerFactory.createProducer(entityName);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testRecreateEventProducerClient() {
        final ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);
        assertNotNull(producer);

        String anotherEventHubName = "eventHub2";
        final ServiceBusSenderAsyncClient anotherProducer = producerFactory.createProducer(anotherEventHubName);
        assertNotNull(anotherProducer);
        assertEquals(2, producerAddedTimes);
    }

}
