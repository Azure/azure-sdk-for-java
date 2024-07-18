// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubConsumerProperties;

/**
 * An event hub consumer related properties.
 */
public class ConsumerProperties extends CommonProperties implements EventHubConsumerProperties {

    /**
     * Create an instance of {@link ConsumerProperties}
     */
    public ConsumerProperties() {
    }

    private String consumerGroup;
    private Integer prefetchCount;

    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Set the custom endpoint address.
     * @param consumerGroup the custom endpoint address.
     */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    @Override
    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Set the prefetch count.
     * @param prefetchCount the prefetch count.
     */
    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

}
