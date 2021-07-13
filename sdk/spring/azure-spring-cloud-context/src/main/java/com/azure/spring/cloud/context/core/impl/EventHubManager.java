// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Event Hubs.
 */
public class EventHubManager extends AzureManager<EventHub, Tuple<EventHubNamespace, String>> {

    private final AzureResourceManager azureResourceManager;

    public EventHubManager(AzureResourceManager azureResourceManager, AzureContextProperties azureContextProperties) {
        super(azureContextProperties);
        this.azureResourceManager = azureResourceManager;
    }

    @Override
    String getResourceName(Tuple<EventHubNamespace, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return EventHub.class.getSimpleName();
    }

    @Override
    public EventHub internalGet(Tuple<EventHubNamespace, String> namespaceAndName) {
        try {
            return azureResourceManager.eventHubs()
                                       .getByName(resourceGroup, namespaceAndName.getFirst().name(),
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
    public EventHub internalCreate(Tuple<EventHubNamespace, String> namespaceAndName) {
        return azureResourceManager.eventHubs()
                                   .define(namespaceAndName.getSecond())
                                   .withExistingNamespace(namespaceAndName.getFirst())
                                   .create();
    }
}
