// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicReference;

public class PointOperationContextForPerPartitionAutomaticFailover {

    private final boolean isOperationEligibleForParallelWriteRegionDiscovery;

    private final boolean isParallelWriteRegionDiscoveryAttempt;

    private final AtomicReference<String> overriddenLocationForPrimaryRequest;

    public PointOperationContextForPerPartitionAutomaticFailover(
        boolean isOperationEligibleForParallelWriteRegionDiscoveryAttempt,
        boolean isParallelWriteRegionDiscoveryAttempt) {

        this.isOperationEligibleForParallelWriteRegionDiscovery = isOperationEligibleForParallelWriteRegionDiscoveryAttempt;
        this.isParallelWriteRegionDiscoveryAttempt = isParallelWriteRegionDiscoveryAttempt;
        this.overriddenLocationForPrimaryRequest = new AtomicReference<>();
    }

    public boolean isOperationEligibleForParallelWriteRegionDiscovery() {
        return this.isOperationEligibleForParallelWriteRegionDiscovery;
    }

    public boolean isParallelWriteRegionDiscoveryAttempt() {
        return this.isParallelWriteRegionDiscoveryAttempt;
    }

    public void setOverriddenLocationForPrimaryRequest(String location) {
        this.overriddenLocationForPrimaryRequest.set(location);
    }

    public String getOverriddenLocationForPrimaryRequest() {
        return this.overriddenLocationForPrimaryRequest.get();
    }
}
