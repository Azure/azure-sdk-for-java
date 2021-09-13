// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.provisioning;

import com.azure.spring.integration.servicebus.factory.ServiceBusTopicProvisioner;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicChannelResourceManagerProvisioner extends ServiceBusChannelProvisioner {

    private final ServiceBusTopicProvisioner serviceBusTopicProvisioner;
    private final String namespace;

    public ServiceBusTopicChannelResourceManagerProvisioner(@NonNull String namespace,
                                                            @NonNull ServiceBusTopicProvisioner topicProvisioner) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.serviceBusTopicProvisioner = topicProvisioner;
        this.namespace = namespace;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String group) {
        this.serviceBusTopicProvisioner.provisionSubscription(this.namespace, name, group);
    }

    @Override
    protected void validateOrCreateForProducer(String name) {
        this.serviceBusTopicProvisioner.provisionTopic(this.namespace, name);
    }
}
