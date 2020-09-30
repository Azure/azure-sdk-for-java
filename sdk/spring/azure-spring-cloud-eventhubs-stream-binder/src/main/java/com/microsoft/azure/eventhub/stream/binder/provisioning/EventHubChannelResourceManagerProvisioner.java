// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhub.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.context.core.impl.EventHubConsumerGroupManager;
import com.microsoft.azure.spring.cloud.context.core.impl.EventHubManager;
import com.microsoft.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;

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
            @NonNull EventHubConsumerGroupManager eventHubConsumerGroupManager, @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.eventHubNamespaceManager = eventHubNamespaceManager;
        this.eventHubManager = eventHubManager;
        this.eventHubConsumerGroupManager = eventHubConsumerGroupManager;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        EventHubNamespace eventHubNamespace = eventHubNamespaceManager.getOrCreate(namespace);
        EventHub eventHub = eventHubManager.get(Tuple.of(eventHubNamespace, name));
        if (eventHub == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }

        eventHubConsumerGroupManager.getOrCreate(Tuple.of(eventHub, group));
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        EventHubNamespace eventHubNamespace = eventHubNamespaceManager.getOrCreate(namespace);
        eventHubManager.getOrCreate(Tuple.of(eventHubNamespace, name));
    }
}
