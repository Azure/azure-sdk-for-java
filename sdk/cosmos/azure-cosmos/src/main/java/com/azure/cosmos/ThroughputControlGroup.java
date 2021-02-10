// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlMode;
import com.azure.cosmos.util.Beta;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Group configuration which will be used in Throughput control.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputControlGroup {
    private final ThroughputControlMode controlMode;
    private final String groupName;
    private final String id;
    private final boolean isDefault;
    private final CosmosAsyncContainer targetContainer;
    private final Integer targetThroughput;
    private final Double targetThroughputThreshold;

    ThroughputControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        ThroughputControlMode controlMode,
        boolean isDefault) {

        checkArgument(StringUtils.isNotEmpty(groupName), "Group name can not be null or empty");
        checkNotNull(targetContainer, "Target container can not be null");
        checkArgument(targetThroughput == null || targetThroughput > 0, "Target throughput should be greater than 0");
        checkArgument(
            targetThroughputThreshold == null || (targetThroughputThreshold > 0 && targetThroughputThreshold <= 1),
            "Target throughput threshold should between (0, 1]");

        this.groupName = groupName;
        this.targetContainer = targetContainer;
        this.targetThroughput = targetThroughput;
        this.targetThroughputThreshold = targetThroughputThreshold;
        this.controlMode = controlMode;
        this.isDefault = isDefault;

        this.id = this.getId();
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
     * Get the throughput control group target container.
     *
     * @return the {@link CosmosAsyncContainer}.
     */
    CosmosAsyncContainer getTargetContainer() {
        return this.targetContainer;
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
     *
     * @return {@code true} this throughput control group will be used by default unless being override. {@code false} otherwise.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isDefault() {
        return this.isDefault;
    }

    ThroughputControlMode getControlMode() {
        return this.controlMode;
    }

    private String getId() {
        return this.targetContainer.getDatabase().getId() + "." + this.targetContainer.getId() + "." + this.groupName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ThroughputControlGroup that = (ThroughputControlGroup) other;

        return StringUtils.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
