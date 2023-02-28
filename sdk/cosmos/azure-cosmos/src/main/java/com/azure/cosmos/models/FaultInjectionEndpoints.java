// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

// TODO: should this also change into builder model
public class FaultInjectionEndpoints {
    private static final int DEFAULT_FAULT_INJECTION_REPLICA_COUNT = 4;
    private static final boolean DEFAULT_FAULT_INJECTION_INCLUDE_PRIMARY = true;

    private final FeedRange feedRange;
    private boolean includePrimary;
    private int replicaCount;

    FaultInjectionEndpoints(FeedRange feedRange, int replicaCount, boolean includePrimary) {
        this.feedRange = feedRange;
        this.replicaCount = replicaCount;
        this.includePrimary = includePrimary;
    }

    public FeedRange getFeedRange() {
        return this.feedRange;
    }

    public boolean isIncludePrimary() {
        return includePrimary;
    }

    public int getReplicaCount() {
        return replicaCount;
    }
}
