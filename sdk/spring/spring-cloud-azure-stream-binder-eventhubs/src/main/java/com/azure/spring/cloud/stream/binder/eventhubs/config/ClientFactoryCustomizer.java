// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.producer.EventHubsProducerFactory;

/**
 * Called by the binder to customize the factories.
 */
public interface ClientFactoryCustomizer {

    /**
     * Customize the producer factory.
     * @param factory The Event Hubs producer factory.
     */
    default void customize(EventHubsProducerFactory factory) {
    }

    /**
     * Customize the processor factory.
     * @param factory The Event Hubs processor factory.
     */
    default void customize(EventHubsProcessorFactory factory) {
    }

}
