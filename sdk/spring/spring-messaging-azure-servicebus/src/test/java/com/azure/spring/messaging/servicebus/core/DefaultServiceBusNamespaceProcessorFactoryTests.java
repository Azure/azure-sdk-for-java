// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultServiceBusNamespaceProcessorFactoryTests {
    private ServiceBusProcessorFactory processorFactory;
    private final String entityName = "test";
    private final String subscription = "subscription";
    private final String anotherSubscription = "subscription2";
    private final ServiceBusRecordMessageListener listener = messageContext -> { };
    private final ServiceBusErrorHandler errorHandler = errorContext -> { };
    private int queueProcessorAddedTimes = 0;
    private int topicProcessorAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.processorFactory = new DefaultServiceBusNamespaceProcessorFactory(namespaceProperties);
        queueProcessorAddedTimes = 0;
        topicProcessorAddedTimes = 0;
        this.processorFactory.addListener((name, client) -> {
            if (client.getSubscriptionName() == null) {
                queueProcessorAddedTimes++;
            } else {
                topicProcessorAddedTimes++;
            }
        });
    }

    @Test
    void testGetServiceBusProcessorClientForQueue() {
        ServiceBusProcessorClient processorClient = processorFactory.createProcessor(entityName, listener, errorHandler);
        assertNotNull(processorClient);
        assertEquals(0, topicProcessorAddedTimes);
        assertEquals(1, queueProcessorAddedTimes);
    }

    @Test
    void testGetServiceBusProcessorClientForTopic() {
        ServiceBusProcessorClient processorClient = processorFactory.createProcessor(entityName, subscription, listener, errorHandler);

        assertNotNull(processorClient);
        assertEquals(1, topicProcessorAddedTimes);
        assertEquals(0, queueProcessorAddedTimes);
    }

    @Test
    void testCreateServiceBusProcessorClientQueueTwice() {
        ServiceBusProcessorClient client = processorFactory.createProcessor(entityName, this.listener, errorHandler);
        assertNotNull(client);

        processorFactory.createProcessor(entityName, subscription, this.listener, errorHandler);
        assertEquals(1, queueProcessorAddedTimes);
    }

    @Test
    void testCreateServiceBusProcessorClientTopicTwice() {
        ServiceBusProcessorClient client = processorFactory.createProcessor(entityName, subscription, this.listener, errorHandler);
        assertNotNull(client);

        processorFactory.createProcessor(entityName, subscription, this.listener, errorHandler);
        assertEquals(1, topicProcessorAddedTimes);
    }

    @Test
    void testRecreateServiceBusProcessorClient() {
        final ServiceBusProcessorClient client = processorFactory.createProcessor(entityName, subscription, this.listener, errorHandler);
        assertNotNull(client);

        ServiceBusProcessorClient anotherClient = processorFactory.createProcessor(entityName, anotherSubscription, this.listener, errorHandler);
        assertNotNull(anotherClient);
        assertEquals(2, topicProcessorAddedTimes);
    }

    @Test
    void customizerShouldBeCalledOnEachCreatedClient() {
        AtomicInteger shareClientCalledTimes = new AtomicInteger();
        AtomicInteger noneSessionClientCalledTimes = new AtomicInteger();
        AtomicInteger sessionClientCalledTimes = new AtomicInteger();
        DefaultServiceBusNamespaceProcessorFactory factory = (DefaultServiceBusNamespaceProcessorFactory) this.processorFactory;

        factory.addServiceBusClientBuilderCustomizer(builder -> shareClientCalledTimes.getAndIncrement());
        factory.addBuilderCustomizer(builder -> noneSessionClientCalledTimes.getAndIncrement());
        factory.addSessionBuilderCustomizer(builder -> sessionClientCalledTimes.getAndIncrement());

        ServiceBusContainerProperties queue1ContainerProperties = new ServiceBusContainerProperties();
        queue1ContainerProperties.setMessageListener(this.listener);
        queue1ContainerProperties.setErrorHandler(this.errorHandler);
        queue1ContainerProperties.setSessionEnabled(false);
        factory.createProcessor("queue-1", queue1ContainerProperties);

        ServiceBusContainerProperties queue2ContainerProperties = new ServiceBusContainerProperties();
        queue2ContainerProperties.setMessageListener(this.listener);
        queue2ContainerProperties.setErrorHandler(this.errorHandler);
        queue2ContainerProperties.setSessionEnabled(true);
        factory.createProcessor("queue-2", queue2ContainerProperties);

        ServiceBusContainerProperties topic1ContainerProperties = new ServiceBusContainerProperties();
        topic1ContainerProperties.setMessageListener(this.listener);
        topic1ContainerProperties.setErrorHandler(this.errorHandler);
        topic1ContainerProperties.setSessionEnabled(false);
        factory.createProcessor("topic-1", "sub-1", topic1ContainerProperties);

        ServiceBusContainerProperties topic2ContainerProperties = new ServiceBusContainerProperties();
        topic2ContainerProperties.setMessageListener(this.listener);
        topic2ContainerProperties.setErrorHandler(this.errorHandler);
        topic2ContainerProperties.setSessionEnabled(true);
        factory.createProcessor("topic-2", "sub-1", topic2ContainerProperties);

        assertEquals(4, shareClientCalledTimes.get());
        assertEquals(2, noneSessionClientCalledTimes.get());
        assertEquals(2, sessionClientCalledTimes.get());
    }

    @Test
    void dedicatedCustomizerShouldBeCalledOnlyWhenMatchingClientsCreated() {
        AtomicInteger shareClientCalledTimes = new AtomicInteger();
        AtomicInteger noneSessionClientCalledTimes = new AtomicInteger();
        AtomicInteger sessionClientCalledTimes = new AtomicInteger();
        DefaultServiceBusNamespaceProcessorFactory factory = (DefaultServiceBusNamespaceProcessorFactory) this.processorFactory;

        factory.addServiceBusClientBuilderCustomizer(builder -> shareClientCalledTimes.getAndIncrement());
        factory.addBuilderCustomizer("queue-1", null, builder -> noneSessionClientCalledTimes.getAndIncrement());
        factory.addSessionBuilderCustomizer("queue-1", null, builder -> sessionClientCalledTimes.getAndIncrement());

        factory.addBuilderCustomizer("queue-2", null, builder -> noneSessionClientCalledTimes.getAndIncrement());
        factory.addSessionBuilderCustomizer("queue-2", null, builder -> sessionClientCalledTimes.getAndIncrement());

        ServiceBusContainerProperties queue1ContainerProperties = new ServiceBusContainerProperties();
        queue1ContainerProperties.setMessageListener(this.listener);
        queue1ContainerProperties.setErrorHandler(this.errorHandler);
        queue1ContainerProperties.setSessionEnabled(false);
        factory.createProcessor("queue-1", queue1ContainerProperties);

        ServiceBusContainerProperties queue2ContainerProperties = new ServiceBusContainerProperties();
        queue2ContainerProperties.setMessageListener(this.listener);
        queue2ContainerProperties.setErrorHandler(this.errorHandler);
        queue2ContainerProperties.setSessionEnabled(true);
        factory.createProcessor("queue-2", queue2ContainerProperties);

        ServiceBusContainerProperties topic1ContainerProperties = new ServiceBusContainerProperties();
        topic1ContainerProperties.setMessageListener(this.listener);
        topic1ContainerProperties.setErrorHandler(this.errorHandler);
        topic1ContainerProperties.setSessionEnabled(false);
        factory.createProcessor("topic-1", "sub-1", topic1ContainerProperties);

        ServiceBusContainerProperties topic2ContainerProperties = new ServiceBusContainerProperties();
        topic2ContainerProperties.setMessageListener(this.listener);
        topic2ContainerProperties.setErrorHandler(this.errorHandler);
        topic2ContainerProperties.setSessionEnabled(true);
        factory.createProcessor("topic-2", "sub-1", topic2ContainerProperties);

        assertEquals(4, shareClientCalledTimes.get());
        assertEquals(1, noneSessionClientCalledTimes.get());
        assertEquals(1, sessionClientCalledTimes.get());
    }

}
