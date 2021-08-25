// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

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
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosBulkExecutionThresholdsState {
    private final ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds;

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBulkExecutionThresholdsState() {
        this.partitionScopeThresholds = new ConcurrentHashMap<>();
    }

    CosmosBulkExecutionThresholdsState(ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds) {
        this.partitionScopeThresholds = partitionScopeThresholds;
    }

    ConcurrentMap<String, PartitionScopeThresholds> getPartitionScopeThresholds() {
        return this.partitionScopeThresholds;
    }



    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosBulkExecutionThresholdsStateHelper.setBulkExecutionThresholdsAccessor(
            new ImplementationBridgeHelpers.CosmosBulkExecutionThresholdsStateHelper.CosmosBulkExecutionThresholdsStateAccessor() {
                @Override
                public ConcurrentMap<String, PartitionScopeThresholds> getPartitionScopeThresholds(CosmosBulkExecutionThresholdsState thresholds) {
                    return thresholds.getPartitionScopeThresholds();
                }

                @Override
                public CosmosBulkExecutionThresholdsState createWithPartitionScopeThresholds(ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds) {
                    return new CosmosBulkExecutionThresholdsState(partitionScopeThresholds);
                }
            }
        );
    }
}
