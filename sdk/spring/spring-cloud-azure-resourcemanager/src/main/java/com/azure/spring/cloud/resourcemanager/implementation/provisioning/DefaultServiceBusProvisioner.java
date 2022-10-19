// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.provisioning;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusQueueCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicSubscriptionCrud;
import com.azure.spring.cloud.resourcemanager.provisioning.ServiceBusProvisioner;
import reactor.util.function.Tuples;

/**
 * A default implementation to provision Service Bus.
 */
public class DefaultServiceBusProvisioner implements ServiceBusProvisioner {

    private final ServiceBusQueueCrud queueCrud;
    private final ServiceBusTopicCrud topicCrud;
    private final ServiceBusTopicSubscriptionCrud subscriptionCrud;

    /**
     * Creates a new instance of {@link DefaultServiceBusProvisioner}.
     * @param azureResourceManager the azure resource manager
     * @param azureResourceMetadata the azure resource metadata
     */
    public DefaultServiceBusProvisioner(AzureResourceManager azureResourceManager,
                                        AzureResourceMetadata azureResourceMetadata) {
        this.queueCrud = new ServiceBusQueueCrud(azureResourceManager, azureResourceMetadata);
        this.topicCrud = new ServiceBusTopicCrud(azureResourceManager, azureResourceMetadata);
        this.subscriptionCrud = new ServiceBusTopicSubscriptionCrud(azureResourceManager, azureResourceMetadata);
    }

    @Override
    public void provisionQueue(String namespace, String queue) {
        this.queueCrud.getOrCreate(Tuples.of(namespace, queue));
    }

    @Override
    public void provisionTopic(String namespace, String topic) {
        this.topicCrud.getOrCreate(Tuples.of(namespace, topic));
    }

    @Override
    public void provisionSubscription(String namespace, String topic, String subscription) {
        this.subscriptionCrud.getOrCreate(Tuples.of(namespace, topic, subscription));
    }
}
