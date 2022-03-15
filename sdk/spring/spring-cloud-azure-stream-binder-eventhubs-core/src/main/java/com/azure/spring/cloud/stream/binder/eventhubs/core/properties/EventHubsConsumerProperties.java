// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;

/**
 *
 */
public class EventHubsConsumerProperties extends ProcessorProperties {

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
