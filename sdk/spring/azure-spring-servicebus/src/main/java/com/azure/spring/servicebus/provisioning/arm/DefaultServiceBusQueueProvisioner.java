// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.provisioning.arm;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusQueueCrud;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.servicebus.provisioning.ServiceBusQueueProvisioner;
import reactor.util.function.Tuples;

/**
 * A default implementation to provision Service Bus Queue.
 */
public class DefaultServiceBusQueueProvisioner implements ServiceBusQueueProvisioner {

    private final ServiceBusQueueCrud serviceBusQueueCrud;

    public DefaultServiceBusQueueProvisioner(AzureResourceManager azureResourceManager,
                                             AzureResourceMetadata azureResourceMetadata) {
        this.serviceBusQueueCrud = new ServiceBusQueueCrud(azureResourceManager, azureResourceMetadata);
    }

    @Override
    public void provisionQueue(String namespace, String queue) {
        this.serviceBusQueueCrud.getOrCreate(Tuples.of(namespace, queue));
    }

}
