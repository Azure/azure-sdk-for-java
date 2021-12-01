// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * @author Warren Zhu
 */
public class EventHubProducerDestination implements ProducerDestination {

    private final String name;

    /**
     *
     * @param name The name.
     */
    public EventHubProducerDestination(String name) {
        this.name = name;
    }

    /**
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     *
     * @param partition The partition.
     * @return The name.
     */
    @Override
    public String getNameForPartition(int partition) {
        return this.name + "-" + partition;
    }

    /**
     *
     * @return The string.
     */
    @Override
    public String toString() {
        return "EventHubProducerDestination{" + "name='" + name + '\'' + '}';
    }
}
