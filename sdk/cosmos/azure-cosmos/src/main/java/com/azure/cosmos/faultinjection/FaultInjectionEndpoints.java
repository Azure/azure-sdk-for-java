// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.util.Beta;

/***
 * Fault injection endpoints.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class FaultInjectionEndpoints {
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
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FeedRange getFeedRange() {
        return this.feedRange;
    }

    /***
     * Get the flag which indicates whether primary replica address can be used.
     *
     * @return the flag which indicates whether primary replica address can be used.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isIncludePrimary() {
        return includePrimary;
    }

    /***
     * Get the replica count.
     * This is used to indicate how many physical addresses can be applied the fault injection rule.
     *
     * @return the replica count.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getReplicaCount() {
        return replicaCount;
    }

    @Override
    public String toString() {
        return "FaultInjectionEndpoints{" +
            "feedRange=" + feedRange +
            ", includePrimary=" + includePrimary +
            ", replicaCount=" + replicaCount +
            '}';
    }
}
