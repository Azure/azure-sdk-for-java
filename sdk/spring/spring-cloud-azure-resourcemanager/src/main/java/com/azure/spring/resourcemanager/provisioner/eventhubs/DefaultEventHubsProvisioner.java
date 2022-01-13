// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.resourcemanager.provisioner.eventhubs;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.resourcemanager.implementation.crud.EventHubNamespaceCrud;
import com.azure.spring.resourcemanager.implementation.crud.EventHubsConsumerGroupCrud;
import com.azure.spring.resourcemanager.implementation.crud.EventHubsCrud;
import reactor.util.function.Tuples;

/**
 * Default implementation to provision resources in Azure Event Hubs.
 */
public class DefaultEventHubsProvisioner implements EventHubsProvisioner {

    private final EventHubNamespaceCrud namespaceCrud;
    private final EventHubsCrud eventHubsCrud;
    private final EventHubsConsumerGroupCrud consumerGroupCrud;

    /**
     * Creates a new instance of {@link DefaultEventHubsProvisioner}.
     * @param azureResourceManager the azure resource manager
     * @param azureResourceMetadata the azure resource metadata
     */
    public DefaultEventHubsProvisioner(AzureResourceManager azureResourceManager,
                                       AzureResourceMetadata azureResourceMetadata) {
        this.namespaceCrud = new EventHubNamespaceCrud(azureResourceManager, azureResourceMetadata);
        this.eventHubsCrud = new EventHubsCrud(azureResourceManager, azureResourceMetadata);
        this.consumerGroupCrud = new EventHubsConsumerGroupCrud(azureResourceManager, azureResourceMetadata);
    }

    @Override
    public void provisionNamespace(String namespace) {
        this.namespaceCrud.getOrCreate(namespace);
    }

    @Override
    public void provisionEventHubs(String namespace, String eventHubs) {
        this.eventHubsCrud.getOrCreate(Tuples.of(namespace, eventHubs));
    }

    @Override
    public void provisionConsumerGroup(String namespace, String eventHubs, String consumerGroup) {
        this.consumerGroupCrud.getOrCreate(Tuples.of(namespace, eventHubs, consumerGroup));
    }

}
