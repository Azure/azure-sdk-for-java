// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.PriorityLevel;
import com.azure.cosmos.util.Beta;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * The throughput control group config builder.
 */
public class ThroughputControlGroupConfigBuilder {
    private static final boolean DEFAULT_CONTINUE_ON_INIT_ERROR = false;
    private String groupName;
    private Integer targetThroughput;
    private Double targetThroughputThreshold;
    private boolean isDefault;
    private PriorityLevel priorityLevel;
    private boolean continueOnInitError = DEFAULT_CONTINUE_ON_INIT_ERROR;

    /**
     * Set the throughput control group name.
     *
     * @param groupName The throughput control group name.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    @Deprecated
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setGroupName(String groupName) {
        checkArgument(StringUtils.isNotEmpty(groupName), "Group name cannot be null nor empty");

        this.groupName = groupName;
        return this;
    }

    /**
     * Set the throughput control group name.
     *
     * @param groupName The throughput control group name.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    public ThroughputControlGroupConfigBuilder groupName(String groupName) {
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
    @Deprecated
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setTargetThroughput(int targetThroughput) {
        checkArgument(targetThroughput > 0, "Target throughput should be greater than 0");

        this.targetThroughput = targetThroughput;
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
    public ThroughputControlGroupConfigBuilder targetThroughput(int targetThroughput) {
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
    @Deprecated
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setTargetThroughputThreshold(double targetThroughputThreshold) {
        checkArgument(targetThroughputThreshold > 0 && targetThroughputThreshold <= 1, "Target throughput threshold should between (0, 1]");

        this.targetThroughputThreshold = targetThroughputThreshold;
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
    public ThroughputControlGroupConfigBuilder targetThroughputThreshold(double targetThroughputThreshold) {
        checkArgument(targetThroughputThreshold > 0 && targetThroughputThreshold <= 1, "Target throughput threshold should between (0, 1]");

        this.targetThroughputThreshold = targetThroughputThreshold;
        return this;
    }

    /**
     * Set the throughput control group priority level.
     * The priority level is used to determine which requests will be throttled first when the total throughput of all control groups exceeds the max throughput.
     *
     * By Default PriorityLevel for each request is treated as High. It can be explicitly set to Low for some requests.
     *
     * Refer to https://aka.ms/CosmosDB/PriorityBasedExecution for more details.
     *
     * @param priorityLevel The priority level for the control group.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    public ThroughputControlGroupConfigBuilder priorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
        return this;
    }

    /**
     * Set whether this throughput control group will be used by default.
     * If set to true, requests without explicit override of the throughput control group will be routed to this group.
     *
     * @param aDefault The flag to indicate whether the throughput control group will be used by default.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    @Deprecated
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroupConfigBuilder setDefault(boolean aDefault) {
        this.isDefault = aDefault;
        return this;
    }

    /**
     * Set whether this throughput control group will be used by default.
     * If set to true, requests without explicit override of the throughput control group will be routed to this group.
     *
     * @param aDefault The flag to indicate whether the throughput control group will be used by default.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    public ThroughputControlGroupConfigBuilder defaultControlGroup(boolean aDefault) {
        this.isDefault = aDefault;
        return this;
    }

    /**
     * Set whether allow request to continue on original request flow if throughput control controller failed on initialization.
     * If set to true, requests will be able to fall back to original request flow if throughput control controller failed on initialization.
     *
     * @param continueOnInitError The flag to indicate whether request is allowed to fall back to original request flow.
     * @return The {@link ThroughputControlGroupConfigBuilder}.
     */
    public ThroughputControlGroupConfigBuilder continueOnInitError(boolean continueOnInitError) {
        this.continueOnInitError = continueOnInitError;
        return this;
    }

    /**
     * Validate the throughput configuration and create a new throughput control group config item.
     *
     * @return A new {@link ThroughputControlGroupConfig}.
     */
    public ThroughputControlGroupConfig build() {
        if (StringUtils.isEmpty(this.groupName)) {
            throw new IllegalArgumentException("Group name cannot be null nor empty");
        }
        if (this.targetThroughput == null && this.targetThroughputThreshold == null && this.priorityLevel == null) {
            throw new IllegalArgumentException("All targetThroughput, targetThroughputThreshold and priorityLevel cannot be null or empty.");
        }
        if (this.targetThroughput == null && this.targetThroughputThreshold == null) {
            this.targetThroughput = Integer.MAX_VALUE;
        }

        return new ThroughputControlGroupConfig(
                this.groupName,
                this.targetThroughput,
                this.targetThroughputThreshold,
                this.priorityLevel,
                this.isDefault,
                this.continueOnInitError);
    }
}
