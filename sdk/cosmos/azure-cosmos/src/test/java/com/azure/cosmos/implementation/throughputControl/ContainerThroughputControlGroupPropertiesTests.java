// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.config.LocalThroughputControlGroup;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ContainerThroughputControlGroupPropertiesTests {

    @Test(groups = "emulator")
    public void enableThroughputControlGroup() {
        CosmosAsyncClient testClient = null;
        try {
            testClient = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .buildAsyncClient();

            ContainerThroughputControlGroupProperties throughputControlContainerProperties = new ContainerThroughputControlGroupProperties();

            CosmosAsyncContainer container = testClient.getDatabase("fakeDatabase").getContainer("fakeContainer");

            // Test 1: add default throughput control group successfully
            LocalThroughputControlGroup throughputControlDefaultGroup = new LocalThroughputControlGroup(
                    "test-" + UUID.randomUUID(),
                    container,
                    6,
                    null,
                    true,
                    false);

            int currentGroupSize = throughputControlContainerProperties.enableThroughputControlGroup(throughputControlDefaultGroup);
            assertThat(currentGroupSize).isEqualTo(1);

            // Test 2: add throughput control group with same id
            LocalThroughputControlGroup throughputControlGroupDuplciate = new LocalThroughputControlGroup(
                    throughputControlDefaultGroup.getGroupName(),
                    container,
                    6,
                    null,
                    false,
                    false);

            assertThatThrownBy(() -> throughputControlContainerProperties.enableThroughputControlGroup(throughputControlGroupDuplciate))
                    .isInstanceOf(IllegalArgumentException.class);

            // Test 3: add another default group
            LocalThroughputControlGroup throughputControlDefaultGroup2 = new LocalThroughputControlGroup(
                    "test-" + UUID.randomUUID(),
                    container,
                    6,
                    null,
                    true,
                    false);
            assertThatThrownBy(() -> throughputControlContainerProperties.enableThroughputControlGroup(throughputControlDefaultGroup2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A default group already exists");

            // Test 4: add a new group
            LocalThroughputControlGroup newGroup = new LocalThroughputControlGroup(
                    "test-" + UUID.randomUUID(),
                    container,
                    6,
                    null,
                    false,
                    false);
            currentGroupSize = throughputControlContainerProperties.enableThroughputControlGroup(newGroup);
            assertThat(currentGroupSize).isEqualTo(2);

            // Test 5: add a same group as step 4
            LocalThroughputControlGroup newGroupDuplicate = new LocalThroughputControlGroup(
                    newGroup.getGroupName(),
                    container,
                    newGroup.getTargetThroughput(),
                    newGroup.getTargetThroughputThreshold(),
                    newGroup.isDefault(),
                    newGroup.isContinueOnInitError());
            currentGroupSize = throughputControlContainerProperties.enableThroughputControlGroup(newGroupDuplicate);
            assertThat(currentGroupSize).isEqualTo(2);
        } finally {
            if (testClient != null) {
                testClient.close();
            }
        }
    }
}
