// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.producer;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void testCreateServiceBusSenderClientWithoutEntityTypeFail() {
        assertThrows(IllegalArgumentException.class, () -> producerFactory.createProducer(entityName));
    }

    @Test
    void testCreateServiceBusSenderClientWithEntityType() {
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName, ServiceBusEntityType.QUEUE);
        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateServiceBusSenderClientWithPropertiesSupplier() {
        producerFactory = new DefaultServiceBusNamespaceProducerFactory(new NamespaceProperties() {{ setNamespace("test-namespace"); }},
                entityName -> new ProducerProperties() {{
                        setType(ServiceBusEntityType.QUEUE);
                        setName(entityName);
                    }});
        this.producerFactory.addListener((name) -> producerAddedTimes++);
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName);
        assertNotNull(producer);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testCreateServiceBusSenderClientTwice() {
        ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName, ServiceBusEntityType.QUEUE);
        assertNotNull(producer);

        producer = producerFactory.createProducer(entityName, ServiceBusEntityType.TOPIC);
        assertEquals(1, producerAddedTimes);
    }

    @Test
    void testRecreateServiceBusSenderClient() {
        final ServiceBusSenderAsyncClient producer = producerFactory.createProducer(entityName,
            ServiceBusEntityType.QUEUE);
        assertNotNull(producer);

        String anotherEntityName = "serviceBus2";
        final ServiceBusSenderAsyncClient anotherProducer = producerFactory.createProducer(anotherEntityName,
            ServiceBusEntityType.QUEUE);
        assertNotNull(anotherProducer);
        assertEquals(2, producerAddedTimes);
    }

}
