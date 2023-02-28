// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionEndpointBuilder {
    private static final int DEFAULT_REPLICA_COUNT = Integer.MAX_VALUE;
    private static final boolean DEFAULT_INCLUDE_PRIMARY = true;

    private final FeedRange feedRange;
    private int replicaCount;
    private boolean includePrimary;

    public FaultInjectionEndpointBuilder(FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");
        this.feedRange = feedRange;
        this.replicaCount = DEFAULT_REPLICA_COUNT;
        this.includePrimary = DEFAULT_INCLUDE_PRIMARY;
    }

    public FaultInjectionEndpointBuilder replicaCount(int replicaCount) {
        checkArgument(replicaCount > 0, "Argument 'replicaCount' can not be negative");
        this.replicaCount = replicaCount;
        return this;
    }

    public FaultInjectionEndpointBuilder includePrimary(boolean includePrimary) {
        this.includePrimary = includePrimary;
        return this;
    }

    public FaultInjectionEndpoints build() {
        return new FaultInjectionEndpoints(this.feedRange, this.replicaCount, this.includePrimary);
    }
}
