// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;

/**
 * Resource manager for Event Hubs namespace.
 */
public class EventHubNamespaceCrud extends AbstractResourceCrud<EventHubNamespace, String> {

    /**
     * Creates a new instance of {@link EventHubNamespaceCrud}.
     *
     * @param azureResourceManager The Azure resource manager.
     * @param azureResourceMetadata The Azure resource metadata.
     */
    public EventHubNamespaceCrud(AzureResourceManager azureResourceManager,
                                 AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
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
            return this.resourceManager.eventHubNamespaces()
                                       .getByResourceGroup(this.resourceMetadata.getResourceGroup(), namespace);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public EventHubNamespace internalCreate(String namespace) {
        return this.resourceManager.eventHubNamespaces()
                                   .define(namespace)
                                   .withRegion(this.resourceMetadata.getRegion())
                                   .withExistingResourceGroup(this.resourceMetadata.getResourceGroup())
                                   .create();
    }
}
