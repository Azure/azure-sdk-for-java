// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

public class QueueClientTests extends ClientTests {
    @Override
    public String getEntityNamePrefix() {
        return "QueueClientTests";
    }

    @Override
    public boolean isEntityQueue() {
        return true;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }

    @Override
    public boolean isEntityPartitioned() {
        return false;
    }
}
