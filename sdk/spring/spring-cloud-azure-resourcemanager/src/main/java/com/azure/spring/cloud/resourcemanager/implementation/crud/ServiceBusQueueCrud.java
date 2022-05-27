// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;

/**
 * Resource manager for Service Bus queue.
 */
public class ServiceBusQueueCrud extends AbstractResourceCrud<Queue, Tuple2<String, String>> {


    public ServiceBusQueueCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple2<String, String> key) {
        return key.getT2();
    }

    @Override
    String getResourceType() {
        return Queue.class.getSimpleName();
    }

    @Override
    public Queue internalGet(Tuple2<String, String> namespaceAndName) {
        try {
            ServiceBusNamespace serviceBusNamespace = new ServiceBusNamespaceCrud(this.resourceManager,
                this.resourceMetadata)
                .get(namespaceAndName.getT1());
            Assert.notNull(serviceBusNamespace, "The Service Bus namespace should exist first.");
            return serviceBusNamespace
                .queues()
                .getByName(namespaceAndName.getT2());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Queue internalCreate(Tuple2<String, String> namespaceAndName) {
        return new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(namespaceAndName.getT1())
            .queues()
            .define(namespaceAndName.getT2())
            .create();
    }
}
