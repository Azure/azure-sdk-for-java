/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * @author Warren Zhu
 */
public class ServiceBusProducerDestination implements ProducerDestination {

    private String name;

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
