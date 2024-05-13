// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;

import java.time.Duration;
import java.util.function.Function;

/**
 * Options set when creating EventProcessorClient.
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

    private Function<String, EventPosition> initialEventPositionProvider;

    /**
     * Gets the consumer group used to receive events.
     *
     * @return The consumer group used to receive events.
     */
    String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Sets the consumer group used to receive events.
     *
     * @param consumerGroup The consumer group.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Gets the function to map a partition id to its {@link EventPosition}.
     *
     * @return Function to map a partition id to its {@link EventPosition}.
     */
    Function<String, EventPosition> getInitialEventPositionProvider() {
        return initialEventPositionProvider;
    }

    /**
     * Sets the function to map a partition id to its {@link EventPosition}.
     *
     * @param initialEventPositionProvider The function to map a partition id to its {@link EventPosition}.
     */
    EventProcessorClientOptions setInitialEventPositionProvider(
        Function<String, EventPosition> initialEventPositionProvider) {

        this.initialEventPositionProvider = initialEventPositionProvider;
        return this;
    }

    /**
     * Gets the boolean value indicating if this processor is configured to receive in batches or single events.
     *
     * @return The boolean value indicating if this processor is configured to receive in batches or single events.
     */
    boolean isBatchReceiveMode() {
        return batchReceiveMode;
    }

    /**
     * Sets the boolean value indicating if this processor is configured to receive in batches or single events.
     *
     * @param batchReceiveMode the boolean value indicating if this processor is configured to receive in batches or
     *     single events.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setBatchReceiveMode(boolean batchReceiveMode) {
        this.batchReceiveMode = batchReceiveMode;
        return this;
    }

    /**
     * If set to {@code true}, all events received by this EventProcessorClient will also include the last enqueued
     * event properties for its respective partitions.
     *
     * @return If set to {@code true}, all events received by this EventProcessorClient will also include the last
     *     enqueued event properties for its respective partitions.
     */
    boolean isTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties != null && trackLastEnqueuedEventProperties;
    }

    /**
     * Sets whether to include the last enqueued event properties for its respective partitions.
     *
     * @param trackLastEnqueuedEventProperties True to include last enqueued event properties.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setTrackLastEnqueuedEventProperties(Boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        return this;
    }

    /**
     * Gets the load balancing strategy to use.
     *
     * @return The load balancing strategy to use.
     */
    LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    /**
     * Sets the load balancing strategy to use.
     *
     * @param loadBalancingStrategy the load balancing strategy to use.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
        return this;
    }

    /**
     * Gets the time duration between load balancing update cycles.
     *
     * @return The time duration between load balancing update cycles.
     */
    Duration getLoadBalancerUpdateInterval() {
        return loadBalancerUpdateInterval;
    }

    /**
     * Sets the interval between load balancing cycles.
     *
     * @param loadBalancerUpdateInterval The interval between load balancing cycles.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setLoadBalancerUpdateInterval(Duration loadBalancerUpdateInterval) {
        this.loadBalancerUpdateInterval = loadBalancerUpdateInterval;
        return this;
    }

    /**
     * Gets the maximum batch size to receive per users' process handler invocation.
     *
     * @return The maximum batch size to receive per users' process handler invocation.
     */
    int getMaxBatchSize() {
        return maxBatchSize;
    }

    /**
     * Sets the maximum batch size to receive per users' process handler invocation.
     *
     * @param maxBatchSize the maximum batch size to receive per users' process handler invocation.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
        return this;
    }

    /**
     * Gets the maximum time to wait to receive a batch or a single event.
     *
     * @return The maximum time to wait to receive a batch or a single event.
     */
    Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * Sets the maximum time to wait to receive a batch or a single event.
     *
     * @param maxWaitTime the maximum time to wait to receive a batch or a single event.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
        return this;
    }

    /**
     * Gets the time duration after which the ownership of partition expires.
     *
     * @return The time duration after which the ownership of partition expires.
     */
    Duration getPartitionOwnershipExpirationInterval() {
        return partitionOwnershipExpirationInterval;
    }

    /**
     * Sets the time duration after which the ownership of partition expires.
     *
     * @param partitionOwnershipExpirationInterval the time duration after which the ownership of partition
     *     expires.
     *
     * @return The updated {@link EventProcessorClientOptions} object.
     */
    EventProcessorClientOptions setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
        this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
        return this;
    }
}
