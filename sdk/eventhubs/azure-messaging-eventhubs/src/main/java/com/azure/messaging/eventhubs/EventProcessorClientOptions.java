// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;

import java.time.Duration;
import java.util.Map;

/**
 * Package-private class specifying the options set when creating EventProcessorClient.
 */
class EventProcessorClientOptions {
    private boolean batchReceiveMode;
    private String consumerGroup;
    private LoadBalancingStrategy loadBalancingStrategy;
    private Duration loadBalancerUpdateInterval;
    private int maxBatchSize;
    private Duration maxWaitTime;
    private Duration partitionOwnershipExpirationInterval;
    private Boolean trackLastEnqueuedEventProperties;
    private Map<String, EventPosition> initialPartitionEventPosition;

    EventProcessorClientOptions() {
    }

    String getConsumerGroup() {
        return consumerGroup;
    }

    void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    Map<String, EventPosition> getInitialPartitionEventPosition() {
        return initialPartitionEventPosition;
    }

    void setInitialPartitionEventPosition(Map<String, EventPosition> initialPartitionEventPosition) {
        this.initialPartitionEventPosition = initialPartitionEventPosition;
    }

    boolean isBatchReceiveMode() {
        return batchReceiveMode;
    }

    void setBatchReceiveMode(boolean batchReceiveMode) {
        this.batchReceiveMode = batchReceiveMode;
    }

    boolean isTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties != null && trackLastEnqueuedEventProperties;
    }

    void setTrackLastEnqueuedEventProperties(Boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
    }

    LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    Duration getLoadBalancerUpdateInterval() {
        return loadBalancerUpdateInterval;
    }

    void setLoadBalancerUpdateInterval(Duration loadBalancerUpdateInterval) {
        this.loadBalancerUpdateInterval = loadBalancerUpdateInterval;
    }

    int getMaxBatchSize() {
        return maxBatchSize;
    }

    void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    void setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    Duration getPartitionOwnershipExpirationInterval() {
        return partitionOwnershipExpirationInterval;
    }

    void setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
        this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
    }
}
