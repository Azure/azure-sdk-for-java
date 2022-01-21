// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.checkpoint;

/**
 * Support checkpoint by setting {@link CheckpointConfig}
 */
public interface Checkpointable {

    /**
     * Get a {@link CheckpointConfig} instance.
     * @return a CheckpointConfig instance.
     */
    CheckpointConfig getCheckpointConfig();

    /**
     * Set a {@link CheckpointConfig} instance.
     * @param checkpointConfig a CheckpointConfig instance.
     */
    void setCheckpointConfig(CheckpointConfig checkpointConfig);
}
