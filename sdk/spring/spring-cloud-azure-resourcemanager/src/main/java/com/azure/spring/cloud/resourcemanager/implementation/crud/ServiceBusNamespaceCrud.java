// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import org.springframework.lang.NonNull;

/**
 * Resource manager for Service Bus namespace.
 */
public class ServiceBusNamespaceCrud extends AbstractResourceCrud<ServiceBusNamespace, String> {

    public ServiceBusNamespaceCrud(@NonNull AzureResourceManager azureResourceManager,
                                   @NonNull AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
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
            return this.resourceManager.serviceBusNamespaces().getByResourceGroup(resourceMetadata.getResourceGroup(),
                                                                                  namespace);
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
        return this.resourceManager.serviceBusNamespaces()
                                   .define(namespace)
                                   .withRegion(this.resourceMetadata.getRegion())
                                   .withExistingResourceGroup(this.resourceMetadata.getResourceGroup())
                                   .create();
    }
}
