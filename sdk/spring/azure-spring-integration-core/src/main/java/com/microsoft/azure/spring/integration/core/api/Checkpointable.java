// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

/**
 * Support checkpoint by setting {@link CheckpointConfig}
 *
 * @author Warren Zhu
 */
public interface Checkpointable {

    CheckpointConfig getCheckpointConfig();

    void setCheckpointConfig(CheckpointConfig checkpointConfig);
}
