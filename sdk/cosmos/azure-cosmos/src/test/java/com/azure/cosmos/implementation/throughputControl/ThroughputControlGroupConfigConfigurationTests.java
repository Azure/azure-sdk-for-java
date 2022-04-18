// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GlobalThroughputControlConfigBuilder;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThroughputControlGroupConfigConfigurationTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ThroughputControlGroupConfigConfigurationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "emulator" })
    public void validateMultipleDefaultGroups() {
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-1")
                .targetThroughput(10)
                .defaultControlGroup(true)
                .build();
        container.enableLocalThroughputControlGroup(groupConfig);

        ThroughputControlGroupConfig groupConfig2 =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-2")
                .targetThroughputThreshold(1.0)
                .defaultControlGroup(true)
                .build();
        assertThatThrownBy(() -> container.enableLocalThroughputControlGroup(groupConfig2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("A default group already exists");
    }

    @Test(groups = { "emulator" })
    public void validateGlobalThroughputControlGroup() {
        GlobalThroughputControlConfigBuilder globalControlConfigBuilder =
            client.createGlobalThroughputControlConfigBuilder(database.getId(), container.getId());

        assertThatThrownBy(() -> globalControlConfigBuilder.setControlItemRenewInterval(Duration.ofMillis(1000)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Renew interval should be no less than 5s");
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ThroughputControlGroupConfigurationTests() {
        client = getClientBuilder().buildAsyncClient();
        database = getSharedCosmosDatabase(client);
        container = getSharedMultiPartitionCosmosContainer(client);
    }
}
