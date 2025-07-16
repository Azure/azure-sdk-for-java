// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.PriorityLevel;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ServerThroughputControlGroupInternal {
    private final String groupName;
    private final boolean isDefault;
    private final CosmosAsyncContainer targetContainer;
    private final PriorityLevel priorityLevel;

    public ServerThroughputControlGroupInternal(
        String groupName,
        boolean isDefault,
        PriorityLevel priorityLevel,
        CosmosAsyncContainer targetContainer) {

        checkArgument(StringUtils.isNotEmpty(groupName), "Argument 'groupName' cannot be null or empty.");
        checkNotNull(targetContainer, "Argument 'targetContainer' can not be null");

        this.groupName = groupName;
        this.isDefault = isDefault;
        this.targetContainer = targetContainer;
        this.priorityLevel = priorityLevel;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public CosmosAsyncContainer getTargetContainer() {
        return targetContainer;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerThroughputControlGroupInternal that = (ServerThroughputControlGroupInternal) o;
        return isDefault == that.isDefault
            && Objects.equals(groupName, that.groupName)
            && Objects.equals(targetContainer.getId(), that.targetContainer.getId())
            && Objects.equals(priorityLevel, that.priorityLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, isDefault, targetContainer, priorityLevel);
    }
}
