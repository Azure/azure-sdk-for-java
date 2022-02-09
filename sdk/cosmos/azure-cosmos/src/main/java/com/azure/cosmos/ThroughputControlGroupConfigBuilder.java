// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * The throughput control group config builder.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputControlGroupConfigBuilder {
    private String groupName;
    private Integer targetThroughput;
    private Double targetThroughputThreshold;
    private boolean isDefault;

    /**
     * Set the throughput control group name.
     *
     * @param groupName The throughput control group name.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setGroupName(String groupName) {
        checkArgument(StringUtils.isNotEmpty(groupName), "Group name cannot be null nor empty");

        this.groupName = groupName;
        return this;
    }

    /**
     * Set the throughput control group target throughput.
     *
     * The target throughput value should be greater than 0.
     *
     * @param targetThroughput The target throughput for the control group.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setTargetThroughput(int targetThroughput) {
        checkArgument(targetThroughput > 0, "Target throughput should be greater than 0");

        this.targetThroughput = targetThroughput;
        return this;
    }

    /**
     * Set the throughput control group target throughput threshold.
     *
     * The target throughput threshold value should be between (0, 1].
     *
     * @param targetThroughputThreshold The target throughput threshold for the control group.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setTargetThroughputThreshold(double targetThroughputThreshold) {
        checkArgument(targetThroughputThreshold > 0 && targetThroughputThreshold <= 1, "Target throughput threshold should between (0, 1]");

        this.targetThroughputThreshold = targetThroughputThreshold;
        return this;
    }


    /**
     * Set whether this throughput control group will be used by default.
     * If set to true, requests without explicit override of the throughput control group will be routed to this group.
     *
     * @param aDefault The flag to indicate whether the throughput control group will be used by default.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setDefault(boolean aDefault) {
        isDefault = aDefault;
        return this;
    }

    /**
     * Validate the throughput configuration and create a new throughput control group config item.
     *
     * @return A new {@link ThroughputControlGroupConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfig build() {
        if (StringUtils.isEmpty(this.groupName)) {
            throw new IllegalArgumentException("Group name cannot be null nor empty");
        }
        if (this.targetThroughput == null && this.targetThroughputThreshold == null) {
            throw new IllegalArgumentException("Neither targetThroughput nor targetThroughputThreshold is defined.");
        }

        return new ThroughputControlGroupConfig(groupName, this.targetThroughput, this.targetThroughputThreshold, isDefault);
    }
}
