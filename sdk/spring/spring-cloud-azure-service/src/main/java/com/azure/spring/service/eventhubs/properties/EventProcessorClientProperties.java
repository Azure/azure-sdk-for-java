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

    Boolean getTrackLastEnqueuedEventProperties();

    Map<String, StartPosition> getInitialPartitionEventPosition();

    EventBatch getBatch();

    LoadBalancing getLoadBalancing();

    /**
     * Event processor load balancing properties.
     */
    class LoadBalancing {

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

    /**
     * Event processor batch properties.
     */
    class EventBatch {

        /**
         * The max time duration to wait to receive an event before processing events.
         */
        private Duration maxWaitTime;
        /**
         * The maximum number of events that will be in the batch.
         */
        private Integer maxSize;

        public Duration getMaxWaitTime() {
            return maxWaitTime;
        }

        public void setMaxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }
    }

    /**
     * The starting position from which to consume events.
     */
    enum StartPosition {
        EARLIEST,
        LATEST;

        public EventPosition toEventPosition() {
            if (EARLIEST.equals(this)) {
                return EventPosition.earliest();
            } else {
                return EventPosition.latest();
            }
        }
    }

}
