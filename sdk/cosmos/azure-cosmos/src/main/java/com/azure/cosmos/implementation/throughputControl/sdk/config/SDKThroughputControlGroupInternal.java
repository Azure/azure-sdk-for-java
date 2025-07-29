// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.IThroughputControlGroup;
import com.azure.cosmos.models.PriorityLevel;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class SDKThroughputControlGroupInternal implements IThroughputControlGroup {
    private final String groupName;
    private final String idPrefix;
    private final String id;
    private final boolean isDefault;
    private final boolean continueOnInitError;
    private final CosmosAsyncContainer targetContainer;
    private final Integer targetThroughput;
    private final Double targetThroughputThreshold;
    private final PriorityLevel priorityLevel;

    public SDKThroughputControlGroupInternal(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        PriorityLevel priorityLevel,
        boolean isDefault,
        boolean continueOnInitError) {

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
        this.priorityLevel = priorityLevel;
        this.isDefault = isDefault;
        this.continueOnInitError = continueOnInitError;

        this.idPrefix = String.format(
            "%s/%s/%s",
            this.targetContainer.getDatabase().getId(),
            this.targetContainer.getId(),
            this.groupName);

        this.id = String.format(
            "%s/%s",
            this.idPrefix,
            getThroughputIdSuffix(targetThroughput, targetThroughputThreshold, priorityLevel));
    }

    public static String getThroughputIdSuffix(
        Integer throughput,
        Double throughputThreshold,
        PriorityLevel priorityLevel) {

        StringBuilder sb = new StringBuilder();
        if (throughput != null) {
            sb.append("t-");
            sb.append(throughput);

            if (throughputThreshold == null) {
                return sb.toString();
            }

            sb.append("_");
        }

        if (throughputThreshold != null) {
            sb.append("tt-");
            sb.append(throughputThreshold);

            if (priorityLevel == null) {
                return sb.toString();
            }

            sb.append("_");
        }

        if (priorityLevel != null) {
            sb.append("p-");
            sb.append(priorityLevel);
        }

        return sb.toString();
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

    /**
     * Get whether allow request to continue on original request flow if throughput control failed on initialization.
     *
     * By default, it is false.
     * If it is true, request will be able to continue on original request flow if throughput control failed on initialization.
     *
     * @return {@code true} request will be allowed to continue on original request flow if throughput control failed on initialization. {@code false} otherwise.
     */
    public boolean isContinueOnInitError() {
        return continueOnInitError;
    }

    public String getIdPrefix() {
        return this.idPrefix;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Get the priority level of the throughput control group used for priority based throttling in the backend
     *
     * By default, it is null.
     *
     * @return the priority level.
     */
    public PriorityLevel getPriorityLevel() {
        return this.priorityLevel;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        SDKThroughputControlGroupInternal that = (SDKThroughputControlGroupInternal) other;

        return Objects.equals(this.id, that.id)
                && this.isDefault == that.isDefault
                && this.continueOnInitError == that.continueOnInitError
                && Objects.equals(this.targetThroughput, that.targetThroughput)
                && Objects.equals(this.targetThroughputThreshold, that.targetThroughputThreshold)
                && Objects.equals(this.priorityLevel, that.priorityLevel);
    }

    public boolean hasSameIdentity(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        SDKThroughputControlGroupInternal that = (SDKThroughputControlGroupInternal) other;

        return Objects.equals(this.idPrefix, that.idPrefix)
            && this.isDefault == that.isDefault
            && this.continueOnInitError == that.continueOnInitError;
    }

    @Override
    public String getDiagnosticsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("name=" + this.groupName);
        sb.append(", default=" + this.isDefault);
        if (this.priorityLevel != null) {
            sb.append(", priorityLevel=" + this.priorityLevel);
        }
        if (this.targetThroughputThreshold != null) {
            sb.append(", targetRUThreshold=" + this.targetThroughputThreshold);
        }
        if (this.targetThroughput != null) {
            sb.append(", targetRU=" + this.targetThroughput);
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (hash * 397) ^ this.id.hashCode();
        hash = (hash * 397) ^ Boolean.hashCode(this.isDefault);
        hash = (hash * 397) ^ Boolean.hashCode(this.continueOnInitError);
        hash = (hash * 397) ^ (this.targetThroughput == null ? 0 : Integer.hashCode(this.targetThroughput));
        hash = (hash * 397) ^ (this.targetThroughputThreshold == null ? 0 : Double.hashCode(this.targetThroughputThreshold));
        hash = (hash * 397) ^ (this.priorityLevel == null ? 0 : this.priorityLevel.hashCode());
        return hash;
    }
}
