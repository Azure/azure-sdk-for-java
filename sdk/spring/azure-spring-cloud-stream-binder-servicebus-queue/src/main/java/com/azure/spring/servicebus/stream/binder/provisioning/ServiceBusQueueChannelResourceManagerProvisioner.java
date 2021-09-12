// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.provisioning;

import com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager.DefaultServiceBusQueueProvisioner;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueChannelResourceManagerProvisioner extends ServiceBusChannelProvisioner {

    private final String namespace;
    private final DefaultServiceBusQueueProvisioner serviceBusQueueProvisioner;

    public ServiceBusQueueChannelResourceManagerProvisioner(@NonNull String namespace,
                                                            @NonNull DefaultServiceBusQueueProvisioner queueProvisioner) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.serviceBusQueueProvisioner = queueProvisioner;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        this.serviceBusQueueProvisioner.provisionQueue(namespace, name);
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        this.serviceBusQueueProvisioner.provisionQueue(namespace, name);
    }
}
