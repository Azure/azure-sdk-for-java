// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.CosmosAsyncContainer;

public class LocalThroughputControlGroup extends ThroughputControlGroupInternal {

    public LocalThroughputControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        boolean isDefault) {
        super (groupName, targetContainer, targetThroughput, targetThroughputThreshold, isDefault);
    }
}
