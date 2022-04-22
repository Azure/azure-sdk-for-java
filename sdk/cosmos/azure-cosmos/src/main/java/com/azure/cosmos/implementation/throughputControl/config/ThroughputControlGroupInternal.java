// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ThroughputControlGroupInternal {
    private final String groupName;
    private final String id;
    private final boolean isDefault;
    private final CosmosAsyncContainer targetContainer;
    private final Integer targetThroughput;
    private final Double targetThroughputThreshold;

    public ThroughputControlGroupInternal(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
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
        this.isDefault = isDefault;

        this.id = String.format(
            "%s/%s/%s",
            this.targetContainer.getDatabase().getId(),
            this.targetContainer.getId(),
            this.groupName);
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
     * Get throughput control group target throughput.
     * Since we allow either TargetThroughput or TargetThroughputThreshold, this value can be null.
     *
     * By default, it is null.
     *
     * @return the target throughput.
     */
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
    public boolean isDefault() {
        return this.isDefault;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ThroughputControlGroupInternal that = (ThroughputControlGroupInternal) other;

        return StringUtils.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
