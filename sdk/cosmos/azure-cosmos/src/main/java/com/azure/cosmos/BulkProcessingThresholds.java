// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BulkProcessingThresholds<TContext> {
    private final ConcurrentMap<String, PartitionScopeThresholds<TContext>> partitionScopeThresholds;

    public BulkProcessingThresholds() {
        this.partitionScopeThresholds = new ConcurrentHashMap<>();
    }

    ConcurrentMap<String, PartitionScopeThresholds<TContext>> getPartitionScopeThresholds() {
        return this.partitionScopeThresholds;
    }
}
