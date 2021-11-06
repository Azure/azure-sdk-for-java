// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.service.servicebus.properties.ServiceBusConsumerDescriptor;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

import java.time.Duration;

/**
 * A service bus consumer related properties.
 */
public class ConsumerProperties extends CommonProperties implements ServiceBusConsumerDescriptor {

    private Boolean sessionAware;
    private Boolean autoComplete;
    private Integer prefetchCount;
    private String name;
    private SubQueue subQueue = SubQueue.NONE;
    private ServiceBusReceiveMode receiveMode = ServiceBusReceiveMode.PEEK_LOCK;
    private String subscriptionName;
    private ServiceBusEntityType type;
    private Duration maxAutoLockRenewDuration = Duration.ofMinutes(5);

    @Override
    public Boolean getSessionAware() {
        return sessionAware;
    }

    public void setSessionAware(Boolean sessionAware) {
        this.sessionAware = sessionAware;
    }

    @Override
    public Boolean getAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(Boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    @Override
    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SubQueue getSubQueue() {
        return subQueue;
    }

    public void setSubQueue(SubQueue subQueue) {
        this.subQueue = subQueue;
    }

    @Override
    public ServiceBusReceiveMode getReceiveMode() {
        return receiveMode;
    }

    public void setReceiveMode(ServiceBusReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
    }

    @Override
    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    @Override
    public ServiceBusEntityType getType() {
        return type;
    }

    public void setType(ServiceBusEntityType type) {
        this.type = type;
    }

    @Override
    public Duration getMaxAutoLockRenewDuration() {
        return maxAutoLockRenewDuration;
    }

    public void setMaxAutoLockRenewDuration(Duration maxAutoLockRenewDuration) {
        this.maxAutoLockRenewDuration = maxAutoLockRenewDuration;
    }
}
