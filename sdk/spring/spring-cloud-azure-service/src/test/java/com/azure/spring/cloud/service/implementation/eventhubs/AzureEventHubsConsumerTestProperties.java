// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubConsumerProperties;

/**
 * Azure Event Hubs related properties.
 */
abstract class AzureEventHubsConsumerTestProperties extends AzureEventHubsCommonTestProperties
    implements EventHubConsumerProperties {

    protected String consumerGroup;

    protected Integer prefetchCount;

    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    @Override
    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }
}
