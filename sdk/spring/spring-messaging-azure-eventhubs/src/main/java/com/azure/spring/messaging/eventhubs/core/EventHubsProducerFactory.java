// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;

/**
 * The strategy to produce {@link EventHubProducerAsyncClient} instance.
 */
public interface EventHubsProducerFactory {

    /**
     * Create {@link EventHubProducerAsyncClient} to send events to the event hub.
     * @param eventHub the event hub
     * @return the producer.
     */
    EventHubProducerAsyncClient createProducer(String eventHub);

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
    interface Listener {

        /**
         * The callback method that the producer has been added.
         * @param name the name for the producer.
         * @param client the client for the producer.
         */
        void producerAdded(String name, EventHubProducerAsyncClient client);

        /**
         * The default callback method that the producer has been removed.
         * @param name the name for the producer.
         * @param client the client for the producer.
         */
        default void producerRemoved(String name, EventHubProducerAsyncClient client) {
        }

    }

}
