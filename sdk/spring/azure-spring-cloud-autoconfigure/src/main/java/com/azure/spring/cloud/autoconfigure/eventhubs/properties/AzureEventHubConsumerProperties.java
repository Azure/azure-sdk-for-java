// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.properties;

/**
 * Azure Event Hub related properties.
 */
public abstract class AzureEventHubConsumerProperties extends AzureEventHubCommonProperties {

    protected String consumerGroup;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}
