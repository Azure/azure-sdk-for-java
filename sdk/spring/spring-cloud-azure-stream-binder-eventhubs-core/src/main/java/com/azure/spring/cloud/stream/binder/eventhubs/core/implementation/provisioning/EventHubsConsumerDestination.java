// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.implementation.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

/**
 *
 */
public class EventHubsConsumerDestination implements ConsumerDestination {

    private final String name;

    /**
     * Construct a {@link EventHubsConsumerDestination} with the specified binder.
     *
     * @param name the name
     */
    public EventHubsConsumerDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "EventHubConsumerDestination{" + "name='" + name + '\'' + '}';
    }
}
