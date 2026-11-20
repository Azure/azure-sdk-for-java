// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public class CosmosServerThroughputControlConfig extends CosmosThroughputControlConfig {
    private final int throughputBucket;

    public CosmosServerThroughputControlConfig(
        boolean throughputControlEnabled,
        String throughputControlGroupName,
        CosmosPriorityLevel priorityLevel,
        int throughputBucket) {

        super(throughputControlEnabled, throughputControlGroupName, priorityLevel);
        this.throughputBucket = throughputBucket;
    }

    public int getThroughputBucket() {
        return throughputBucket;
    }
}
