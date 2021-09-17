// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import javax.annotation.Nullable;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {

    @Nullable
    protected ServiceBusQueueProvisioner queueProvisioner;

    @Nullable
    protected ServiceBusTopicProvisioner topicProvisioner;

    protected final ServiceBusClientBuilder serviceBusClientBuilder;

    AbstractServiceBusSenderFactory(ServiceBusClientBuilder serviceBusClientBuilder) {
        this.serviceBusClientBuilder = serviceBusClientBuilder;
    }

    public void setQueueProvisioner(@Nullable ServiceBusQueueProvisioner queueProvisioner) {
        this.queueProvisioner = queueProvisioner;
    }

    public void setTopicProvisioner(@Nullable ServiceBusTopicProvisioner topicProvisioner) {
        this.topicProvisioner = topicProvisioner;
    }

}
