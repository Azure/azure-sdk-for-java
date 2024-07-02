// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.spring.cloud.service.eventhubs.properties.EventBatchProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import com.azure.spring.cloud.service.eventhubs.properties.LoadBalancingProperties;
import com.azure.spring.cloud.service.eventhubs.properties.StartPositionProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * An event hub processor related properties.
 */
public class ProcessorProperties extends ConsumerProperties implements EventProcessorClientProperties {

    /**
     * Create an instance of {@link ProcessorProperties}.
     */
    public ProcessorProperties() {
    }

    private final Map<String, StartPositionProperties> initialPartitionEventPosition = new HashMap<>();
    private final LoadBalancingProperties loadBalancing = new LoadBalancingProperties();
    private final EventBatchProperties batch = new EventBatchProperties();
    private Boolean trackLastEnqueuedEventProperties;

    @Override
    public Map<String, StartPositionProperties> getInitialPartitionEventPosition() {
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
