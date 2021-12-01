// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

/**
 * @author Warren Zhu
 */
public class ServiceBusConsumerDestination implements ConsumerDestination {

    private final String name;

    /**
     *
     * @param name The name.
     */
    public ServiceBusConsumerDestination(String name) {
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
}
