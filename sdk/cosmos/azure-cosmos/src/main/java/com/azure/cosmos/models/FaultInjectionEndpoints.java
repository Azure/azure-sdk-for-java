// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public class FaultInjectionEndpoints {
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
