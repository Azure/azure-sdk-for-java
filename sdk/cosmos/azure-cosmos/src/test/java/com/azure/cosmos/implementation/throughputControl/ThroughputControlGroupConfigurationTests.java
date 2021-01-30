// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThroughputControlGroupConfigurationTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ThroughputControlGroupConfigurationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "emulator" })
    public void validateMultipleDefaultGroups() {
        ThroughputControlGroup group1 = container.createThroughputControlGroup("group-1", 10);
        group1.setUseByDefault();

        ThroughputControlGroup group2 = container.createThroughputControlGroup("group-2", 10);
        group2.setUseByDefault();

        Set<ThroughputControlGroup> groups = new HashSet<>();
        groups.add(group1);
        groups.add(group2);
        assertThatThrownBy(() -> client.enableThroughputControl(groups))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Only at most one group can be set as default");
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ThroughputControlGroupConfigurationTests() {
        client = getClientBuilder().buildAsyncClient();
        container = getSharedMultiPartitionCosmosContainer(client);
    }
}
