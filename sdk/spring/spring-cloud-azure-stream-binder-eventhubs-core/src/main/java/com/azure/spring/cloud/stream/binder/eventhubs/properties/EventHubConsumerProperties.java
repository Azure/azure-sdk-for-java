// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.properties;

import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;

import java.time.Duration;

/**
 *
 */
public class EventHubConsumerProperties {
//    /**
//     * Whether the consumer receives messages from the beginning or end of event hub.
//     * If {@link StartPosition#EARLIEST}, from beginning. If {@link StartPosition#LATEST}, from end.
//     * <p>
//     * Default: {@link StartPosition#LATEST}
//     */
//    private EventProcessingProperties.StartPosition startPosition = EventProcessingProperties.StartPosition.LATEST;
    private final CheckpointConfig checkpoint = new CheckpointConfig();
    private final ProcessorProperties processor = new ProcessorProperties();

    public CheckpointConfig getCheckpoint() {
        return checkpoint;
    }

    public ProcessorProperties getProcessor() {
        return processor;
    }

}
