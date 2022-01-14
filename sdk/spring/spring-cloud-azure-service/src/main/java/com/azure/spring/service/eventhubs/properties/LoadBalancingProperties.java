// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;

import java.time.Duration;

/**
 * Event processor load balancing properties.
 */
public class LoadBalancingProperties implements EventProcessorClientProperties.LoadBalancing {

    /**
     * The time interval between load balancing update cycles.
     */
    private Duration updateInterval;
    /**
     * The load balancing strategy for claiming partition ownership.
     */
    private LoadBalancingStrategy strategy = LoadBalancingStrategy.BALANCED;
    /**
     * The time duration after which the ownership of partition expires.
     */
    private Duration partitionOwnershipExpirationInterval;

    public Duration getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(Duration updateInterval) {
        this.updateInterval = updateInterval;
    }

    public LoadBalancingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(LoadBalancingStrategy strategy) {
        this.strategy = strategy;
    }

    public Duration getPartitionOwnershipExpirationInterval() {
        return partitionOwnershipExpirationInterval;
    }

    /**
     * Set the partition ownership expiration interval.
     * @param partitionOwnershipExpirationInterval the partition ownership expiration interval.
     */
    public void setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
        this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
    }
}
