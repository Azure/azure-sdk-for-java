// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

/**
 * The strategy to produce {@link ServiceBusProcessorClient} instance.
 */
public interface ServiceBusListenerFactory {

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

        /**
         * The callback method that the processor has been added.
         * @param name the name for the processor.
         * @param subscription the subscription for the processor.
         * @param client the client for the processor.
         */
        void processorAdded(String name, String subscription, ServiceBusProcessorClient client);

        /**
         * The default callback method that the processor has been removed.
         * @param name the name for the processor.
         * @param subscription the subscription for the processor.
         * @param client the client for the processor.
         */
        default void processorRemoved(String name, String subscription, ServiceBusProcessorClient client) {
        }

    }

}
