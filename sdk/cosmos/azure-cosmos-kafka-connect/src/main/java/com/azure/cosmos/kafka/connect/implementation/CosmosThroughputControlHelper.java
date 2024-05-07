// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.GlobalThroughputControlConfigBuilder;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PriorityLevel;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosThroughputControlHelper {
    public static CosmosAsyncContainer tryEnableThroughputControl(
        CosmosAsyncContainer container,
        CosmosAsyncClient throughputControlCosmosClient,
        CosmosThroughputControlConfig cosmosThroughputControlConfig) {

        checkNotNull(container, "Argument 'container' should not be null");
        if (cosmosThroughputControlConfig == null || !cosmosThroughputControlConfig.isThroughputControlEnabled()) {
            return container;
        }

        enableGlobalThroughputControl(container, throughputControlCosmosClient, cosmosThroughputControlConfig);
        return container;
    }

    private static void enableGlobalThroughputControl(
        CosmosAsyncContainer container,
        CosmosAsyncClient throughputControlCosmosClient,
        CosmosThroughputControlConfig throughputControlConfig) {

        ThroughputControlGroupConfigBuilder groupConfigBuilder =
            new ThroughputControlGroupConfigBuilder().groupName(throughputControlConfig.getThroughputControlGroupName());

        if (throughputControlConfig.getTargetThroughput() > 0) {
            groupConfigBuilder.targetThroughput(throughputControlConfig.getTargetThroughput());
        }

        if (throughputControlConfig.getTargetThroughputThreshold() > 0) {
            groupConfigBuilder.targetThroughputThreshold(throughputControlConfig.getTargetThroughputThreshold());
        }

        switch (throughputControlConfig.getPriorityLevel()) {
            case NONE:
                break;
            case LOW:
                groupConfigBuilder.priorityLevel(PriorityLevel.LOW);
                break;
            case HIGH:
                groupConfigBuilder.priorityLevel(PriorityLevel.HIGH);
                break;
            default:
                throw new IllegalArgumentException("Priority level " + throughputControlConfig.getPriorityLevel() + " is not supported");
        }

        GlobalThroughputControlConfigBuilder globalThroughputControlConfigBuilder =
            throughputControlCosmosClient.createGlobalThroughputControlConfigBuilder(
                throughputControlConfig.getGlobalThroughputControlDatabaseName(),
                throughputControlConfig.getGlobalThroughputControlContainerName());
        if (throughputControlConfig.getGlobalThroughputControlRenewInterval() != null) {
            globalThroughputControlConfigBuilder.setControlItemRenewInterval(throughputControlConfig.getGlobalThroughputControlRenewInterval());
        }

        if (throughputControlConfig.getGlobalThroughputControlExpireInterval() != null) {
            globalThroughputControlConfigBuilder.setControlItemExpireInterval(throughputControlConfig.getGlobalThroughputControlExpireInterval());
        }

        container.enableGlobalThroughputControlGroup(groupConfigBuilder.build(), globalThroughputControlConfigBuilder.build());
    }

    public static void tryPopulateThroughputControlGroupName(
        CosmosBulkExecutionOptions bulkExecutionOptions,
        CosmosThroughputControlConfig throughputControlConfig) {

        if (throughputControlConfig.isThroughputControlEnabled()) {
            bulkExecutionOptions.setThroughputControlGroupName(throughputControlConfig.getThroughputControlGroupName());
        }
    }

    public static void tryPopulateThroughputControlGroupName(
        CosmosItemRequestOptions itemRequestOptions,
        CosmosThroughputControlConfig throughputControlConfig) {

        if (throughputControlConfig.isThroughputControlEnabled()) {
            itemRequestOptions.setThroughputControlGroupName(throughputControlConfig.getThroughputControlGroupName());
        }
    }

    public static void tryPopulateThroughputControlGroupName(
        CosmosChangeFeedRequestOptions changeFeedRequestOptions,
        CosmosThroughputControlConfig throughputControlConfig) {

        if (throughputControlConfig.isThroughputControlEnabled()) {
            changeFeedRequestOptions.setThroughputControlGroupName(throughputControlConfig.getThroughputControlGroupName());
        }
    }
}
