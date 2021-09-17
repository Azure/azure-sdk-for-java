// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicSubscriptionCrud;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.util.Triple;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.servicebus.core.ServiceBusTopicProvisioner;

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
        this.topicCrud.getOrCreate(Tuple.of(namespace, topic));
    }

    @Override
    public void provisionSubscription(String namespace, String topic, String subscription) {
        this.subscriptionCrud.getOrCreate(Triple.of(namespace, topic, subscription));
    }
}
