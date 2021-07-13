// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;

/**
 * Resource manager for Event Hubs namespace.
 */
public class EventHubNamespaceManager extends AzureManager<EventHubNamespace, String> {

    private final AzureResourceManager azureResourceManager;

    public EventHubNamespaceManager(AzureResourceManager azureResourceManager, AzureContextProperties azureContextProperties) {
        super(azureContextProperties);
        this.azureResourceManager = azureResourceManager;
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return EventHubNamespace.class.getSimpleName();
    }

    @Override
    public EventHubNamespace internalGet(String namespace) {
        try {
            return azureResourceManager.eventHubNamespaces().getByResourceGroup(resourceGroup, namespace);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public EventHubNamespace internalCreate(String namespace) {
        return azureResourceManager.eventHubNamespaces()
                                   .define(namespace)
                                   .withRegion(region)
                                   .withExistingResourceGroup(resourceGroup)
                                   .create();
    }
}
