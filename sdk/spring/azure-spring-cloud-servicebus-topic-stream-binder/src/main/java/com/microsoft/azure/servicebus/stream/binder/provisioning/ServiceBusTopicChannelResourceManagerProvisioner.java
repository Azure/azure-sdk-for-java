// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.provisioning;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicChannelResourceManagerProvisioner extends ServiceBusChannelProvisioner {

    private final ResourceManagerProvider resourceManagerProvider;
    private final String namespace;

    public ServiceBusTopicChannelResourceManagerProvisioner(@NonNull ResourceManagerProvider resourceManagerProvider,
            @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.resourceManagerProvider = resourceManagerProvider;
        this.namespace = namespace;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        ServiceBusNamespace namespace =
                this.resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(this.namespace);
        Topic topic = this.resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of(namespace, name));
        if (topic == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }

        this.resourceManagerProvider.getServiceBusTopicSubscriptionManager().getOrCreate(Tuple.of(topic, group));
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        ServiceBusNamespace namespace =
                this.resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(this.namespace);
        this.resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of(namespace, name));
    }
}
