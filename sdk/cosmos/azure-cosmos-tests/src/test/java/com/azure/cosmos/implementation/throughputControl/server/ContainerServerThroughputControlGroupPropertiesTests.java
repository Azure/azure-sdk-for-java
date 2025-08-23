// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.server.config.ServerThroughputControlGroup;
import com.azure.cosmos.models.PriorityLevel;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ContainerServerThroughputControlGroupPropertiesTests {
    @Test(groups = "emulator")
    public void enableThroughputControlGroup() {
        CosmosAsyncClient testClient = null;
        try {
            testClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            ContainerServerThroughputControlGroupProperties throughputControlContainerProperties =
                new ContainerServerThroughputControlGroupProperties("/testDB/testContainer");

            CosmosAsyncContainer container = testClient.getDatabase("fakeDatabase").getContainer("fakeContainer");

            // Test 1: add default throughput control group successfully
            ServerThroughputControlGroup throughputControlDefaultGroup = new ServerThroughputControlGroup(
                "test-" + UUID.randomUUID(),
                true,
                PriorityLevel.HIGH,
                1,
                container);

            Integer currentGroupSize =
                throughputControlContainerProperties.enableThroughputControlGroup(throughputControlDefaultGroup);
            assertThat(currentGroupSize).isEqualTo(1);
            assertThat(throughputControlContainerProperties.hasDefaultGroup()).isTrue();
            assertThat(throughputControlContainerProperties.hasGroup(throughputControlDefaultGroup.getGroupName())).isTrue();

            // Test 2: add another default throughput control group
            ServerThroughputControlGroup throughputControlDefaultGroup2 = new ServerThroughputControlGroup(
                "test-" + UUID.randomUUID(),
                true,
                PriorityLevel.HIGH,
                2,
                container);

            assertThatThrownBy(
                () -> throughputControlContainerProperties
                    .enableThroughputControlGroup(throughputControlDefaultGroup2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A default group already exists");
            assertThat(throughputControlContainerProperties.hasGroup(throughputControlDefaultGroup2.getGroupName())).isFalse();

            // Test 3: add a new non-default group
            ServerThroughputControlGroup throughputControlGroup1 = new ServerThroughputControlGroup(
                "test-" + UUID.randomUUID(),
                false,
                PriorityLevel.HIGH,
                1,
                container);

            currentGroupSize = throughputControlContainerProperties.enableThroughputControlGroup(throughputControlGroup1);
            assertThat(currentGroupSize).isEqualTo(2);
            assertThat(throughputControlContainerProperties.hasGroup(throughputControlGroup1.getGroupName())).isTrue();

            // Test 4: add a throughput control group with name as throughputControlGroup1 but with different config
            ServerThroughputControlGroup throughputControlGroup2 = new ServerThroughputControlGroup(
                throughputControlGroup1.getGroupName(),
                false,
                PriorityLevel.HIGH,
                2,
                container);
            assertThatThrownBy(
                () -> throughputControlContainerProperties
                    .enableThroughputControlGroup(throughputControlGroup2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A group with same name already exists, name: " + throughputControlGroup1.getGroupName());

            // Test 5: add a throughput control group with same config as throughputControlGroup1, to verify no errors will be thrown
            ServerThroughputControlGroup throughputControlGroup3 = new ServerThroughputControlGroup(
                throughputControlGroup1.getGroupName(),
                throughputControlGroup1.isDefault(),
                throughputControlGroup1.getPriorityLevel(),
                throughputControlGroup1.getThroughputBucket(),
                container);

            currentGroupSize = throughputControlContainerProperties.enableThroughputControlGroup(throughputControlGroup3);
            assertThat(currentGroupSize).isEqualTo(2);

        } finally {
            if (testClient != null) {
                testClient.close();
            }
        }
    }
}
