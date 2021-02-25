// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.spring.cloud.context.core.config.AzureProperties;

/**
 * Resource manager for Service Bus namespace.
 */
public class ServiceBusNamespaceManager extends AzureManager<ServiceBusNamespace, String> {

    private final AzureResourceManager azureResourceManager;

    public ServiceBusNamespaceManager(AzureResourceManager azureResourceManager, AzureProperties azureProperties) {
        super(azureProperties);
        this.azureResourceManager = azureResourceManager;
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return ServiceBusNamespace.class.getSimpleName();
    }

    @Override
    public ServiceBusNamespace internalGet(String namespace) {
        try {
            return azureResourceManager.serviceBusNamespaces().getByResourceGroup(resourceGroup, namespace);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServiceBusNamespace internalCreate(String namespace) {
        return azureResourceManager.serviceBusNamespaces()
                                   .define(namespace)
                                   .withRegion(region)
                                   .withExistingResourceGroup(resourceGroup).create();
    }
}
