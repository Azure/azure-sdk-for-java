// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.provisioning;

import com.azure.spring.eventhubs.provisioning.EventHubsProvisioner;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 *
 */
public class EventHubsChannelResourceManagerProvisioner extends EventHubsChannelProvisioner {

    private final String namespace;
    private final EventHubsProvisioner eventHubProvisioner;

    public EventHubsChannelResourceManagerProvisioner(@NonNull String namespace,
                                                      @NonNull EventHubsProvisioner eventHubProvisioner) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.eventHubProvisioner = eventHubProvisioner;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        eventHubProvisioner.provisionConsumerGroup(this.namespace, name, group);
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        eventHubProvisioner.provisionEventHub(this.namespace, name);
    }
}
