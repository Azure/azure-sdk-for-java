// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;

/**
 * Resource manager for Service Bus namespace.
 */
public class ServiceBusNamespaceManager extends AzureManager<ServiceBusNamespace, String> {

    private final Azure azure;

    public ServiceBusNamespaceManager(Azure azure, AzureProperties azureProperties) {
        super(azureProperties);
        this.azure = azure;
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
            return azure.serviceBusNamespaces().getByResourceGroup(azureProperties.getResourceGroup(), namespace);
        } catch (NullPointerException e) {
            // azure management api has no way to determine whether an eventhub namespace
            // exists
            // Workaround for this is by catching NPE
            return null;
        }
    }

    @Override
    public ServiceBusNamespace internalCreate(String namespace) {
        return azure.serviceBusNamespaces().define(namespace).withRegion(azureProperties.getRegion())
                    .withExistingResourceGroup(azureProperties.getResourceGroup()).create();
    }
}
