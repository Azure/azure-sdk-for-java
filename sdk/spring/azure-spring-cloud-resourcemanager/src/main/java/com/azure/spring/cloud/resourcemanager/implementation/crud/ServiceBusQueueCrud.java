// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Service Bus queue.
 */
public class ServiceBusQueueCrud extends AbstractResourceCrud<Queue, Tuple<String, String>> {


    public ServiceBusQueueCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple<String, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return Queue.class.getSimpleName();
    }

    @Override
    public Queue internalGet(Tuple<String, String> namespaceAndName) {
        try {
            return new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
                .get(namespaceAndName.getFirst())
                .queues()
                .getByName(namespaceAndName.getSecond());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Queue internalCreate(Tuple<String, String> namespaceAndName) {
        return new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(namespaceAndName.getFirst())
            .queues()
            .define(namespaceAndName.getSecond())
            .create();
    }
}
