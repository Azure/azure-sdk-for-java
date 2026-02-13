// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public abstract class CosmosThroughputControlConfig {
    private final boolean throughputControlEnabled;
    private final String throughputControlGroupName;
    private final CosmosPriorityLevel priorityLevel;

    protected CosmosThroughputControlConfig(
        boolean throughputControlEnabled,
        String throughputControlGroupName,
        CosmosPriorityLevel priorityLevel) {

        this.throughputControlEnabled = throughputControlEnabled;
        this.throughputControlGroupName = throughputControlGroupName;
        this.priorityLevel = priorityLevel;
    }

    public boolean isThroughputControlEnabled() {
        return throughputControlEnabled;
    }

    public String getThroughputControlGroupName() {
        return throughputControlGroupName;
    }

    public CosmosPriorityLevel getPriorityLevel() {
        return priorityLevel;
    }
}
