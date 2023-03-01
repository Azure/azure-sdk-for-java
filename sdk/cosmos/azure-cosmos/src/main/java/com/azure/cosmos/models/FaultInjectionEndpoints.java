// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/***
 * Fault injection endpoints.
 */
public class FaultInjectionEndpoints {
    private final FeedRange feedRange;
    private boolean includePrimary;
    private int replicaCount;

    FaultInjectionEndpoints(FeedRange feedRange, int replicaCount, boolean includePrimary) {
        this.feedRange = feedRange;
        this.replicaCount = replicaCount;
        this.includePrimary = includePrimary;
    }

    /***
     * Get the feed range.
     *
     * @return the feed range.
     */
    public FeedRange getFeedRange() {
        return this.feedRange;
    }

    /***
     * Get the flag which indicates whether primary replica address can be used.
     *
     * @return the flag which indicates whether primary replica address can be used.
     */
    public boolean isIncludePrimary() {
        return includePrimary;
    }

    /***
     * Get the replica count.
     * This is used to indicate how many physical addresses can be applied the fault injection rule.
     *
     * @return the replica count.
     */
    public int getReplicaCount() {
        return replicaCount;
    }
}
