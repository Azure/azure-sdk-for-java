// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

public class PartitionedSubscriptionClientTests extends SubscriptionClientTests {
    @Override
    public String getEntityNamePrefix() {
        return "PartitionedSubscriptionClientTests";
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
