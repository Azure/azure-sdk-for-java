// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.provisioning;

import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.core.util.Tuple;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicChannelResourceManagerProvisioner extends ServiceBusChannelProvisioner {

    private final ServiceBusNamespaceManager serviceBusNamespaceManager;
    private final ServiceBusTopicManager serviceBusTopicManager;
    private final ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager;
    private final String namespace;

    public ServiceBusTopicChannelResourceManagerProvisioner(
            @NonNull ServiceBusNamespaceManager serviceBusNamespaceManager,
            @NonNull ServiceBusTopicManager serviceBusTopicManager,
            @NonNull ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager, @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.serviceBusNamespaceManager = serviceBusNamespaceManager;
        this.serviceBusTopicManager = serviceBusTopicManager;
        this.serviceBusTopicSubscriptionManager = serviceBusTopicSubscriptionManager;
        this.namespace = namespace;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        ServiceBusNamespace namespace = serviceBusNamespaceManager.getOrCreate(this.namespace);
        Topic topic = serviceBusTopicManager.getOrCreate(Tuple.of(namespace, name));
        if (topic == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }

        this.serviceBusTopicSubscriptionManager.getOrCreate(Tuple.of(topic, group));
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        ServiceBusNamespace namespace = serviceBusNamespaceManager.getOrCreate(this.namespace);
        serviceBusTopicManager.getOrCreate(Tuple.of(namespace, name));
    }
}
