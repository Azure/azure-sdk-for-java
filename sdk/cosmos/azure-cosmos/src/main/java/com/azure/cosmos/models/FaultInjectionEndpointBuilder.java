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

    /***
     * Constructor.
     *
     * @param feedRange the feed range.
     */
    public FaultInjectionEndpointBuilder(FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");
        this.feedRange = feedRange;
        this.replicaCount = DEFAULT_REPLICA_COUNT;
        this.includePrimary = DEFAULT_INCLUDE_PRIMARY;
    }

    /***
     * Set the replica count of the fault injection endpoint.
     *
     * @param replicaCount the replica count.
     * @return the builder.
     */
    public FaultInjectionEndpointBuilder replicaCount(int replicaCount) {
        checkArgument(replicaCount > 0, "Argument 'replicaCount' can not be negative");
        this.replicaCount = replicaCount;
        return this;
    }

    /***
     * Flag to indicate whether primary replica addresses can be used.
     *
     * @param includePrimary flag to indicate whether primary addresses can be used.
     * @return the builder.
     */
    public FaultInjectionEndpointBuilder includePrimary(boolean includePrimary) {
        this.includePrimary = includePrimary;
        return this;
    }

    /***
     * Create the fault injection endpoints.
     *
     * @return the {@link FaultInjectionEndpoints}.
     */
    public FaultInjectionEndpoints build() {
        return new FaultInjectionEndpoints(this.feedRange, this.replicaCount, this.includePrimary);
    }
}
