// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;

/**
 * Throughput control group configuration.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class ThroughputControlGroupConfig {
    private final String groupName;
    private final Integer targetThroughput;
    private final Double targetThroughputThreshold;
    private final boolean isDefault;

    ThroughputControlGroupConfig(String groupName, Integer targetThroughput, Double targetThroughputThreshold, boolean isDefault) {
       this.groupName= groupName;
       this.targetThroughput = targetThroughput;
       this.targetThroughputThreshold = targetThroughputThreshold;
       this.isDefault = isDefault;
    }

    /**
     * Get the throughput control group name.
     *
     * @return the group name.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Get throughput control group target throughput.
     * Since we allow either TargetThroughput or TargetThroughputThreshold, this value can be null.
     *
     * By default, it is null.
     *
     * @return the target throughput.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getTargetThroughput() {
        return this.targetThroughput;
    }

    /**
     * Get the throughput control group target throughput threshold.
     * Since we allow either TargetThroughput or TargetThroughputThreshold, this value can be null.
     *
     * By default, this value is null.
     *
     * @return the target throughput threshold.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Double getTargetThroughputThreshold() {
        return this.targetThroughputThreshold;
    }

    /**
     * Get whether this throughput control group will be used by default.
     *
     * By default, it is false.
     * If it is true, requests without explicit override of the throughput control group will be routed to this group.
     *
     * @return {@code true} this throughput control group will be used by default unless being override. {@code false} otherwise.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isDefault() {
        return this.isDefault;
    }
}
