// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.properties.EventHubConsumerDescriptor;

/**
 * An event hub consumer related properties.
 */
public class ConsumerProperties extends CommonProperties implements EventHubConsumerDescriptor {

    protected String consumerGroup;
    protected Integer prefetchCount;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

}
