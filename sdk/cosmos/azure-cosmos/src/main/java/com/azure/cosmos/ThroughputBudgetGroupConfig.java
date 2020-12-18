// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputBudgetGroupConfig {

    private final static boolean DEFAULT_USE_BY_DEFAULT = false;
    private final static ThroughputBudgetGroupControlMode DEFAULT_CONTROL_MODE = ThroughputBudgetGroupControlMode.LOCAL;

    private ThroughputBudgetGroupControlMode controlMode;
    private ThroughputBudgetDistributedControlConfig distributedControlConfig;
    private String groupName;
    private CosmosAsyncContainer targetContainer;
    private Integer throughputLimit;
    private Double throughputLimitThreshold;
    private boolean useByDefault;

    public ThroughputBudgetGroupConfig() {
        this.controlMode = DEFAULT_CONTROL_MODE;
        this.useByDefault = DEFAULT_USE_BY_DEFAULT;
    }

    public ThroughputBudgetGroupControlMode getControlMode() {
        return controlMode;
    }

    public ThroughputBudgetGroupConfig localControlMode() {
        this.controlMode = ThroughputBudgetGroupControlMode.LOCAL;
        return this;
    }

    public ThroughputBudgetGroupConfig distributedControlMode(ThroughputBudgetDistributedControlConfig distributedControlConfig) {
        checkNotNull(distributedControlConfig, "Distributed control configuration can not be null");
        this.distributedControlConfig = distributedControlConfig;
        this.controlMode = ThroughputBudgetGroupControlMode.DISTRIBUTED;
        return this;
    }

    public ThroughputBudgetDistributedControlConfig getDistributedControlConfig() {
        return distributedControlConfig;
    }

    public ThroughputBudgetGroupConfig groupName(String groupName) {
        checkArgument(StringUtils.isNotEmpty(groupName), "Group name can not be null or empty");
        this.groupName = groupName;

        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public CosmosAsyncContainer getTargetContainer() {
        return targetContainer;
    }

    public ThroughputBudgetGroupConfig targetContainer(CosmosAsyncContainer targetContainer) {
        checkNotNull(targetContainer, "Target container cannot be null");

        if (this.targetContainer != null) {
            throw new IllegalArgumentException("Target container has been set already");
        }

        this.targetContainer = targetContainer;
        return this;
    }

    public int getThroughputLimit() {
        return throughputLimit;
    }

    public ThroughputBudgetGroupConfig throughputLimit(int throughputLimit) {
        checkArgument(throughputLimit > 0, "Throughput limit should be larger than 0");
        this.throughputLimit = throughputLimit;
        return this;
    }

    public Double getThroughputLimitThreshold() {
        return throughputLimitThreshold;
    }

    public ThroughputBudgetGroupConfig throughputLimitThreshold(double throughputLimitThreshold) {
        checkArgument(throughputLimitThreshold > 0, "Throughput limit threshold should be larger than 0");
        this.throughputLimitThreshold = throughputLimitThreshold;
        return this;
    }

    public boolean isUseByDefault() {
        return useByDefault;
    }

    public ThroughputBudgetGroupConfig useByDefault() {
        this.useByDefault = Boolean.TRUE;
        return this;
    }

    void validate() {
        if (StringUtils.isEmpty(this.groupName)) {
            throw new IllegalArgumentException("Group name can not be null or empty");
        }
        if (this.targetContainer == null) {
            throw new IllegalArgumentException(String.format("Target is missing for group %s", this.groupName));
        }
        if (this.throughputLimitThreshold == null && this.throughputLimit == null) {
            throw new IllegalArgumentException("Throughput budget is not configured");
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

        ThroughputBudgetGroupConfig that = (ThroughputBudgetGroupConfig) other;

        return StringUtils.equals(this.getId(), that.getId());
    }
}
