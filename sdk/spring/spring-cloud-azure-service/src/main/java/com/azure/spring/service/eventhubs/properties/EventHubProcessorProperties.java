// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.service.storage.blob.StorageBlobProperties;

import java.time.Duration;
import java.util.Map;

/**
 * Azure Event Hub related properties.
 */
public interface EventHubProcessorProperties extends EventHubConsumerProperties {

    Boolean getTrackLastEnqueuedEventProperties();

    Map<String, EventPosition> getInitialPartitionEventPosition();

    Duration getPartitionOwnershipExpirationInterval();

    Batch getBatch();

    LoadBalancing getLoadBalancing();

    StorageBlobProperties getCheckpointStore();

    /**
     * Event processor load balancing properties.
     */
    interface LoadBalancing {
        Duration getUpdateInterval();

        LoadBalancingStrategy getStrategy();

    }

    /**
     * Event processor batch properties.
     */
    interface Batch {

        Duration getMaxWaitTime();

        Integer getMaxSize();

    }

}
