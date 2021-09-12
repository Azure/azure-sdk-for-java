package com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusQueueCrud;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.integration.servicebus.factory.ServiceBusProvisioner;

public class DefaultServiceBusQueueProvisioner implements ServiceBusProvisioner {

    private final ServiceBusQueueCrud serviceBusQueueCrud;

    public DefaultServiceBusQueueProvisioner(AzureResourceManager azureResourceManager,
                                             AzureResourceMetadata azureResourceMetadata) {
        this.serviceBusQueueCrud = new ServiceBusQueueCrud(azureResourceManager, azureResourceMetadata);
    }

    @Override
    public void provisionQueue(String namespace, String queue) {
        this.serviceBusQueueCrud.getOrCreate(Tuple.of(namespace, queue));
    }

    @Override
    public void provisionTopic(String namespace, String topic) {
        throw new UnsupportedOperationException("Can't provision topic in a queue client");
    }

    @Override
    public void provisionSubscription(String namespace, String topic, String subscription) {
        throw new UnsupportedOperationException("Can't provision subscription in a queue client");
    }
}