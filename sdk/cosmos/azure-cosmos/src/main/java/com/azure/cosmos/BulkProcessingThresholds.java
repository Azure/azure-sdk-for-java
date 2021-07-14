// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import com.azure.cosmos.util.Beta;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Encapsulates internal state used to dynamically determine max micro batch size for bulk operations.
 * It allows passing this state for one `BulkProcessingOptions` to another in case bulk operations are
 * expected to have similar characteristics and the context for determining the micro batch size should be preserved.
 */
@Beta(value = Beta.SinceVersion.V4_17_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class BulkProcessingThresholds<TContext> {
    private final ConcurrentMap<String, PartitionScopeThresholds<TContext>> partitionScopeThresholds;

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_17_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkProcessingThresholds() {
        this.partitionScopeThresholds = new ConcurrentHashMap<>();
    }

    ConcurrentMap<String, PartitionScopeThresholds<TContext>> getPartitionScopeThresholds() {
        return this.partitionScopeThresholds;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.BulkProcessingThresholdsHelper.setBulkProcessingThresholdsAccessor(
            new ImplementationBridgeHelpers.BulkProcessingThresholdsHelper.BulkProcessingThresholdsAccessor() {
                @Override
                public <T> ConcurrentMap<String, PartitionScopeThresholds<T>> getPartitionScopeThresholds(
                    BulkProcessingThresholds<T> thresholds) {

                    return thresholds.getPartitionScopeThresholds();
                }
            });
    }
}
