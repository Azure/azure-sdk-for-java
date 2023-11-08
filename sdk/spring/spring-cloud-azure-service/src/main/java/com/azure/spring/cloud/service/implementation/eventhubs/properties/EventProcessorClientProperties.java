// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Azure Event Processor client related properties.
 */
public interface EventProcessorClientProperties extends EventHubConsumerProperties {

    /**
     * Get whether to track the last enqueued event properties.
     * @return whether to track the last enqueued event properties.
     */
    Boolean getTrackLastEnqueuedEventProperties();

    /**
     * Get the initial partition event position mapping.
     * @return the partition event position map.
     */
    Map<String, ? extends StartPosition> getInitialPartitionEventPosition();

    /**
     * Get the event batch.
     * @return the event batch.
     */
    EventBatch getBatch();

    /**
     * Get the load balancing configuration.
     * @return the load balancing configuration.
     */
    LoadBalancing getLoadBalancing();

    /**
     * Event processor load balancing properties.
     */
    interface LoadBalancing {

        /**
         * Get the time interval between load balancing update cycles.
         * @return the update interval.
         */
        Duration getUpdateInterval();

        /**
         * Get the load balancing strategy for claiming partition ownership.
         * @return the load balancing strategy.
         */
        LoadBalancingStrategy getStrategy();

        /**
         * Get the time duration after which the ownership of partition expires.
         * @return the expiration interval.
         */
        Duration getPartitionOwnershipExpirationInterval();

    }

    /**
     * Event processor batch properties.
     */
    interface EventBatch {

        /**
         * Get the max time duration to wait to receive an event before processing events.
         * @return the max wait time.
         */
        Duration getMaxWaitTime();

        /**
         * Get the maximum number of events that will be in the batch.
         * @return the max size.
         */
        Integer getMaxSize();

    }

    /**
     * The starting position from which to consume events.
     */
    interface StartPosition {

        /**
         * Whether the event of the specified sequence number is included.
         * @return Whether to include the specified event.
         */
        boolean isInclusive();

        /**
         * The offset of the event within that partition. String keyword, "earliest" and "latest" (case-insensitive),
         * are reserved for specifying the start and end of the partition. Other provided value will be cast to Long.
         * @return The offset of the event within that partition.
         */
        String getOffset();

        /**
         * The sequence number of the event within that partition.
         * @return The sequence number of the event within that partition.
         */
        Long getSequenceNumber();

        /**
         * The event enqueued after the requested enqueuedDateTime becomes the current position.
         * @return The enqueued datetime.
         */
        Instant getEnqueuedDateTime();

    }

}
