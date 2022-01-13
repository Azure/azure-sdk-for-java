// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.time.Duration;
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
    Map<String, StartPosition> getInitialPartitionEventPosition();

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
    enum StartPosition {
        EARLIEST,
        LATEST;

        /**
         * Convert the current StartPosition to EventPosition object.
         * @return the EventPosition object.
         */
        public EventPosition toEventPosition() {
            if (EARLIEST.equals(this)) {
                return EventPosition.earliest();
            } else {
                return EventPosition.latest();
            }
        }
    }

}
