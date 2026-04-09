// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.ChangeFeedCheckpointStrategy;
import com.azure.cosmos.EveryBatchCheckpointStrategy;
import com.azure.cosmos.TimeIntervalCheckpointStrategy;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;

/**
 * Maps public checkpoint strategy options to internal checkpoint frequency settings.
 */
public final class CheckpointFrequencyFactory {
    private CheckpointFrequencyFactory() {
    }

    public static CheckpointFrequency fromOptions(ChangeFeedProcessorOptions options) {
        if (options == null || options.getCheckpointStrategy() == null) {
            return new CheckpointFrequency();
        }

        ChangeFeedCheckpointStrategy strategy = options.getCheckpointStrategy();
        if (strategy instanceof EveryBatchCheckpointStrategy) {
            return new CheckpointFrequency();
        }

        if (strategy instanceof TimeIntervalCheckpointStrategy) {
            return new CheckpointFrequency().withTimeInterval(
                ((TimeIntervalCheckpointStrategy) strategy).getMaxCheckpointDelay());
        }

        throw new IllegalArgumentException("Unsupported checkpoint strategy " + strategy.getClass().getName());
    }
}

