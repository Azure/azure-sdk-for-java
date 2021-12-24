// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.properties.EventProcessorClientProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * An event hub processor related properties.
 */
public class ProcessorProperties extends ConsumerProperties implements EventProcessorClientProperties {

    private final Map<String, EventProcessorClientProperties.StartPosition> initialPartitionEventPosition = new HashMap<>();
    private final LoadBalancing loadBalancing = new LoadBalancing();
    private final EventBatch batch = new EventBatch();
    private Boolean trackLastEnqueuedEventProperties;

    @Override
    public Map<String, StartPosition> getInitialPartitionEventPosition() {
        return initialPartitionEventPosition;
    }

    @Override
    public LoadBalancing getLoadBalancing() {
        return loadBalancing;
    }

    @Override
    public EventBatch getBatch() {
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

}
