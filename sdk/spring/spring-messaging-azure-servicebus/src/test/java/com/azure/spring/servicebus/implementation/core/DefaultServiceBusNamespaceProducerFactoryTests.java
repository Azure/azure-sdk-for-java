// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.implementation.core;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultServiceBusNamespaceProducerFactoryTests {
    private ServiceBusProducerFactory producerFactory;
    private final String entityName = "serviceBus";
    private int producerAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        namespaceProperties.setEntityName(entityName);
        namespaceProperties.setEntityType(ServiceBusEntityType.QUEUE);
        this.producerFactory = new DefaultServiceBusNamespaceProducerFactory(namespaceProperties);
        producerAddedTimes = 0;
        this.producerFactory.addListener((name, client) -> producerAddedTimes++);
    }

    @Test
    void testCreateServiceBusSenderClient() {
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);
        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateServiceBusSenderClientWithEntityType() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.producerFactory = new DefaultServiceBusNamespaceProducerFactory(namespaceProperties);
        producerAddedTimes = 0;
        this.producerFactory.addListener((name, client) -> producerAddedTimes++);

        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName, ServiceBusEntityType.QUEUE);

        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateServiceBusSenderClientTwice() {
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);
        assertNotNull(producer);

        producerFactory.createProducer(entityName);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testRecreateServiceBusSenderClient() {
        final ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);
        assertNotNull(producer);

        String anotherEntityName = "serviceBus2";
        final ServiceBusSenderAsyncClient anotherProducer = producerFactory.createProducer(anotherEntityName);
        assertNotNull(anotherProducer);
        assertEquals(2, producerAddedTimes);
    }

}
