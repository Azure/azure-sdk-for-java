// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * Checkpoint strategy that writes a checkpoint after each processed batch.
 */
public final class EveryBatchCheckpointStrategy extends ChangeFeedCheckpointStrategy {
    /**
     * Creates a new every-batch checkpoint strategy.
     */
    public EveryBatchCheckpointStrategy() {
    }
}

