package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.CosmosAsyncContainer;

public class ThroughputLocalControlGroup extends ThroughputControlGroupInternal {

    public ThroughputLocalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        boolean isDefault) {
        super (groupName, targetContainer, targetThroughput, targetThroughputThreshold, isDefault);
    }
}
