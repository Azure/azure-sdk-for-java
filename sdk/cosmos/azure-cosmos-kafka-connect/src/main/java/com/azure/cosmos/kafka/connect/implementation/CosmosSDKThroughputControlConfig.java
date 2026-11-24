// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import java.time.Duration;

public class CosmosSDKThroughputControlConfig extends CosmosThroughputControlConfig {
    private final CosmosAccountConfig throughputControlAccountConfig;
    private final int targetThroughput;
    private final double targetThroughputThreshold;
    private final String globalThroughputControlDatabaseName;
    private final String globalThroughputControlContainerName;
    private final Duration globalThroughputControlRenewInterval;
    private final Duration globalThroughputControlExpireInterval;

    public CosmosSDKThroughputControlConfig(
        boolean throughputControlEnabled,
        String throughputControlGroupName,
        CosmosPriorityLevel priorityLevel,
        CosmosAccountConfig throughputControlAccountConfig,
        int targetThroughput,
        double targetThroughputThreshold,
        String globalThroughputControlDatabaseName,
        String globalThroughputControlContainerName,
        int globalThroughputControlRenewIntervalInMs,
        int globalThroughputControlExpireIntervalInMs) {

        super(throughputControlEnabled, throughputControlGroupName, priorityLevel);
        this.throughputControlAccountConfig = throughputControlAccountConfig;
        this.targetThroughput = targetThroughput;
        this.targetThroughputThreshold = targetThroughputThreshold;
        this.globalThroughputControlDatabaseName = globalThroughputControlDatabaseName;
        this.globalThroughputControlContainerName = globalThroughputControlContainerName;
        this.globalThroughputControlRenewInterval =
            globalThroughputControlRenewIntervalInMs > 0 ? Duration.ofMillis(globalThroughputControlRenewIntervalInMs) : null;
        this.globalThroughputControlExpireInterval =
            globalThroughputControlExpireIntervalInMs > 0 ? Duration.ofMillis(globalThroughputControlExpireIntervalInMs) : null;
    }

    public CosmosAccountConfig getThroughputControlAccountConfig() {
        return throughputControlAccountConfig;
    }

    public int getTargetThroughput() {
        return targetThroughput;
    }

    public double getTargetThroughputThreshold() {
        return targetThroughputThreshold;
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
