// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.producer;


import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

/**
 * Factory to return functional creator of service bus sender
 *
 * @author Warren Zhu
 */
public interface ServiceBusProducerFactory {

    /**
     * Return a function which accepts service bus topic or queue name, then returns {@link ServiceBusSenderClient}
     *
     * @param name entity name
     * @return message sender implement instance
     */
    ServiceBusSenderAsyncClient createProducer(String name);

    /**
     * Return a function which accepts service bus topic or queue name, then returns {@link ServiceBusSenderClient}
     *
     * @param name entity name of the sender destination
     * @param entityType entity type of the sender destination
     * @return message sender implement instance
     */
    ServiceBusSenderAsyncClient createProducer(String name, ServiceBusEntityType entityType);

    default void addListener(Listener listener) {

    }

    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a producer is added or removed.
     */
    @FunctionalInterface
    interface Listener {

        void producerAdded(String name, ServiceBusSenderAsyncClient client);

        default void producerRemoved(String name, ServiceBusSenderAsyncClient client) {
        }

    }
}
