// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThroughputControlGroupConfigurationTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ThroughputControlGroupConfigurationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "invalidThroughputControlGroup")
    public Object[][] invalidThroughputControlGroup() {
        return new Object[][]{
            // group name, targetThroughput, targetThroughputThreshold, exception
            {"group", -1, null, IllegalArgumentException.class},
            {"group", null, -0.2, IllegalArgumentException.class},
            {"", 10, null, IllegalArgumentException.class},
            {"group", 10, null, null},
            {"group", null, 0.2, null}
        };
    }

    @Test(groups = { "emulator" }, dataProvider = "invalidThroughputControlGroup")
    public void validateSingleGroupConfiguration(
        String groupName,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        Class<?> exceptionType) {

        try {
            ThroughputControlGroup group = container.createThroughputControlGroup(groupName);

            if (targetThroughput != null) {
                group.setTargetThroughput(targetThroughput);
            }

            if (targetThroughputThreshold != null) {
                group.setTargetThroughputThreshold(targetThroughputThreshold);
            }

            Set<ThroughputControlGroup> groups = new HashSet<>();
            groups.add(group);

            client.enableThroughputControl(groups);
        } catch (Exception exception) {
            if (exceptionType != null) {
                assertThat(exception).isInstanceOf(exceptionType);
            }
        }
    }

    @Test(groups = { "emulator" })
    public void validateMultipleDefaultGroups() {
        ThroughputControlGroup group1 = container.createThroughputControlGroup("group-1");
        group1.setTargetThroughput(10)
            .setUseByDefault();

        ThroughputControlGroup group2 = container.createThroughputControlGroup("group-2")
            .setTargetThroughput(10)
            .setTargetThroughputThreshold(0.2)
            .setUseByDefault();

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
