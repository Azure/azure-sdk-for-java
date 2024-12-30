// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class PointOperationContextForPerPartitionAutomaticFailover {

    private final boolean isOperationEligibleForParallelWriteRegionDiscovery;

    private final boolean isParallelWriteRegionDiscoveryAttempt;

    public PointOperationContextForPerPartitionAutomaticFailover(
        boolean isOperationEligibleForParallelWriteRegionDiscoveryAttempt,
        boolean isParallelWriteRegionDiscoveryAttempt) {

        this.isOperationEligibleForParallelWriteRegionDiscovery = isOperationEligibleForParallelWriteRegionDiscoveryAttempt;
        this.isParallelWriteRegionDiscoveryAttempt = isParallelWriteRegionDiscoveryAttempt;
    }

    public boolean isOperationEligibleForParallelWriteRegionDiscovery() {
        return this.isOperationEligibleForParallelWriteRegionDiscovery;
    }

    public boolean isParallelWriteRegionDiscoveryAttempt() {
        return this.isParallelWriteRegionDiscoveryAttempt;
    }
}
