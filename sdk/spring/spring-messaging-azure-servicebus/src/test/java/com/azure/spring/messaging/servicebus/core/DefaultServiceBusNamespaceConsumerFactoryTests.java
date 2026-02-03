// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultServiceBusNamespaceConsumerFactoryTests {
    private ServiceBusConsumerFactory consumerFactory;
    private int queueReceiverAddedTimes = 0;
    private int topicReceiverAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        PropertiesSupplier<ConsumerIdentifier, ConsumerProperties> supplier = key -> {
            if (key.getDestination().startsWith("queue-") || key.getDestination().startsWith("topic-")) {
                ConsumerProperties consumerProperties = new ProcessorProperties();
                consumerProperties.setEntityName(key.getDestination());
                consumerProperties.setSessionEnabled(true);
                if (key.getDestination().startsWith("topic-")) {
                    consumerProperties.setSubscriptionName("test-sub");
                }
                return consumerProperties;
            }
            return null;
        };
        this.consumerFactory = new DefaultServiceBusNamespaceConsumerFactory(namespaceProperties, supplier);
        queueReceiverAddedTimes = 0;
        topicReceiverAddedTimes = 0;
        this.consumerFactory.addListener((name, client) -> {
            if (name.startsWith("queue-")) {
                queueReceiverAddedTimes++;
            } else {
                topicReceiverAddedTimes++;
            }
        });
    }

    @Test
    void getSessionReceiverClientForQueue() {
        ServiceBusSessionReceiverClient sessionReceiverClient = consumerFactory.createReceiver("queue-1", ServiceBusEntityType.QUEUE);
        assertNotNull(sessionReceiverClient);
        assertEquals(0, topicReceiverAddedTimes);
        assertEquals(1, queueReceiverAddedTimes);
    }

    @Test
    void getSessionReceiverClientForTopic() {
        ServiceBusSessionReceiverClient sessionReceiverClient = consumerFactory.createReceiver("topic-1", ServiceBusEntityType.TOPIC);
        assertNotNull(sessionReceiverClient);
        assertEquals(1, topicReceiverAddedTimes);
        assertEquals(0, queueReceiverAddedTimes);
    }

    @Test
    void createSessionReceiverClientQueueTwice() {
        ServiceBusSessionReceiverClient sessionReceiverClient = consumerFactory.createReceiver("queue-1", ServiceBusEntityType.QUEUE);
        assertNotNull(sessionReceiverClient);

        consumerFactory.createReceiver("queue-1", ServiceBusEntityType.QUEUE);
        assertEquals(1, queueReceiverAddedTimes);
    }

    @Test
    void createSessionReceiverClientTopicTwice() {
        ServiceBusSessionReceiverClient sessionReceiverClient = consumerFactory.createReceiver("topic-1", ServiceBusEntityType.TOPIC);
        assertNotNull(sessionReceiverClient);

        consumerFactory.createReceiver("topic-1", ServiceBusEntityType.TOPIC);
        assertEquals(1, topicReceiverAddedTimes);
    }

    @Test
    void createMultipleSessionReceiverClients() {
        ServiceBusSessionReceiverClient topicSessionReceiverClient = consumerFactory.createReceiver("topic-1", ServiceBusEntityType.QUEUE);
        assertNotNull(topicSessionReceiverClient);

        ServiceBusSessionReceiverClient anotherTopicSessionReceiverClient = consumerFactory.createReceiver("topic-2", ServiceBusEntityType.TOPIC);
        assertNotNull(anotherTopicSessionReceiverClient);
        assertEquals(2, topicReceiverAddedTimes);
    }

    @Test
    void customizerShouldBeCalledOnEachCreatedClient() {
        AtomicInteger shareClientCalledTimes = new AtomicInteger();
        AtomicInteger sessionClientCalledTimes = new AtomicInteger();
        AtomicInteger dedicatedSessionClientCalledTimes = new AtomicInteger();

        DefaultServiceBusNamespaceConsumerFactory factory = (DefaultServiceBusNamespaceConsumerFactory) this.consumerFactory;

        factory.addServiceBusClientBuilderCustomizer(builder -> shareClientCalledTimes.getAndIncrement());
        factory.addBuilderCustomizer(builder -> sessionClientCalledTimes.getAndIncrement());

        factory.addBuilderCustomizer("queue-1", builder -> dedicatedSessionClientCalledTimes.getAndIncrement());
        factory.addBuilderCustomizer("topic-1", builder -> dedicatedSessionClientCalledTimes.getAndIncrement());

        factory.createReceiver("queue-1", ServiceBusEntityType.QUEUE);
        factory.createReceiver("queue-2", ServiceBusEntityType.QUEUE);

        factory.createReceiver("topic-1", ServiceBusEntityType.TOPIC);
        factory.createReceiver("topic-2", ServiceBusEntityType.TOPIC);

        assertEquals(4, shareClientCalledTimes.get());
        assertEquals(4, sessionClientCalledTimes.get());
        assertEquals(2, dedicatedSessionClientCalledTimes.get());
    }
}
