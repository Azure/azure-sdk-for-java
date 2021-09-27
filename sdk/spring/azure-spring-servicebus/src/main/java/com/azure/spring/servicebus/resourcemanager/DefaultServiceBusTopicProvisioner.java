// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicSubscriptionCrud;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import reactor.util.function.Tuples;

/**
 * A default implementation to provision Service Bus Topic.
 */
public class DefaultServiceBusTopicProvisioner implements ServiceBusTopicProvisioner {

    private final ServiceBusTopicCrud topicCrud;
    private final ServiceBusTopicSubscriptionCrud subscriptionCrud;

    public DefaultServiceBusTopicProvisioner(AzureResourceManager azureResourceManager,
                                             AzureResourceMetadata azureResourceMetadata) {
        this.topicCrud = new ServiceBusTopicCrud(azureResourceManager, azureResourceMetadata);
        this.subscriptionCrud = new ServiceBusTopicSubscriptionCrud(azureResourceManager, azureResourceMetadata);
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
