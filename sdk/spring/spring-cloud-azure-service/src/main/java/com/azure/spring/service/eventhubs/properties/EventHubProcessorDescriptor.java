// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.time.Duration;
import java.util.Map;

/**
 * Azure Event Hub Processor related properties.
 */
public interface EventHubProcessorDescriptor extends EventHubConsumerDescriptor {

    Boolean getTrackLastEnqueuedEventProperties();

    Map<String, StartPosition> getInitialPartitionEventPosition();

    Duration getPartitionOwnershipExpirationInterval();

    Batch getBatch();

    LoadBalancing getLoadBalancing();

    /**
     * Event processor load balancing properties.
     */
    class LoadBalancing {
        private Duration updateInterval;
        private LoadBalancingStrategy strategy = LoadBalancingStrategy.BALANCED;

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
    }

    /**
     * Event processor batch properties.
     */
    class Batch  {
        private Duration maxWaitTime;
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
