// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.processor;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;

/**
 * The strategy to produce {@link EventProcessorClient} instance.
 */
public interface EventHubsProcessorFactory {

    EventProcessorClient createProcessor(String eventHub, String consumerGroup, EventProcessingListener listener);

    default void addListener(Listener listener) {

    }

    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a processor is added or removed.
     */
    interface Listener {

        default void processorAdded(String eventHub, String consumerGroup) {

        }

        default void processorRemoved(String eventHub, String consumerGroup) {
        }

    }

}
