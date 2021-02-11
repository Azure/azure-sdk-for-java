// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.util.Beta;

/**
 * Group configuration which will be used in Throughput control.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class ThroughputControlGroup {
    private final ThroughputControlGroupInternal controlGroupInternal;

    ThroughputControlGroup(ThroughputControlGroupInternal groupInternal) {
       this.controlGroupInternal = groupInternal;
    }

    /**
     * Get the throughput control group name.
     *
     * @return the group name.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getGroupName() {
        return this.controlGroupInternal.getGroupName();
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
        return this.controlGroupInternal.getTargetThroughput();
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
        return this.controlGroupInternal.getTargetThroughputThreshold();
    }

    /**
     * Get whether this throughput control group will be used by default.
     *
     * By default, it is false.
     *
     * @return {@code true} this throughput control group will be used by default unless being override. {@code false} otherwise.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isDefault() {
        return this.controlGroupInternal.isDefault();
    }

    ThroughputControlGroupInternal getControlGroupInternal() {
        return this.controlGroupInternal;
    }
}
