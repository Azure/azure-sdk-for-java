// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;


import com.azure.spring.messaging.eventhubs.core.EventHubsProducerFactory;

/**
 * Called by the binder to customize the  {@link EventHubsProducerFactory}.
 */
@FunctionalInterface
public interface EventHubsProducerFactoryCustomizer {

    /**
     * Customize the producer factory.
     *
     * @param factory The producer factory.
     */
    void customize(EventHubsProducerFactory factory);

}
