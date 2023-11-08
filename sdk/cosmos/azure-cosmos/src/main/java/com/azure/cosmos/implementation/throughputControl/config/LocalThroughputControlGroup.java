// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PriorityLevel;

public class LocalThroughputControlGroup extends ThroughputControlGroupInternal {

    public LocalThroughputControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        PriorityLevel priorityLevel,
        boolean isDefault,
        boolean continueOnInitError) {
        super (groupName, targetContainer, targetThroughput, targetThroughputThreshold, priorityLevel, isDefault, continueOnInitError);
    }
}
