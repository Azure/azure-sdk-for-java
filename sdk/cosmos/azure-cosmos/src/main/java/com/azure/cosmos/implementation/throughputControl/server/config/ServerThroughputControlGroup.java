// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.IThroughputControlGroup;
import com.azure.cosmos.models.PriorityLevel;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ServerThroughputControlGroup implements IThroughputControlGroup {
    private final String groupName;
    private final boolean isDefault;
    private final CosmosAsyncContainer targetContainer;
    private final PriorityLevel priorityLevel;
    private final Integer throughputBucket;

    public ServerThroughputControlGroup(
        String groupName,
        boolean isDefault,
        PriorityLevel priorityLevel,
        Integer throughputBucket,
        CosmosAsyncContainer targetContainer) {

        checkArgument(StringUtils.isNotEmpty(groupName), "Argument 'groupName' cannot be null or empty.");
        checkNotNull(targetContainer, "Argument 'targetContainer' can not be null");
        checkArgument(
            priorityLevel != null || throughputBucket != null,
            "At least one of 'priorityLevel' or 'throughputBucket' must be provided.");
        checkArgument(throughputBucket == null || throughputBucket >= 0, "Target throughput bucket should be no less than 0");

        this.groupName = groupName;
        this.isDefault = isDefault;
        this.targetContainer = targetContainer;
        this.priorityLevel = priorityLevel;
        this.throughputBucket = throughputBucket;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public CosmosAsyncContainer getTargetContainer() {
        return this.targetContainer;
    }

    public PriorityLevel getPriorityLevel() {
        return this.priorityLevel;
    }

    public Integer getThroughputBucket() {
        return this.throughputBucket;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerThroughputControlGroup that = (ServerThroughputControlGroup) o;
        return isDefault == that.isDefault
            && Objects.equals(groupName, that.groupName)
            && Objects.equals(targetContainer, that.targetContainer)
            && Objects.equals(priorityLevel, that.priorityLevel)
            && Objects.equals(throughputBucket, that.throughputBucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, isDefault, targetContainer, priorityLevel, throughputBucket);
    }

    /***
     * Config show up in the diagnostics.
     * @return the config string.
     */
    @Override
    public String getDiagnosticsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("name=" + this.groupName);
        sb.append(", default=" + this.isDefault);

        if (this.priorityLevel != null) {
            sb.append(", priorityLevel=" + this.priorityLevel);
        }

        if (this.throughputBucket != null) {
            sb.append(", throughputBucket=" + this.throughputBucket);
        }

        sb.append(")");
        return sb.toString();
    }
}
