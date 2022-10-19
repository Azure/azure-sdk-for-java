// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;

/**
 * The strategy to produce {@link ServiceBusProcessorClient} instance.
 */
public interface ServiceBusProcessorFactory {

    /**
     * Create a {@link ServiceBusProcessorClient} to consume events from the specified queue.
     *
     * @param queue The queue name.
     * @param messageListener message listener to be registered on service bus processor client.
     * @param errorHandler the error handler to consume the errors.
     *
     * @return ServiceBusProcessorClient queue processor client.
     */
    ServiceBusProcessorClient createProcessor(String queue,
                                              MessageListener<?> messageListener,
                                              ServiceBusErrorHandler errorHandler);

    /**
     * Create a {@link ServiceBusProcessorClient} to consume events from the specified queue.
     *
     * @param queue The queue name.
     * @param containerProperties the {@link ServiceBusContainerProperties} to describe the processor.
     *
     * @return the queue processor client.
     */
    ServiceBusProcessorClient createProcessor(String queue, ServiceBusContainerProperties containerProperties);

    /**
     * Create a {@link ServiceBusProcessorClient} to consume events from the specified topic in the context of the given
     * subscription.
     *
     * @param topic The topic.
     * @param subscription The subscription.
     * @param messageListener message listener to be registered on service bus processor client.
     * @param errorHandler the error handler to consume the errors.
     *
     * @return the topic processor client.
     */
    ServiceBusProcessorClient createProcessor(String topic,
                                              String subscription,
                                              MessageListener<?> messageListener,
                                              ServiceBusErrorHandler errorHandler);

    /**
     * Create a {@link ServiceBusProcessorClient} to consume events from the specified topic in the context of the given
     * subscription.
     *
     * @param topic The topic.
     * @param subscription The subscription.
     * @param containerProperties the {@link ServiceBusContainerProperties} to describe the processor.
     *
     * @return the topic processor client.
     */
    ServiceBusProcessorClient createProcessor(String topic,
                                              String subscription,
                                              ServiceBusContainerProperties containerProperties);

    /**
     * Add a listener for this factory.
     *
     * @param listener the listener.
     */
    default void addListener(Listener listener) {

    }

    /**
     * Remove a listener from the factory.
     *
     * @param listener the listener.
     *
     * @return true if removed.
     */
    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a processor is added or removed.
     */
    @FunctionalInterface
    interface Listener {

        /**
         * The callback method that the processor has been added.
         *
         * @param name the name for the processor.
         * @param client the client for the processor.
         */
        void processorAdded(String name, ServiceBusProcessorClient client);

        /**
         * The default callback method that the processor has been removed.
         *
         * @param name the name for the processor.
         * @param client the client for the processor.
         */
        default void processorRemoved(String name, ServiceBusProcessorClient client) {
        }

    }

}
