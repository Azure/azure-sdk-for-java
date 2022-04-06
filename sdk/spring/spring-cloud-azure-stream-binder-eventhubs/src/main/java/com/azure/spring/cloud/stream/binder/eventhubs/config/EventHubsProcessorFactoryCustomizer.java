// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;


import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;

/**
 * Called by the binder to customize the {@link EventHubsProcessorFactory}.
 */
@FunctionalInterface
public interface EventHubsProcessorFactoryCustomizer {

    /**
     * Customize the processor factory.
     *
     * @param factory The processor factory.
     */
    void customize(EventHubsProcessorFactory factory);

}
