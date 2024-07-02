// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;

import java.time.Duration;

/**
 * Event processor load balancing properties.
 */
public class LoadBalancingProperties implements EventProcessorClientProperties.LoadBalancing {

    /**
     * Creates an instance of {@link LoadBalancingProperties}.
     */
    public LoadBalancingProperties() {
    }

    /**
     * The time interval between load balancing update cycles.
     */
    private Duration updateInterval;
    /**
     * The load balancing strategy for claiming partition ownership.
     */
    private LoadBalancingStrategy strategy;
    /**
     * The time duration after which the ownership of partition expires.
     */
    private Duration partitionOwnershipExpirationInterval;

    @Override
    public Duration getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Set the update interval.
     * @param updateInterval The update interval.
     */
    public void setUpdateInterval(Duration updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Override
    public LoadBalancingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Set the load balancing strategy.
     * @param strategy The load balancing strategy.
     */
    public void setStrategy(LoadBalancingStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
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
