// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
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

            Pair<Integer, Boolean> stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(throughputControlDefaultGroup);
            int currentGroupSize = stateAfterEnabling.getLeft();
            boolean wasGroupConfigUpdated = stateAfterEnabling.getRight();

            assertThat(currentGroupSize).isEqualTo(1);
            assertThat(wasGroupConfigUpdated).isEqualTo(false);

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

            stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(newGroup);
            currentGroupSize = stateAfterEnabling.getLeft();
            wasGroupConfigUpdated = stateAfterEnabling.getRight();

            assertThat(currentGroupSize).isEqualTo(2);
            assertThat(wasGroupConfigUpdated).isEqualTo(false);

            // Test 5: add a same group as step 4
            LocalThroughputControlGroup newGroupDuplicate = new LocalThroughputControlGroup(
                    newGroup.getGroupName(),
                    container,
                    newGroup.getTargetThroughput(),
                    newGroup.getTargetThroughputThreshold(),
                    newGroup.isDefault(),
                    newGroup.isContinueOnInitError());

            stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(newGroupDuplicate);
            currentGroupSize = stateAfterEnabling.getLeft();
            wasGroupConfigUpdated = stateAfterEnabling.getRight();

            assertThat(currentGroupSize).isEqualTo(2);
            assertThat(wasGroupConfigUpdated).isEqualTo(false);

            // Test 6: add a same group as step 4 but with updated threshold
            LocalThroughputControlGroup newGroupDuplicateModified = new LocalThroughputControlGroup(
                newGroup.getGroupName(),
                container,
                newGroup.getTargetThroughput() != null ? newGroup.getTargetThroughput() * 2 : null,
                newGroup.getTargetThroughputThreshold()  != null ? newGroup.getTargetThroughputThreshold() * 2: null,
                newGroup.isDefault(),
                newGroup.isContinueOnInitError());

            stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(newGroupDuplicateModified);
            currentGroupSize = stateAfterEnabling.getLeft();
            wasGroupConfigUpdated = stateAfterEnabling.getRight();

            assertThat(currentGroupSize).isEqualTo(2);
            assertThat(wasGroupConfigUpdated).isEqualTo(true);
        } finally {
            if (testClient != null) {
                testClient.close();
            }
        }
    }
}
