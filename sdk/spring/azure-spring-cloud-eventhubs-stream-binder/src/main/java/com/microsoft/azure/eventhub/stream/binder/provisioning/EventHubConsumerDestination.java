// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhub.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

/**
 * @author Warren Zhu
 */
public class EventHubConsumerDestination implements ConsumerDestination {

    private String name;

    public EventHubConsumerDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
