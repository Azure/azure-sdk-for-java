// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus;

import com.azure.spring.service.servicebus.properties.ServiceBusSenderClientProperties;

public class TestServiceBusSenderClientProperties extends TestServiceBusClientCommonProperties
    implements ServiceBusSenderClientProperties {

    private String subscriptionName;

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }
}
