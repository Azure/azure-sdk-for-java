// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.throughputControl.sdk.config.LocalThroughputControlGroup;
import com.azure.cosmos.models.PriorityLevel;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ContainerSDKThroughputControlGroupPropertiesTests {

    @Test(groups = "emulator")
    public void enableThroughputControlGroup() {
        CosmosAsyncClient testClient = null;
        try {
            testClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            ContainerSDKThroughputControlGroupProperties throughputControlContainerProperties =
                new ContainerSDKThroughputControlGroupProperties("/testDB/testContainer");

            CosmosAsyncContainer container = testClient.getDatabase("fakeDatabase").getContainer("fakeContainer");

            // Test 1: add default throughput control group successfully
            LocalThroughputControlGroup throughputControlDefaultGroup = new LocalThroughputControlGroup(
                "test-" + UUID.randomUUID(),
                container,
                6,
                null,
                PriorityLevel.HIGH,
                true,
                false);

            Pair<Integer, Boolean> stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(throughputControlDefaultGroup, null);
            int currentGroupSize = stateAfterEnabling.getLeft();
            boolean wasGroupConfigUpdated = stateAfterEnabling.getRight();
            assertThat(currentGroupSize).isEqualTo(1);
            assertThat(wasGroupConfigUpdated).isEqualTo(false);

            // Test 2: add throughput control group with same id, but different values for immutable
            // properties isDefault or continueOnError
            LocalThroughputControlGroup throughputControlGroupDuplicate1 = new LocalThroughputControlGroup(
                throughputControlDefaultGroup.getGroupName(),
                container,
                6,
                null,
                PriorityLevel.HIGH,
                false,
                false);

            assertThatThrownBy(
                () -> throughputControlContainerProperties
                    .enableThroughputControlGroup(throughputControlGroupDuplicate1, null)
            ).isInstanceOf(IllegalArgumentException.class);

            LocalThroughputControlGroup throughputControlGroupDuplicate2 = new LocalThroughputControlGroup(
                throughputControlDefaultGroup.getGroupName(),
                container,
                6,
                null,
                PriorityLevel.HIGH,
                true,
                true);

            assertThatThrownBy(
                () -> throughputControlContainerProperties
                    .enableThroughputControlGroup(throughputControlGroupDuplicate2, null)
            ).isInstanceOf(IllegalArgumentException.class);


            // Test 3: add another default group
            LocalThroughputControlGroup throughputControlDefaultGroup2 = new LocalThroughputControlGroup(
                "test-" + UUID.randomUUID(),
                container,
                6,
                null,
                PriorityLevel.HIGH,
                true,
                false);
            assertThatThrownBy(() -> throughputControlContainerProperties.enableThroughputControlGroup(throughputControlDefaultGroup2, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A default group already exists");

            // Test 4: add a new group
            LocalThroughputControlGroup newGroup = new LocalThroughputControlGroup(
                "test-" + UUID.randomUUID(),
                container,
                6,
                null,
                PriorityLevel.HIGH,
                false,
                false);
            stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(newGroup, null);
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
                PriorityLevel.HIGH,
                newGroup.isDefault(),
                newGroup.isContinueOnInitError());
            stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(newGroupDuplicate, null);
            currentGroupSize = stateAfterEnabling.getLeft();
            wasGroupConfigUpdated = stateAfterEnabling.getRight();
            assertThat(currentGroupSize).isEqualTo(2);
            assertThat(wasGroupConfigUpdated).isEqualTo(false);

            // Test 6: add a same group as step 4 - but with modified target throughput
            LocalThroughputControlGroup newGroupDuplicateModifiedTarget = new LocalThroughputControlGroup(
                newGroup.getGroupName(),
                container,
                newGroup.getTargetThroughput() + 1,
                newGroup.getTargetThroughputThreshold(),
                PriorityLevel.HIGH,
                newGroup.isDefault(),
                newGroup.isContinueOnInitError());
            stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(newGroupDuplicateModifiedTarget,
                    null);
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

    @Test(groups = "emulator")
    public void enableThroughputControlGroupWithoutDefault() {
        CosmosAsyncClient testClient = null;
        try {
            testClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            ContainerSDKThroughputControlGroupProperties throughputControlContainerProperties =
                new ContainerSDKThroughputControlGroupProperties("/testDB/testContainer");

            //  Test: Without default group and request not having the group name, allowRequestToContinueOnInitError
            //  should not throw NPE
            boolean allowRequestToContinue =
                throughputControlContainerProperties.allowRequestToContinueOnInitError(Mockito.mock(RxDocumentServiceRequest.class));

            assertThat(allowRequestToContinue).isTrue();

        } finally {
            if (testClient != null) {
                testClient.close();
            }
        }
    }
}
