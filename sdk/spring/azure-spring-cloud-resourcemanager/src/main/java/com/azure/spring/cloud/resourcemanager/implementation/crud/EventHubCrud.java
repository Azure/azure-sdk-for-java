// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Event Hubs.
 */
public class EventHubCrud extends AbstractResourceCrud<EventHub, Tuple<String, String>> {

    public EventHubCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple<String, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return EventHub.class.getSimpleName();
    }

    @Override
    public EventHub internalGet(Tuple<String, String> namespaceAndName) {
        try {
            return this.resourceManager.eventHubs()
                                       .getByName(this.resourceMetadata.getResourceGroup(),
                                                  namespaceAndName.getFirst(),
                                                  namespaceAndName.getSecond());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public EventHub internalCreate(Tuple<String, String> namespaceAndName) {
        return this.resourceManager.eventHubs()
                                   .define(namespaceAndName.getSecond())
                                   .withExistingNamespace(new EventHubNamespaceCrud(this.resourceManager,
                                                                                    this.resourceMetadata)
                                                              .getOrCreate(namespaceAndName.getFirst()))
                                   .create();
    }
}
