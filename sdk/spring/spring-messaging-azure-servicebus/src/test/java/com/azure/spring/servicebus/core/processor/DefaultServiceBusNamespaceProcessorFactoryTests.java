// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.implementation.core.DefaultServiceBusNamespaceProcessorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultServiceBusNamespaceProcessorFactoryTests {
    private ServiceBusProcessorFactory processorFactory;
    private final String entityName = "test";
    private final String subscription = "subscription";
    private final String anotherSubscription = "subscription2";
    private final RecordMessageProcessingListener listener = messageContext -> { };
    private int queueProcessorAddedTimes = 0;
    private int topicProcessorAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.processorFactory = new DefaultServiceBusNamespaceProcessorFactory(namespaceProperties);
        queueProcessorAddedTimes = 0;
        topicProcessorAddedTimes = 0;
        this.processorFactory.addListener((name, subscription, client) -> {
            if (subscription == null) {
                queueProcessorAddedTimes++;
            } else {
                topicProcessorAddedTimes++;
            }
        });
    }

    @Test
    void testGetServiceBusProcessorClientForQueue() {
        ServiceBusProcessorClient processorClient = processorFactory.createProcessor(entityName, listener);
        assertNotNull(processorClient);
        assertEquals(0, topicProcessorAddedTimes);
        assertEquals(1, queueProcessorAddedTimes);
    }

    @Test
    void testGetServiceBusProcessorClientForTopic() {
        ServiceBusProcessorClient processorClient = processorFactory.createProcessor(entityName, subscription, listener);

        assertNotNull(processorClient);
        assertEquals(1, topicProcessorAddedTimes);
        assertEquals(0, queueProcessorAddedTimes);
    }

    @Test
    void testCreateServiceBusProcessorClientQueueTwice() {
        ServiceBusProcessorClient client = processorFactory.createProcessor(entityName, this.listener);
        assertNotNull(client);

        processorFactory.createProcessor(entityName, subscription, this.listener);
        assertEquals(1, queueProcessorAddedTimes);
    }

    @Test
    void testCreateServiceBusProcessorClientTopicTwice() {
        ServiceBusProcessorClient client = processorFactory.createProcessor(entityName, subscription, this.listener);
        assertNotNull(client);

        processorFactory.createProcessor(entityName, subscription, this.listener);
        assertEquals(1, topicProcessorAddedTimes);
    }

    @Test
    void testRecreateServiceBusProcessorClient() {
        final ServiceBusProcessorClient client = processorFactory.createProcessor(entityName, subscription, this.listener);
        assertNotNull(client);

        ServiceBusProcessorClient anotherClient = processorFactory.createProcessor(entityName, anotherSubscription, this.listener);
        assertNotNull(anotherClient);
        assertEquals(2, topicProcessorAddedTimes);
    }

}
