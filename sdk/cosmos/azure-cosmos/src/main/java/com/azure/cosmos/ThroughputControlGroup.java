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
@Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputControlGroup {

    private final static boolean DEFAULT_USE_BY_DEFAULT = false;
    private final static ThroughputControlMode DEFAULT_CONTROL_MODE = ThroughputControlMode.LOCAL;

    private final String groupName;
    private final CosmosAsyncContainer targetContainer;

    private ThroughputControlMode controlMode;
    private String id;
    private Integer targetThroughput;
    private Double targetThroughputThreshold;
    private boolean useByDefault;

    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroup(String groupName, CosmosAsyncContainer targetContainer) {
        checkArgument(StringUtils.isNotEmpty(groupName), "Group name can not be null or empty");
        checkNotNull(targetContainer, "Target container can not be null");

        this.groupName = groupName;
        this.targetContainer = targetContainer;
        this.controlMode = DEFAULT_CONTROL_MODE;
        this.useByDefault = DEFAULT_USE_BY_DEFAULT;
    }

    /**
     * Get throughput group control mode.
     *
     * By default, it will be local control mode.
     *
     * @return the {@link ThroughputControlMode}.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlMode getControlMode() {
        return this.controlMode;
    }

    /**
     * Set throughput group control mode to local.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroup setLocalControlMode() {
        this.controlMode = ThroughputControlMode.LOCAL;
        return this;
    }

    /**
     * Get the throughput control group name.
     *
     * @return the group name.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Get the throughput control group target container.
     *
     * @return the {@link CosmosAsyncContainer}.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosAsyncContainer getTargetContainer() {
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
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getTargetThroughput() {
        return this.targetThroughput;
    }

    /**
     * Set the throughput control group target throughput. The value should be larger than 0.
     *
     * @param targetThroughput the target throughput for the throughput control group.
     * @return the {@link ThroughputControlGroup}.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroup setTargetThroughput(int targetThroughput) {
        checkArgument(targetThroughput > 0, "Target throughput should be larger than 0");
        this.targetThroughput = targetThroughput;
        return this;
    }

    /**
     * Get the throughput control group target throughput threshold.
     * Since we allow either TargetThroughput or TargetThroughputThreshold, this value can be null.
     *
     * By default, this value is null.
     *
     * @return the target throughput threshold.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Double getTargetThroughputThreshold() {
        return this.targetThroughputThreshold;
    }

    /**
     * Set throughput control group target throughput threshold. The value should be larger than 0 and less than 1.
     *
     * @param targetThroughputThreshold the target throughput threshold for the throughput control group.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroup setTargetThroughputThreshold(double targetThroughputThreshold) {
        checkArgument(
            targetThroughputThreshold > 0 && targetThroughputThreshold <= 1,
            "Target throughput threshold should between (0, 1]");

        this.targetThroughputThreshold = targetThroughputThreshold;
        return this;
    }

    /**
     * Get whether this throughput control group will be used by default.
     *
     * By default, it is false.
     *
     * @return {@code true} this throughput control group will be used by default unless being override. {@code false} otherwise.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isUseByDefault() {
        return this.useByDefault;
    }

    /**
     * Set the throughput control group to be used by default.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlGroup setUseByDefault() {
        this.useByDefault = Boolean.TRUE;
        return this;
    }

    /**
     * Get the id of the throughput control group.
     *
     * @return the throughput control group id.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getId() {
        if (StringUtils.isEmpty(this.id)) {
            this.validate();
            this.id = this.targetContainer.getDatabase().getId() + "." + this.targetContainer.getId() + "." + this.groupName;
        }

        return this.id;
    }

    /**
     * Validate whether the throughput control group config is valid.
     */
    private void validate() {
        if (this.targetThroughputThreshold == null && this.targetThroughput == null) {
            throw new IllegalArgumentException("Neither target throughput nor target throughput threshold is defined.");
        }
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

        return StringUtils.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
