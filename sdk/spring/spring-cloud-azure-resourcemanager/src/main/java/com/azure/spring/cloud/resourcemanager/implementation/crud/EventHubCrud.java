// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import reactor.util.function.Tuple2;

/**
 * Resource manager for Event Hubs.
 */
public class EventHubCrud extends AbstractResourceCrud<EventHub, Tuple2<String, String>> {

    public EventHubCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple2<String, String> key) {
        return key.getT2();
    }

    @Override
    String getResourceType() {
        return EventHub.class.getSimpleName();
    }

    @Override
    public EventHub internalGet(Tuple2<String, String> namespaceAndName) {
        try {
            return this.resourceManager.eventHubs()
                                       .getByName(this.resourceMetadata.getResourceGroup(),
                                                  namespaceAndName.getT1(),
                                                  namespaceAndName.getT2());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public EventHub internalCreate(Tuple2<String, String> namespaceAndName) {
        return this.resourceManager.eventHubs()
                                   .define(namespaceAndName.getT2())
                                   .withExistingNamespace(new EventHubNamespaceCrud(this.resourceManager,
                                                                                    this.resourceMetadata)
                                                              .getOrCreate(namespaceAndName.getT1()))
                                   .create();
    }
}
