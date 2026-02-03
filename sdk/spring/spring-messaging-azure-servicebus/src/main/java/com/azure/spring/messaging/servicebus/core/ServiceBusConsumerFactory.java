// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;


import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

/**
 * The strategy to produce {@link ServiceBusSessionReceiverClient} instance.
 * @since 5.22.0
 */
public interface ServiceBusConsumerFactory {

    /**
     * Create a {@link ServiceBusSessionReceiverClient} to consume events from the specified queue.
     *
     * @param name the destination entity name.
     *
     * @return ServiceBusReceiverClient queue receiver client.
     */
    ServiceBusSessionReceiverClient createReceiver(String name);

    /**
     * Create a {@link ServiceBusSessionReceiverClient} to consume events from the specified queue.
     *
     * @param name the destination entity name.
     * @param entityType the Service Bus entity type.
     *
     * @return ServiceBusReceiverClient queue receiver client.
     */
    ServiceBusSessionReceiverClient createReceiver(String name, ServiceBusEntityType entityType);

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
     * Called whenever a consumer (session receiver client) is added or removed.
     */
    @FunctionalInterface
    interface Listener {

        /**
         * The callback method that the consumer (session receiver) has been added.
         *
         * @param name the name for the receiver.
         * @param client the client for the session receiver.
         */
        void consumerAdded(String name, ServiceBusSessionReceiverClient client);

        /**
         * The default callback method that the consumer (session receiver) has been removed.
         *
         * @param name the name for the receiver client.
         * @param client the client for the session receiver.
         */
        default void consumerRemoved(String name, ServiceBusSessionReceiverClient client) {
        }
    }

}
