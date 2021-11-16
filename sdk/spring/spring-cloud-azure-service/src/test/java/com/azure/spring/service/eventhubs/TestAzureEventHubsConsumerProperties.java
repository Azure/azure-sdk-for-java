// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs;

/**
 * Azure Event Hubs related properties.
 */
public abstract class TestAzureEventHubConsumerProperties extends TestAzureEventHubCommonProperties {

    protected String consumerGroup;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}
