/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

/**
 * Support checkpoint by setting {@link CheckpointConfig}
 *
 * @author Warren Zhu
 */
public interface Checkpointable {

    void setCheckpointConfig(CheckpointConfig checkpointConfig);

    CheckpointConfig getCheckpointConfig();
}
