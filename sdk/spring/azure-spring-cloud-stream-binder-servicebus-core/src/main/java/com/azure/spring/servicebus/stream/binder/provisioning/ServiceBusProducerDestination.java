// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * @author Warren Zhu
 */
public class ServiceBusProducerDestination implements ProducerDestination {

    private final String name;

    /**
     *
     * @param name The name.
     */
    public ServiceBusProducerDestination(String name) {
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
     * @return The name for partition.
     */
    @Override
    public String getNameForPartition(int partition) {
        return this.name + "-" + partition;
    }
}
