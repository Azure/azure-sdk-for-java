package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.ThroughputControlGroup;

import java.time.Duration;

public class ThroughputControlGroupFactory {

    // region createThroughputLocalControlGroup

    public static ThroughputControlGroup createThroughputLocalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        int targetThroughput) {

        return createThroughputLocalControlGroup(groupName, targetContainer, targetThroughput, null, false);
    }

    public static ThroughputControlGroup createThroughputLocalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        int targetThroughput,
        boolean isDefault) {

        return createThroughputLocalControlGroup(groupName, targetContainer, targetThroughput, null, isDefault);
    }

    public static ThroughputControlGroup createThroughputLocalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        double targetThroughputThreshold) {

        return createThroughputLocalControlGroup(groupName, targetContainer, null, targetThroughputThreshold, false);
    }

    public static ThroughputControlGroup createThroughputLocalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        double targetThroughputThreshold,
        boolean isDefault) {

        return createThroughputLocalControlGroup(groupName, targetContainer, null, targetThroughputThreshold, isDefault);
    }

    private static ThroughputControlGroup createThroughputLocalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        boolean isDefault) {

        ThroughputLocalControlGroup localControlGroup = new ThroughputLocalControlGroup(
            groupName, targetContainer, targetThroughput, targetThroughputThreshold, isDefault);

        return BridgeInternal.createThroughputControlGroup(localControlGroup);
    }

    // endregion

    // region createThroughputGlobalControlGroup

    public static ThroughputControlGroup createThroughputGlobalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        int targetThroughput,
        CosmosAsyncContainer controlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        return createThroughputGlobalControlGroup(
            groupName,
            targetContainer,
            targetThroughput,
            null,
            false,
            controlContainer,
            controlItemRenewInterval,
            controlItemExpireInterval);
    }

    public static ThroughputControlGroup createThroughputGlobalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        int targetThroughput,
        boolean isDefault,
        CosmosAsyncContainer controlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        return createThroughputGlobalControlGroup(
            groupName,
            targetContainer,
            targetThroughput,
            null,
            isDefault,
            controlContainer,
            controlItemRenewInterval,
            controlItemExpireInterval);
    }

    public static ThroughputControlGroup createThroughputGlobalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        double targetThroughputThreshold,
        CosmosAsyncContainer controlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        return createThroughputGlobalControlGroup(
            groupName,
            targetContainer,
            null,
            targetThroughputThreshold,
            false,
            controlContainer,
            controlItemRenewInterval,
            controlItemExpireInterval);
    }

    public static ThroughputControlGroup createThroughputGlobalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        double targetThroughputThreshold,
        boolean isDefault,
        CosmosAsyncContainer controlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        return createThroughputGlobalControlGroup(
            groupName,
            targetContainer,
            null,
            targetThroughputThreshold,
            isDefault,
            controlContainer,
            controlItemRenewInterval,
            controlItemExpireInterval);
    }

    private static ThroughputControlGroup createThroughputGlobalControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        boolean isDefault,
        CosmosAsyncContainer controlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        ThroughputGlobalControlGroup globalControlGroup =
            new ThroughputGlobalControlGroup(
                groupName,
                targetContainer,
                targetThroughput,
                targetThroughputThreshold,
                isDefault,
                controlContainer,
                controlItemRenewInterval,
                controlItemExpireInterval);

        return BridgeInternal.createThroughputControlGroup(globalControlGroup);
    }

    // endregion
}
