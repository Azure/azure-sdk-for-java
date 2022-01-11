// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.properties.EventBatchProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import com.azure.spring.service.eventhubs.properties.LoadBalancingProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * An event hub processor related properties.
 */
public class ProcessorProperties extends ConsumerProperties implements EventProcessorClientProperties {

    private final Map<String, EventProcessorClientProperties.StartPosition> initialPartitionEventPosition = new HashMap<>();
    private final LoadBalancingProperties loadBalancing = new LoadBalancingProperties();
    private final EventBatchProperties batch = new EventBatchProperties();
    private Boolean trackLastEnqueuedEventProperties;

    @Override
    public Map<String, StartPosition> getInitialPartitionEventPosition() {
        return initialPartitionEventPosition;
    }

    @Override
    public LoadBalancingProperties getLoadBalancing() {
        return loadBalancing;
    }

    @Override
    public EventBatchProperties getBatch() {
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
