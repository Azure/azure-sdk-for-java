// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

/**
 * Azure Event Hubs related properties.
 */
abstract class AzureEventHubsConsumerTestProperties extends AzureEventHubsCommonTestProperties {

    protected String consumerGroup;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}
