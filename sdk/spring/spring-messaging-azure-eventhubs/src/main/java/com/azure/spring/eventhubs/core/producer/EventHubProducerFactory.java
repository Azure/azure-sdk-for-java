// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;

/**
 * The strategy to produce {@link EventHubProducerAsyncClient} instance.
 */
public interface EventHubProducerFactory {

    EventHubProducer createProducer(String eventHub);

    default void addListener(Listener listener) {

    }

    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a producer is added or removed.
     */
    interface Listener {

        default void producerAdded(String name) {

        }

        default void producerRemoved(String name) {
        }

    }

}
