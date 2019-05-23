// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

public class PartitionedTopicSendReceiveTests extends TopicSendReceiveTests {
    @Override
    public String getEntityNamePrefix() {
        return "PartitionedTopicSendReceiveTests";
    }

    @Override
    public boolean isEntityPartitioned() {
        return true;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }
}
