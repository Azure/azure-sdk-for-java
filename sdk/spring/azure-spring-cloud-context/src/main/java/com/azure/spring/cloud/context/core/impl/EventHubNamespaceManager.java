// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.spring.cloud.context.core.config.AzureProperties;

/**
 * Resource manager for Event Hubs namespace.
 */
public class EventHubNamespaceManager extends AzureManager<EventHubNamespace, String> {

    private final AzureResourceManager azureResourceManager;

    public EventHubNamespaceManager(AzureResourceManager azureResourceManager, AzureProperties azureProperties) {
        super(azureProperties);
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
        } catch (NullPointerException e) {
            // azure management api has no way to determine whether an eventhub namespace exists
            // Workaround for this is by catching NPE
            return null;
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
