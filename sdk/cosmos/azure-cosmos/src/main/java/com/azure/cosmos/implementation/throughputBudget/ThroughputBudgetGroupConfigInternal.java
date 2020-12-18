// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.ThroughputBudgetDistributedControlConfig;
import com.azure.cosmos.ThroughputBudgetGroupConfig;
import com.azure.cosmos.ThroughputBudgetGroupControlMode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputBudgetGroupConfigInternal {
    private final ThroughputBudgetGroupControlMode controlMode;
    private final ThroughputBudgetDistributedControlConfig distributedControlConfig;
    private final String groupName;
    private final String hostName;
    private final CosmosAsyncContainer targetContainer;
    private final String targetContainerRid;
    private final Integer throughputLimit;
    private final Double throughputLimitThreshold;
    private final boolean useByDefault;

    public ThroughputBudgetGroupConfigInternal(
        ThroughputBudgetGroupConfig groupConfig,
        String hostName,
        String targetContainerRid) {

        checkArgument(StringUtils.isNotEmpty(hostName), "hostName can not be null or empty");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid cannot be null or empty");
        checkNotNull(groupConfig, "Group config can not be null");

        this.controlMode = groupConfig.getControlMode();
        this.distributedControlConfig = groupConfig.getDistributedControlConfig();
        this.groupName = groupConfig.getGroupName();
        this.hostName = hostName;
        this.targetContainer = groupConfig.getTargetContainer();
        this.targetContainerRid = targetContainerRid;
        this.throughputLimit = groupConfig.getThroughputLimit();
        this.throughputLimitThreshold = groupConfig.getThroughputLimitThreshold();
        this.useByDefault = groupConfig.isUseByDefault();
    }

    public ThroughputBudgetGroupControlMode getControlMode() {
        return controlMode;
    }

    public ThroughputBudgetDistributedControlConfig getDistributedControlConfig() {
        return distributedControlConfig;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getHostName() {
        return hostName;
    }

    public CosmosAsyncContainer getTargetContainer() {
        return targetContainer;
    }

    public String getTargetContainerRid() {
        return targetContainerRid;
    }

    public Integer getThroughputLimit() {
        return throughputLimit;
    }

    public Double getThroughputLimitThreshold() {
        return throughputLimitThreshold;
    }

    public boolean isUseByDefault() {
        return useByDefault;
    }
}
