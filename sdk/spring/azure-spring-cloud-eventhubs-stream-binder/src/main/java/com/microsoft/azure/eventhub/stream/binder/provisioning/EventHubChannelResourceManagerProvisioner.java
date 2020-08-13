// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhub.stream.binder.provisioning;

import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class EventHubChannelResourceManagerProvisioner extends EventHubChannelProvisioner {
    private final String namespace;
    private final ResourceManagerProvider resourceManagerProvider;

    public EventHubChannelResourceManagerProvisioner(@NonNull ResourceManagerProvider resourceManagerProvider,
            @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.resourceManagerProvider = resourceManagerProvider;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        EventHubNamespace eventHubNamespace =
                this.resourceManagerProvider.getEventHubNamespaceManager().getOrCreate(namespace);
        EventHub eventHub = this.resourceManagerProvider.getEventHubManager().get(Tuple.of(eventHubNamespace, name));
        if (eventHub == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }

        this.resourceManagerProvider.getEventHubConsumerGroupManager().getOrCreate(Tuple.of(eventHub, group));
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        if (resourceManagerProvider == null) {
            return;
        }
        EventHubNamespace eventHubNamespace =
                this.resourceManagerProvider.getEventHubNamespaceManager().getOrCreate(namespace);
        this.resourceManagerProvider.getEventHubManager().getOrCreate(Tuple.of(eventHubNamespace, name));
    }
}
