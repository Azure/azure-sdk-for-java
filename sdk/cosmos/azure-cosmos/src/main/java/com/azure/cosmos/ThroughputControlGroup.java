// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlMode;
import com.azure.cosmos.util.Beta;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * Group configuration which will be used in Throughput control.
 */
@Beta(value = Beta.SinceVersion.V4_10_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputControlGroup {

    private final static boolean DEFAULT_USE_BY_DEFAULT = false;
    private final static ThroughputControlMode DEFAULT_CONTROL_MODE = ThroughputControlMode.LOCAL;

    private ThroughputControlMode controlMode;
    private String groupName;
    private CosmosAsyncContainer targetContainer;
    private Integer targetThroughput;
    private Double targetThroughputThreshold;
    private boolean useByDefault;

    public ThroughputControlGroup() {
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
    public ThroughputControlMode getControlMode() {
        return this.controlMode;
    }

    /**
     * Set throughput group control mode to local.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    public ThroughputControlGroup localControlMode() {
        this.controlMode = ThroughputControlMode.LOCAL;
        return this;
    }

    /**
     * Set the throughput control group name.
     *
     * @param groupName
     *
     * @return the {@link ThroughputControlGroup}.
     */
    public ThroughputControlGroup groupName(String groupName) {
        checkArgument(StringUtils.isNotEmpty(groupName), "Group name can not be null or empty");
        this.groupName = groupName;

        return this;
    }

    /**
     * Get the throughput control group name.
     *
     * @return the group name.
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Get the throughput control group target container.
     *
     * @return the {@link CosmosAsyncContainer}.
     */
    public CosmosAsyncContainer getTargetContainer() {
        return this.targetContainer;
    }

    /**
     * Set the throughput control group target container.
     *
     * @param targetContainer the target container for the throughput control group.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    public ThroughputControlGroup targetContainer(CosmosAsyncContainer targetContainer) {
        checkArgument(targetContainer != null, "Target container cannot be null");
        this.targetContainer = targetContainer;
        return this;
    }

    /**
     * Get throughput control group target throughput.
     *
     * @return the target throughput.
     */
    public Integer getTargetThroughput() {
        return this.targetThroughput;
    }

    /**
     * Set the throughput control group target throughput. The value should be larger than 0.
     *
     * @param targetThroughput the target throughput for the throughput control group.
     * @return the {@link ThroughputControlGroup}.
     */
    public ThroughputControlGroup targetThroughput(Integer targetThroughput) {
        checkArgument(
            targetThroughput != null && targetThroughput > 0,
            "Target throughput should not be null and should be larger than 0");
        this.targetThroughput = targetThroughput;
        return this;
    }

    /**
     * Get the throughput control group target throughput threshold.
     *
     * @return the target throughput threshold.
     */
    public Double getTargetThroughputThreshold() {
        return this.targetThroughputThreshold;
    }

    /**
     * Set throughput control group target throughput threshold. The value should be larger than 0.
     *
     * @param targetThroughputThreshold the target throughput threshold for the throughput control group.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    public ThroughputControlGroup targetThroughputThreshold(Double targetThroughputThreshold) {
        checkArgument(
            targetThroughputThreshold != null && targetThroughputThreshold > 0,
            "Target throughput threshold should not be null and should be larger than 0");

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
    public boolean isUseByDefault() {
        return this.useByDefault;
    }

    /**
     * Set the throughput control group to be used by default.
     *
     * @return the {@link ThroughputControlGroup}.
     */
    public ThroughputControlGroup useByDefault() {
        this.useByDefault = Boolean.TRUE;
        return this;
    }

    /**
     * Validate whether the throughput control group config is valid.
     */
    public void validate() {
        if (StringUtils.isEmpty(this.groupName)) {
            throw new IllegalArgumentException("Group name can not be null or empty");
        }
        if (this.targetContainer == null) {
            throw new IllegalArgumentException(String.format("Target container is missing for group %s", this.groupName));
        }
        if (this.targetThroughputThreshold == null && this.targetThroughput == null) {
            throw new IllegalArgumentException("Neither target throughput nor target throughput threshold is defined.");
        }
    }

    public String getId() {
        return String.format(
            "%s-%s-%s",
            this.targetContainer.getDatabase().getId(),
            this.targetContainer.getId(),
            this.groupName);
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
        int result = this.groupName != null ? this.groupName.hashCode() : 0;
        if (this.targetContainer != null) {
            result = (397 * result) ^ this.targetContainer.getId().hashCode() ^ this.targetContainer.getDatabase().getId().hashCode();
        }

        return result;
    }
}
