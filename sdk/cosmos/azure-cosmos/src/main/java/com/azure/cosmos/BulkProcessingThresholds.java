// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import com.azure.cosmos.util.Beta;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Beta(value = Beta.SinceVersion.V4_17_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class BulkProcessingThresholds<TContext> {
    private final ConcurrentMap<String, PartitionScopeThresholds<TContext>> partitionScopeThresholds;

    @Beta(value = Beta.SinceVersion.V4_17_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkProcessingThresholds() {
        this.partitionScopeThresholds = new ConcurrentHashMap<>();
    }

    ConcurrentMap<String, PartitionScopeThresholds<TContext>> getPartitionScopeThresholds() {
        return this.partitionScopeThresholds;
    }
}
