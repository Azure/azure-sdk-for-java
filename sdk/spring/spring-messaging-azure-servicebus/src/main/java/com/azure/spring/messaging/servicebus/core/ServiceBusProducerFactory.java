// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;


import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

/**
 * The strategy to produce {@link ServiceBusSenderAsyncClient} instance.
 */
public interface ServiceBusProducerFactory {

    /**
     * Create {@link ServiceBusSenderAsyncClient} to send events to the Service Bus queue/topic entity.
     * @param name the destination entity name
     * @return the producer.
     */
    ServiceBusSenderAsyncClient createProducer(String name);

    /**
     * Create {@link ServiceBusSenderAsyncClient} to send events to the Service Bus queue/topic entity with
     * explicit {@link ServiceBusEntityType}.
     * @param name the destination entity name.
     * @param entityType the Service Bus entity type.
     * @return the producer.
     */
    ServiceBusSenderAsyncClient createProducer(String name, ServiceBusEntityType entityType);

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
     * Called whenever a producer is added or removed.
     */
    @FunctionalInterface
    interface Listener {

        /**
         * The callback method that the producer has been added.
         * @param name the name for the producer.
         * @param client the client for the producer.
         */
        void producerAdded(String name, ServiceBusSenderAsyncClient client);

        /**
         * The default callback method that the producer has been removed.
         * @param name the name for the producer.
         * @param client the client for the producer.
         */
        default void producerRemoved(String name, ServiceBusSenderAsyncClient client) {
        }

    }
}
