// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import java.time.Duration;

public class CosmosThroughputControlConfig {
    private final boolean throughputControlEnabled;
    private final CosmosAccountConfig throughputControlAccountConfig;
    private final String throughputControlGroupName;
    private final int targetThroughput;
    private final double targetThroughputThreshold;
    private final CosmosPriorityLevel priorityLevel;
    private final String globalThroughputControlDatabaseName;
    private final String globalThroughputControlContainerName;
    private final Duration globalThroughputControlRenewInterval;
    private final Duration globalThroughputControlExpireInterval;

    public CosmosThroughputControlConfig(
        boolean throughputControlEnabled,
        CosmosAccountConfig throughputControlAccountConfig,
        String throughputControlGroupName,
        int targetThroughput,
        double targetThroughputThreshold,
        CosmosPriorityLevel priorityLevel,
        String globalThroughputControlDatabaseName,
        String globalThroughputControlContainer,
        int globalThroughputControlRenewIntervalInMs,
        int globalThroughputControlExpireIntervalInMs) {

        this.throughputControlEnabled = throughputControlEnabled;
        this.throughputControlAccountConfig = throughputControlAccountConfig;
        this.throughputControlGroupName = throughputControlGroupName;
        this.targetThroughput = targetThroughput;
        this.targetThroughputThreshold = targetThroughputThreshold;
        this.priorityLevel = priorityLevel;
        this.globalThroughputControlDatabaseName = globalThroughputControlDatabaseName;
        this.globalThroughputControlContainerName = globalThroughputControlContainer;
        this.globalThroughputControlRenewInterval =
            globalThroughputControlRenewIntervalInMs > 0 ? Duration.ofMillis(globalThroughputControlRenewIntervalInMs) : null;
        this.globalThroughputControlExpireInterval =
            globalThroughputControlExpireIntervalInMs > 0 ? Duration.ofMillis(globalThroughputControlExpireIntervalInMs) : null;
    }

    public boolean isThroughputControlEnabled() {
        return throughputControlEnabled;
    }

    public CosmosAccountConfig getThroughputControlAccountConfig() {
        return throughputControlAccountConfig;
    }

    public String getThroughputControlGroupName() {
        return throughputControlGroupName;
    }

    public int getTargetThroughput() {
        return targetThroughput;
    }

    public double getTargetThroughputThreshold() {
        return targetThroughputThreshold;
    }

    public CosmosPriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public String getGlobalThroughputControlDatabaseName() {
        return globalThroughputControlDatabaseName;
    }

    public String getGlobalThroughputControlContainerName() {
        return globalThroughputControlContainerName;
    }

    public Duration getGlobalThroughputControlRenewInterval() {
        return globalThroughputControlRenewInterval;
    }

    public Duration getGlobalThroughputControlExpireInterval() {
        return globalThroughputControlExpireInterval;
    }
}
