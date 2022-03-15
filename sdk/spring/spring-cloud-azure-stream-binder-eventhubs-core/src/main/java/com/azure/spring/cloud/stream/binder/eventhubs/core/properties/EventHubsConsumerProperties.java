// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;

/**
 *
 */
public class EventHubsConsumerProperties extends ProcessorProperties {

    // TODO (xiada): should we support this for the entire binding, but we should figure out how to iterate partitions
    // first
    //    /**
    //     * Whether the consumer receives messages from the beginning or end of event hub.
    //     * If {@link StartPosition#EARLIEST}, from beginning. If {@link StartPosition#LATEST}, from end.
    //     * <p>
    //     * Default: {@link StartPosition#LATEST}
    //     */
    //    private EventProcessingProperties.StartPosition startPosition = EventProcessingProperties.StartPosition.LATEST;

    private final CheckpointConfig checkpoint = new CheckpointConfig();

    /**
     * Get the {@link CheckpointConfig}.
     *
     * @return CheckpointConfig the {@link CheckpointConfig}
     * @see CheckpointConfig
     */
    public CheckpointConfig getCheckpoint() {
        return checkpoint;
    }

}
