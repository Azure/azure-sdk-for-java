// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.properties.EventHubsProcessorDescriptor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * An event hub processor related properties.
 */
public class ProcessorProperties extends ConsumerProperties implements EventHubsProcessorDescriptor {

    private final Map<String, EventHubsProcessorDescriptor.StartPosition> initialPartitionEventPosition = new HashMap<>();
    private final LoadBalancing loadBalancing = new LoadBalancing();
    private final Batch batch = new Batch();
    private Boolean trackLastEnqueuedEventProperties;
    private Duration partitionOwnershipExpirationInterval;

    @Override
    public Map<String, StartPosition> getInitialPartitionEventPosition() {
        return initialPartitionEventPosition;
    }

    @Override
    public LoadBalancing getLoadBalancing() {
        return loadBalancing;
    }

    @Override
    public Batch getBatch() {
        return batch;
    }

    @Override
    public Boolean getTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties;
    }

    /**
     * Set whether to track the last enqueued event properties.
     * @param trackLastEnqueuedEventProperties whether to track the last enqueued event properties.
     */
    public void setTrackLastEnqueuedEventProperties(Boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
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
