// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.provisioning;

import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.spring.cloud.context.core.impl.EventHubConsumerGroupManager;
import com.azure.spring.cloud.context.core.impl.EventHubManager;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.core.util.Tuple;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class EventHubChannelResourceManagerProvisioner extends EventHubChannelProvisioner {
    private final String namespace;
    private final EventHubNamespaceManager eventHubNamespaceManager;
    private final EventHubManager eventHubManager;
    private final EventHubConsumerGroupManager eventHubConsumerGroupManager;

    public EventHubChannelResourceManagerProvisioner(@NonNull EventHubNamespaceManager eventHubNamespaceManager,
                                                     @NonNull EventHubManager eventHubManager,
                                                     @NonNull EventHubConsumerGroupManager eventHubConsumerGroupManager,
                                                     @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.eventHubNamespaceManager = eventHubNamespaceManager;
        this.eventHubManager = eventHubManager;
        this.eventHubConsumerGroupManager = eventHubConsumerGroupManager;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        EventHubNamespace eventHubNamespace = eventHubNamespaceManager.getOrCreate(namespace);
        // If the consumer is created before the producer, we need to create the event
        // hub with it.
        // Otherwise, this method will fail and the startup of a distributed
        // application, where no order of operations can be imposed, will fail with it.
        EventHub eventHub = eventHubManager.getOrCreate(Tuple.of(eventHubNamespace, name));
        if (eventHub == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' could not be created", name, namespace));
        }
        eventHubConsumerGroupManager.getOrCreate(Tuple.of(eventHub, group));
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        EventHubNamespace eventHubNamespace = eventHubNamespaceManager.getOrCreate(namespace);
        eventHubManager.getOrCreate(Tuple.of(eventHubNamespace, name));
    }
}
