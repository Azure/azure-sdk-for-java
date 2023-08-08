// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

public class PartitionedQueueClientSessionTests extends QueueClientSessionTests {
    @Override
    public String getEntityNamePrefix() {
        return "PartitionedQueueClientSessionTests";
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }

    @Override
    public boolean isEntityPartitioned() {
        return true;
    }
}
