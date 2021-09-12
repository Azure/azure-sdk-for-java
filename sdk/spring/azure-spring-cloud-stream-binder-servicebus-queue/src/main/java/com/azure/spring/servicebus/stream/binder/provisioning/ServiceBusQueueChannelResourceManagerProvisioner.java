// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.provisioning;

import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.spring.cloud.resourcemanager.core.impl.ServiceBusNamespaceCrud;
import com.azure.spring.cloud.resourcemanager.core.impl.ServiceBusQueueManager;
import com.azure.spring.core.util.Tuple;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueChannelResourceManagerProvisioner extends ServiceBusChannelProvisioner {

    private final String namespace;
    private final ServiceBusNamespaceManager serviceBusNamespaceManager;
    private final ServiceBusQueueManager serviceBusQueueManager;

    public ServiceBusQueueChannelResourceManagerProvisioner(
            @NonNull ServiceBusNamespaceManager serviceBusNamespaceManager,
            @NonNull ServiceBusQueueManager serviceBusQueueManager, @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.serviceBusNamespaceManager = serviceBusNamespaceManager;
        this.serviceBusQueueManager = serviceBusQueueManager;
        this.namespace = namespace;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        ServiceBusNamespace namespace = serviceBusNamespaceManager.getOrCreate(this.namespace);
        Queue queue = serviceBusQueueManager.getOrCreate(Tuple.of(namespace, name));
        if (queue == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        ServiceBusNamespace namespace = serviceBusNamespaceManager.getOrCreate(this.namespace);
        serviceBusQueueManager.getOrCreate(Tuple.of(namespace, name));
    }
}
