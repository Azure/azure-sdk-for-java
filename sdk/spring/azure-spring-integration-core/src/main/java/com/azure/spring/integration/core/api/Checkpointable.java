// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

/**
 * Support checkpoint by setting {@link CheckpointConfig}
 *
 * @author Warren Zhu
 */
public interface Checkpointable {

    /**
     *
     * @return The CheckpointConfig.
     */
    CheckpointConfig getCheckpointConfig();

    /**
     *
     * @param checkpointConfig The CheckpointConfig.
     */
    void setCheckpointConfig(CheckpointConfig checkpointConfig);
}
