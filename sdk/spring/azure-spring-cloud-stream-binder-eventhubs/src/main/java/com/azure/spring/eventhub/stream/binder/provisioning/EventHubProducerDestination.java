// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * @author Warren Zhu
 */
public class EventHubProducerDestination implements ProducerDestination {

    private final String name;

    public EventHubProducerDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getNameForPartition(int partition) {
        return this.name + "-" + partition;
    }

    @Override
    public String toString() {
        return "EventHubProducerDestination{" + "name='" + name + '\'' + '}';
    }
}
