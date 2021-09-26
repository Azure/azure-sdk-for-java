// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubConsumerGroupCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubNamespaceCrud;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.eventhubs.core.EventHubProvisioner;
import reactor.util.function.Tuples;

/**
 * Default implementation to provision an Event Hub.
 */
public class DefaultEventHubProvisioner implements EventHubProvisioner {

    private final EventHubNamespaceCrud namespaceCrud;
    private final EventHubCrud eventHubCrud;
    private final EventHubConsumerGroupCrud consumerGroupCrud;

    public DefaultEventHubProvisioner(AzureResourceManager azureResourceManager,
                                      AzureResourceMetadata azureResourceMetadata) {
        this.namespaceCrud = new EventHubNamespaceCrud(azureResourceManager, azureResourceMetadata);
        this.eventHubCrud = new EventHubCrud(azureResourceManager, azureResourceMetadata);
        this.consumerGroupCrud = new EventHubConsumerGroupCrud(azureResourceManager, azureResourceMetadata);
    }

    @Override
    public void provisionNamespace(String namespace) {
        this.namespaceCrud.getOrCreate(namespace);
    }

    @Override
    public void provisionEventHub(String namespace, String eventHub) {
        this.eventHubCrud.getOrCreate(Tuples.of(namespace, eventHub));
    }

    @Override
    public void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup) {
        this.consumerGroupCrud.getOrCreate(Tuples.of(namespace, eventHub, consumerGroup));
    }

}
