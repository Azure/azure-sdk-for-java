// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;

/**
 * The strategy to produce {@link ServiceBusProcessorClient} instance.
 */
public interface ServiceBusProcessorFactory {

    /**
     * Create a {@link ServiceBusProcessorClient} to consume events from the specified queue.
     * @param queue The queue name.
     * @param messageProcessingListener Callback processor listener to be registered on service bus processor client.
     * @return ServiceBusProcessorClient queue processor client
     */
    ServiceBusProcessorClient createProcessor(String queue,
                                              MessageProcessingListener messageProcessingListener);

    /**
     * Create a {@link ServiceBusProcessorClient} to consume events from the specified topic in the context of the given
     * subscription.
     *
     * @param topic The topic.
     * @param subscription The subscription.
     * @param messageProcessingListener The callback processor listener to be registered on service bus processor client.
     * @return subscription client
     */
    ServiceBusProcessorClient createProcessor(String topic,
                                              String subscription,
                                              MessageProcessingListener messageProcessingListener);

    /**
     * Add a listener for this factory.
     * @param listener the listener
     */
    default void addListener(Listener listener) {

    }

    /**
     * Remove a listener
     * @param listener the listener
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

        void processorAdded(String name, String subscription, ServiceBusProcessorClient client);

        default void processorRemoved(String name, String subscription, ServiceBusProcessorClient client) {
        }

    }

}
