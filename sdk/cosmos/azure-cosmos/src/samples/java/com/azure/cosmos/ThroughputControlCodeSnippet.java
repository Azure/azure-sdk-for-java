// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;

import java.time.Duration;

public class ThroughputControlCodeSnippet {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    public ThroughputControlCodeSnippet() {
        this.client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();

        this.database = this.client.getDatabase("TestDB");
        this.container = this.database.getContainer("TestContainer");
    }

    public void codeSnippetForEnableLocalThroughputControl() {
        // BEGIN: com.azure.cosmos.throughputControl.localControl
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .setGroupName("localControlGroup")
                .setTargetThroughputThreshold(0.1)
                .build();

        container.enableLocalThroughputControlGroup(groupConfig);
        // END: com.azure.cosmos.throughputControl.localControl
    }

    public void codeSnippetForEnableGlobalThroughputControl() {
        // BEGIN: com.azure.cosmos.throughputControl.globalControl
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .setGroupName("localControlGroup")
                .setTargetThroughputThreshold(0.1)
                .build();

        GlobalThroughputControlConfig globalControlConfig =
            this.client.createGlobalThroughputControlConfigBuilder(database.getId(), container.getId())
                .setControlItemRenewInterval(Duration.ofSeconds(5))
                .setControlItemExpireInterval(Duration.ofSeconds(10))
                .build();

        container.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);
        // END: com.azure.cosmos.throughputControl.globalControl
    }
}
