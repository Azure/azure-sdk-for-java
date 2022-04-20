// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;

import java.time.Duration;

public class ServiceBusReceiverClientTestProperties extends ServiceBusClientCommonTestProperties implements ServiceBusReceiverClientProperties {

    private String subscriptionName;

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    @Override
    public Boolean getSessionEnabled() {
        return null;
    }

    @Override
    public Boolean getAutoComplete() {
        return null;
    }

    @Override
    public Integer getPrefetchCount() {
        return null;
    }

    @Override
    public SubQueue getSubQueue() {
        return null;
    }

    @Override
    public ServiceBusReceiveMode getReceiveMode() {
        return null;
    }

    @Override
    public String getSubscriptionName() {
        return subscriptionName;
    }

    @Override
    public Duration getMaxAutoLockRenewDuration() {
        return null;
    }
}
