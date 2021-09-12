// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.provisioning;

import com.azure.spring.integration.eventhub.factory.EventHubProvisioner;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class EventHubChannelResourceManagerProvisioner extends EventHubChannelProvisioner {

    private final String namespace;
    private final EventHubProvisioner eventHubProvisioner;

    public EventHubChannelResourceManagerProvisioner(@NonNull String namespace,
                                                     @NonNull EventHubProvisioner eventHubProvisioner) {
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
