// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

public class CosmosSinkWriteConfig {
    private final boolean bulkEnabled;
    private final int bulkMaxConcurrentCosmosPartitions;
    private final int bulkInitialBatchSize;
    private final ItemWriteStrategy itemWriteStrategy;
    private final int maxRetryCount;

    private final ToleranceOnErrorLevel toleranceOnErrorLevel;

    public CosmosSinkWriteConfig(
        boolean bulkEnabled,
        int bulkMaxConcurrentCosmosPartitions,
        int bulkInitialBatchSize,
        ItemWriteStrategy itemWriteStrategy,
        int maxRetryCount,
        ToleranceOnErrorLevel toleranceOnErrorLevel) {

        this.bulkEnabled = bulkEnabled;
        this.bulkMaxConcurrentCosmosPartitions = bulkMaxConcurrentCosmosPartitions;
        this.bulkInitialBatchSize = bulkInitialBatchSize;
        this.itemWriteStrategy = itemWriteStrategy;
        this.maxRetryCount = maxRetryCount;
        this.toleranceOnErrorLevel = toleranceOnErrorLevel;
    }

    public boolean isBulkEnabled() {
        return bulkEnabled;
    }

    public int getBulkMaxConcurrentCosmosPartitions() {
        return bulkMaxConcurrentCosmosPartitions;
    }

    public int getBulkInitialBatchSize() {
        return bulkInitialBatchSize;
    }

    public ItemWriteStrategy getItemWriteStrategy() {
        return itemWriteStrategy;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public ToleranceOnErrorLevel getToleranceOnErrorLevel() {
        return toleranceOnErrorLevel;
    }
}
