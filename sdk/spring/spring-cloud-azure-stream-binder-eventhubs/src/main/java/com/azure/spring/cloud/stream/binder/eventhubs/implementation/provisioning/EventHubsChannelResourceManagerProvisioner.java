// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation.provisioning;

import com.azure.spring.cloud.resourcemanager.implementation.provisioning.EventHubsProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.core.implementation.provisioning.EventHubsChannelProvisioner;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 *
 */
public class EventHubsChannelResourceManagerProvisioner extends EventHubsChannelProvisioner {

    private final String namespace;
    private final EventHubsProvisioner eventHubsProvisioner;

    /**
     * Construct a {@link EventHubsChannelResourceManagerProvisioner} with the specified namespace and {@link EventHubsProvisioner}.
     *
     * @param namespace the namespace
     * @param eventHubsProvisioner the event Hubs Provisioner
     */
    public EventHubsChannelResourceManagerProvisioner(@NonNull String namespace,
                                                      @NonNull EventHubsProvisioner eventHubsProvisioner) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.eventHubsProvisioner = eventHubsProvisioner;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        eventHubsProvisioner.provisionConsumerGroup(this.namespace, name, group);
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        eventHubsProvisioner.provisionEventHub(this.namespace, name);
    }
}
