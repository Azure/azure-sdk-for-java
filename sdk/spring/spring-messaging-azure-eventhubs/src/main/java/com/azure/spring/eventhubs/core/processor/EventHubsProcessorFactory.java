// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.processor;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;

/**
 * The strategy to produce {@link EventProcessorClient} instance.
 */
public interface EventHubsProcessorFactory {

    /**
     * Create an {@link EventProcessorClient} to consume events from the specified event hub in the context of the given
     * consumer group.
     * @param eventHub the event hub to consume events from
     * @param consumerGroup the consumer group
     * @param listener the {@link EventProcessingListener} to consume events with
     * @return the EventProcessorClient.
     */
    EventProcessorClient createProcessor(String eventHub, String consumerGroup, EventProcessingListener listener);

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
    interface Listener {

        /**
         * The callback method that the processor has been added.
         * @param eventHub the event hub name.
         * @param consumerGroup the consumer group.
         * @param client the client for the processor.
         */
        void processorAdded(String eventHub, String consumerGroup, EventProcessorClient client);

        /**
         * The default callback method that the processor has been removed.
         * @param eventHub the event hub name.
         * @param consumerGroup the consumer group.
         * @param client the client for the processor.
         */
        default void processorRemoved(String eventHub, String consumerGroup, EventProcessorClient client) {
        }

    }

}
