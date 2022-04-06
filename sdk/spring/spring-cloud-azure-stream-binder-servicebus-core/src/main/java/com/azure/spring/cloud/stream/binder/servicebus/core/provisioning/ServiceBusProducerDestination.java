// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 *
 */
public class ServiceBusProducerDestination implements ProducerDestination {

    private final String name;

    /**
     * Construct a {@link ServiceBusProducerDestination} with the specified name.
     *
     * @param name the name
     */
    public ServiceBusProducerDestination(String name) {
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
}
